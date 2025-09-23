package com.kanade.easyrpc.registry;

import com.kanade.easyrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RegistryServiceCache {
    private Map<String, List<ServiceMetaInfo>> cache= new ConcurrentHashMap<>();

    void writeCache(String key, List<ServiceMetaInfo> value){
        cache.put(key,value);
    }

    public List<ServiceMetaInfo> readCache(String key){
        return cache.get(key);
    }

    public void clearCache(String key){
        cache.remove(key);
    }
    public boolean containsCache(String serviceKey, String serviceNodeKey) {
        List<ServiceMetaInfo> metaInfoList = cache.get(serviceKey);
        if (metaInfoList == null) {
            return false;
        }
        return metaInfoList.contains(serviceNodeKey);
    }

    public boolean containsKey(String serviceKey) {
        return cache.containsKey(serviceKey);
    }
}
