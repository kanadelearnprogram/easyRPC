package com.kanade.provider;

import com.kanade.common.model.User;
import com.kanade.common.service.UserService;
import com.kanade.easyrpc.registry.LocalRegistry;
import com.kanade.easyrpc.server.HttpServer;
import com.kanade.easyrpc.server.VertxHttpServer;

public class EasyProvider {
    public static void main(String[] args) {
        // registry
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);


        // provide service
        HttpServer httpServer = new VertxHttpServer();

        httpServer.start(8080);
    }
}
