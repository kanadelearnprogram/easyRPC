package com.kanade.easyrpc.server;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;

public class TcpClient {
    public void start(){
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(8888,"localhost",result ->{
            if (result.succeeded()){
                System.out.println("successful TCP connect");
                NetSocket socket = result.result();
                socket.write("ciallo1");

                socket.handler(buffer -> {
                    System.out.println(buffer.toString());
                });
            }else{
                System.err.println("Failed to connect to TCP server");
            }
        });
    }
    public static void main(String[] args) {
        new TcpClient().start();
    }
}
