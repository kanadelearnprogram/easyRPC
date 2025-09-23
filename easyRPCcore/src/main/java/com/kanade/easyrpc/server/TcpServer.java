package com.kanade.easyrpc.server;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;

public class TcpServer implements HttpServer{
    byte[] handleRequest(byte[] requestData){
        return "ciallo".getBytes();
    }
    @Override
    public void start(int port) {

        Vertx vertx = Vertx.vertx();

        NetServer server = vertx.createNetServer();

        server.connectHandler(socket -> {
            socket.handler(buffer -> {
                byte[] requestData = buffer.getBytes();
                byte[] responseData = handleRequest(requestData);
                socket.write(Buffer.buffer(responseData));
            });
        });

        server.listen(port, result ->{
            if (result.succeeded()) {
                System.out.println("TCP server started on port " + port);
            } else {
                System.err.println("Failed to start TCP server: " + result.cause());
            }
        });
    }
    public static void main(String[] args) {
        new TcpServer().start(8888);
    }
}
