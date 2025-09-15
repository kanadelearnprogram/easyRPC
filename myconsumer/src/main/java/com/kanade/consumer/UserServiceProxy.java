package com.kanade.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.kanade.common.model.User;
import com.kanade.common.service.UserService;
import com.kanade.easyrpc.model.RPCRequest;
import com.kanade.easyrpc.model.RPCResponse;
import com.kanade.easyrpc.serializer.JDKSerializer;
import com.kanade.easyrpc.serializer.Serializer;


import java.io.IOException;

public class UserServiceProxy implements UserService {
    // 请求对象序列化 -> 发送到RPC服务 -> 获取响应 -> 反序列化
    @Override
    public User getUser(User user) {
        Serializer serializer = new JDKSerializer();

        RPCRequest request = RPCRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();
        try{
            byte[] bytes = serializer.serialize(request);
            HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bytes)
                    .execute();
            // rpc return a binary data
            byte[] result = httpResponse.bodyBytes();
            RPCResponse response = serializer.deserialize(result, RPCResponse.class);
            return (User) response.getData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
