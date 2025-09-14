package easyrpc.registry;

import easyrpc.config.RegistryConfig;
import easyrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface Registry {
    void init(RegistryConfig registryConfig);
    void register(ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException;
    void unregister(ServiceMetaInfo serviceMetaInfo);
    List<ServiceMetaInfo> serviceDiscover(String serviceKey);
    void destroy();
}
