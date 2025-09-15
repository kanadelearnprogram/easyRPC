package com.kanade.easyrpc.registry;


import cn.hutool.json.JSONUtil;
import com.kanade.easyrpc.config.RegistryConfig;
import com.kanade.easyrpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry {
    private Client client;
    private KV kv;

    private static final String PATH = "/rpc/";


    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kv = client.getKVClient();
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
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        kv.delete(ByteSequence.from(PATH + serviceMetaInfo.getServiceNode(),StandardCharsets.UTF_8));
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscover(String serviceKey) {

        // 前缀搜索，结尾一定要加 '/'
        String searchPrefix = PATH + serviceKey + "/";

        try {
            // 前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kv.get(
                            ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                            getOption)
                    .get()
                    .getKvs();
            // 解析服务信息
            return keyValues.stream()
                    .map(keyValue -> {
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }


    }

    @Override
    public void destroy() {
        System.out.println("当前节点下线");
        // 释放资源
        if (kv != null) {
            kv.close();
        }
        if (client != null) {
            client.close();
        }
    }
}
