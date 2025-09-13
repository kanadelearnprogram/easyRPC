package com.kanade.provider;


import com.kanade.common.service.UserService;
import easyrpc.RPCApplication;
import easyrpc.config.RPCConfig;
import easyrpc.registry.LocalRegistry;
import easyrpc.server.HttpServer;
import easyrpc.server.VertxHttpServer;
import easyrpc.utils.ConfigUtils;


public class EasyProvider {
    public static void main(String[] args) {
        RPCApplication.init();
        // registry
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);


        // provide service
        HttpServer httpServer = new VertxHttpServer();

        httpServer.start(RPCApplication.getRpcConfig().getServerPort());
        //httpServer.start(8080);
    }
}
