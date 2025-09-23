package com.kanade.easyrpc.server.tcp;

import com.kanade.easyrpc.model.RPCRequest;
import com.kanade.easyrpc.model.RPCResponse;
import com.kanade.easyrpc.protocol.*;
import com.kanade.easyrpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

public class TcpServerHandler implements Handler<NetSocket> {
    @Override
    public void handle(NetSocket netSocket) {
// 处理连接
        netSocket.handler(buffer -> {
            // 接受请求，解码
            ProtocolMessage<RPCRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RPCRequest>) ProtocolDecodeMessage.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RPCRequest rpcRequest = protocolMessage.getBody();

            // 处理请求
            // 构造响应结果对象
            RPCResponse rpcResponse = new RPCResponse();
            try {
                // 获取要调用的服务实现类，通过反射调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 发送响应，编码
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            /*ProtocolMessage.Header header = new ProtocolMessage.Header();
            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
            header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
            header.setSerializer(protocolMessage.getHeader().getSerializer());
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            header.setState((byte) 20); // OK状态
            header.setRequestId(protocolMessage.getHeader().getRequestId());*/

            System.out.println("header "+header);
            System.out.println("res "+rpcResponse);
            ProtocolMessage<RPCResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolEncodeMessage.encode(responseProtocolMessage);
                netSocket.write(encode);
            } catch (IOException e) {
                throw new RuntimeException("协议消息编码错误");
            }
        });
    }
}
