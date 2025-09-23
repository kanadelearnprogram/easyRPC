package com.kanade.easyrpc.registry;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.kanade.easyrpc.config.RegistryConfig;
import com.kanade.easyrpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.vertx.core.impl.ConcurrentHashSet;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry {
    private Client client;
    private KV kv;

    private static final String PATH = "/rpc/";
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kv = client.getKVClient();
        beats();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        Lease leaseClient = client.getLeaseClient();

        long lease = leaseClient.grant(30).get().getID();

        String registryKey = PATH + serviceMetaInfo.getServiceNode();
        ByteSequence k = ByteSequence.from(registryKey, StandardCharsets.UTF_8);
        ByteSequence v = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo),StandardCharsets.UTF_8);

        PutOption putOption = PutOption.builder().withLeaseId(lease).build();
        kv.put(k,v,putOption).get();

        localRegisterNodeKeySet.add(registryKey);
        //registryServiceCache.writeCache();
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        kv.delete(ByteSequence.from(PATH + serviceMetaInfo.getServiceNode(),StandardCharsets.UTF_8));
        localRegisterNodeKeySet.remove(PATH + serviceMetaInfo.getServiceNode());
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscover(String serviceKey) {

        // 前缀搜索，结尾一定要加 '/'
        String searchPrefix = PATH + serviceKey + "/";

        List<ServiceMetaInfo> list = registryServiceCache.readCache(serviceKey);
        if (list != null){
            return list;
        }

        try {
            // 前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kv.get(
                            ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                            getOption)
                    .get()
                    .getKvs();

            // 解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues
                    .stream()
                    .map(keyValue -> {
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        // 监听 key 的变化
                        watch(key);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());

            registryServiceCache.writeCache(serviceKey,serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }


    }

    @Override
    public void destroy() {
        System.out.println("当前节点下线");
        for (String key : localRegisterNodeKeySet) {
            try {
                kv.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }
        // 释放资源
        if (kv != null) {
            kv.close();
        }
        if (client != null) {
            client.close();
        }
    }

    public  void beats(){
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历节点 获取key
                for (String key: localRegisterNodeKeySet){
                    try {
                        // 使用kv 获取信息
                        List<KeyValue> keyValues = kv.get(ByteSequence.from(key,StandardCharsets.UTF_8)).get().getKvs();
                        // 检查是否为空
                        if (CollUtil.isEmpty(keyValues)){
                            continue;
                        }
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString();
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }


            }
        });
        CronUtil.setMatchSecond(true);
        CronUtil.start();

    }

    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        // 之前未被监听，开启监听
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if (newWatch) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
                for (WatchEvent event : response.getEvents()) {
                    switch (event.getEventType()) {
                        // key 删除时触发
                        case DELETE:
                            // 清理注册服务缓存
                            registryServiceCache.clearCache(serviceNodeKey);
                            break;
                        case PUT :/*->{
                            if (registryServiceCache.containsCache(serviceKey, watchServiceNodeKey)) {
                                break;
                            }
                            ServiceMetaInfo newServiceMetaInfo = JSONUtil.toBean(keyValue.getValue().toString(StandardCharsets.UTF_8), ServiceMetaInfo.class);
                            registryServiceCache.writeCache(serviceKey, watchServiceNodeKey, newServiceMetaInfo);
                        }*/
                        
                        default:
                            break;
                    }
                }
            });
        }
    }


}
