package com.kanade.easyrpc.server;

import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer{

    @Override
    public void start(int port) {

        Vertx vertx = Vertx.vertx();

        io.vertx.core.http.HttpServer httpServer = vertx.createHttpServer();

        // Listening prot and request response
        httpServer.requestHandler(new HttpServerHandler());

        httpServer.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server is now listening on port " + port);
            } else {
                System.err.println("Failed to start server: " + result.cause());
            }
        });
    }
}
