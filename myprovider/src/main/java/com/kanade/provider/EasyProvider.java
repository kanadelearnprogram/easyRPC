package com.kanade.provider;


import com.kanade.common.service.UserService;
import easyrpc.RPCApplication;
import easyrpc.config.RPCConfig;
import easyrpc.config.RegistryConfig;
import easyrpc.model.ServiceMetaInfo;
import easyrpc.registry.LocalRegistry;
import easyrpc.registry.Registry;
import easyrpc.registry.RegistryFactory;
import easyrpc.server.HttpServer;
import easyrpc.server.VertxHttpServer;
import easyrpc.utils.ConfigUtils;


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
