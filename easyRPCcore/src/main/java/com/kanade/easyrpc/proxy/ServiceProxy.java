package com.kanade.easyrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import com.kanade.easyrpc.RPCApplication;
import com.kanade.easyrpc.config.RPCConfig;
import com.kanade.easyrpc.constant.RPCConstant;
import com.kanade.easyrpc.model.RPCRequest;
import com.kanade.easyrpc.model.RPCResponse;
import com.kanade.easyrpc.model.ServiceMetaInfo;
import com.kanade.easyrpc.protocol.*;
import com.kanade.easyrpc.registry.Registry;
import com.kanade.easyrpc.registry.RegistryFactory;
import com.kanade.easyrpc.serializer.Serializer;
import com.kanade.easyrpc.serializer.SerializerFactory;
import com.kanade.easyrpc.server.TcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;


import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Serializer serializer = SerializerFactory.getInstance(RPCApplication.getRpcConfig().getSerializer());

        String serviceName = method.getDeclaringClass().getName();

        RPCRequest request = RPCRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try{
            byte[] bytes = serializer.serialize(request);
            // 需要注册中心
            RPCConfig rpcConfig = RPCApplication.getRpcConfig();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());

            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RPCConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> list = registry.serviceDiscover(serviceMetaInfo.getServiceKey());

            if ((CollUtil.isEmpty(list))){
                throw new RuntimeException("service is empty");
            }
            ServiceMetaInfo serviceMetaInfo1 = list.get(0);
            // 发送 TCP 请求
            RPCResponse rpcResponse = TcpClient.doRequest(request, serviceMetaInfo1);
            return rpcResponse.getData();
            /*Vertx vertx = Vertx.vertx();
            NetClient netClient = vertx.createNetClient();
            CompletableFuture<RPCResponse> responseFuture = new CompletableFuture<>();
            netClient.connect(serviceMetaInfo1.getServicePort(), serviceMetaInfo1.getServiceHost(),
                    result -> {
                        if (result.succeeded()) {
                            System.out.println("Connected to TCP server");
                            io.vertx.core.net.NetSocket socket = result.result();
                            // 发送数据
                            // 构造消息
                            ProtocolMessage<RPCRequest> protocolMessage = new ProtocolMessage<>();
                            ProtocolMessage.Header header = new ProtocolMessage.Header();
                            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                            header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                            header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RPCApplication.getRpcConfig().getSerializer()).getKey());
                            header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                            header.setRequestId(IdUtil.getSnowflakeNextId());
                            protocolMessage.setHeader(header);
                            protocolMessage.setBody(request);
                            // 编码请求
                            try {
                                Buffer encodeBuffer = ProtocolEncodeMessage.encode(protocolMessage);
                                socket.write(encodeBuffer);
                            } catch (IOException e) {
                                throw new RuntimeException("协议消息编码错误");
                            }

                            // 接收响应
                            socket.handler(buffer -> {
                                try {
                                    ProtocolMessage<RPCResponse> rpcResponseProtocolMessage = (ProtocolMessage<RPCResponse>) ProtocolDecodeMessage.decode(buffer);
                                    responseFuture.complete(rpcResponseProtocolMessage.getBody());
                                } catch (IOException e) {
                                    throw new RuntimeException("协议消息解码错误");
                                }
                            });
                        } else {
                            System.err.println("Failed to connect to TCP server");
                        }
                    });
            RPCResponse rpcResponse = responseFuture.get();
            // 记得关闭连接
            netClient.close();
            return rpcResponse.getData();*/
            /* HTTP
            HttpResponse httpResponse = HttpRequest.post(serviceMetaInfo1.getServiceAddress())
                    .body(bytes)
                    .execute();
            // rpc return a binary data
            byte[] result = httpResponse.bodyBytes();
            RPCResponse response = serializer.deserialize(result, RPCResponse.class);
            return response.getData();*/
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
