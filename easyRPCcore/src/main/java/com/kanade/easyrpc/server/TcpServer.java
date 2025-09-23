package com.kanade.easyrpc.server;

import com.kanade.easyrpc.server.tcp.TcpBufferHandlerWrapper;
import com.kanade.easyrpc.server.tcp.TcpServerHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcpServer implements HttpServer{
    byte[] handleRequest(byte[] requestData){
        return requestData;
    }
    @Override
    public void start(int port) {

        Vertx vertx = Vertx.vertx();

        NetServer server = vertx.createNetServer();

        server.connectHandler(new TcpServerHandler());
        TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            // 处理请求代码
        });

        /*server.connectHandler(socket -> {
            // 构造 parser
            RecordParser parser = RecordParser.newFixed(8);
            parser.setOutput(new Handler<Buffer>() {
                // 初始化
                int size = -1;
                // 一次完整的读取（头 + 体）
                Buffer resultBuffer = Buffer.buffer();

                @Override
                public void handle(Buffer buffer) {
                    if (-1 == size) {
                        // 读取消息体长度
                        size = buffer.getInt(4);
                        parser.fixedSizeMode(size);
                        // 写入头信息到结果
                        resultBuffer.appendBuffer(buffer);
                    } else {
                        // 写入体信息到结果
                        resultBuffer.appendBuffer(buffer);
                        System.out.println(resultBuffer.toString());
                        // 重置一轮
                        parser.fixedSizeMode(8);
                        size = -1;
                        resultBuffer = Buffer.buffer();
                    }
                }
            });

            socket.handler(parser);
        });*/
        /*server.connectHandler(socket -> {
            socket.handler(buffer -> {
                byte[] requestData = buffer.getBytes();
                byte[] responseData = handleRequest(requestData);
                socket.write(Buffer.buffer(responseData));
            });
        });*/

        server.listen(port, result ->{
            if (result.succeeded()) {
                log.info("TCP server started on port " + port);
            } else {
                log.info("Failed to start TCP server: " + result.cause());
            }
        });
    }
    public static void main(String[] args) {
        new TcpServer().start(8888);
    }
}
