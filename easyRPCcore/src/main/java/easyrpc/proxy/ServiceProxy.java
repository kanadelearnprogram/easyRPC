package easyrpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import easyrpc.model.RPCRequest;
import easyrpc.model.RPCResponse;
import easyrpc.serializer.JDKSerializer;
import easyrpc.serializer.Serializer;


import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Serializer serializer = new JDKSerializer();

        RPCRequest request = RPCRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try{
            byte[] bytes = serializer.serialize(request);
            // 需要注册中心
            HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bytes)
                    .execute();
            // rpc return a binary data
            byte[] result = httpResponse.bodyBytes();
            RPCResponse response = serializer.deserialize(result, RPCResponse.class);
            return response.getData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
