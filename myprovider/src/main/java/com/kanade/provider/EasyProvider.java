package com.kanade.provider;


import com.kanade.common.service.UserService;
import com.kanade.easyrpc.RPCApplication;
import com.kanade.easyrpc.config.RPCConfig;
import com.kanade.easyrpc.config.RegistryConfig;
import com.kanade.easyrpc.model.ServiceMetaInfo;
import com.kanade.easyrpc.registry.LocalRegistry;
import com.kanade.easyrpc.registry.Registry;
import com.kanade.easyrpc.registry.RegistryFactory;
import com.kanade.easyrpc.server.HttpServer;
import com.kanade.easyrpc.server.VertxHttpServer;


public class EasyProvider {
    public static void main(String[] args) {
        RPCApplication.init();
        // registry
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 注册服务到注册中心
        RPCConfig rpcConfig = RPCApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(UserService.class.getName());
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());

        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // provide service
        HttpServer httpServer = new VertxHttpServer();

        httpServer.start(RPCApplication.getRpcConfig().getServerPort());
        //httpServer.start(8080);
    }
}
