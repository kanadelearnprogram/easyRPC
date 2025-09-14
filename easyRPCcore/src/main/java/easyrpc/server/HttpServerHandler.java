package easyrpc.server;



import easyrpc.RPCApplication;
import easyrpc.model.RPCRequest;
import easyrpc.model.RPCResponse;
import easyrpc.registry.LocalRegistry;
import easyrpc.serializer.JDKSerializer;
import easyrpc.serializer.Serializer;
import easyrpc.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Method;

public class HttpServerHandler implements Handler<HttpServerRequest> {
    /**
     * 反序列化 将byte流转换为对象并获取参数
     * 根据携带的服务名称 获取服务实现类
     * 调用方法 序列化并进行响应
     *
     * @param httpServerRequest
     */
    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        Serializer serializer = SerializerFactory.getInstance(RPCApplication.getRpcConfig().getSerializer());
        System.out.println(httpServerRequest);
        httpServerRequest.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RPCRequest rpcRequest = null;

            try {
                rpcRequest = serializer.deserialize(bytes, RPCRequest.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 构造响应结果对象
            RPCResponse rpcResponse = new RPCResponse();
            // 如果请求为 null，直接返回
            if (rpcRequest == null) {
                rpcResponse.setMessage("rpcRequest is null");
                doResponse(httpServerRequest, rpcResponse, serializer);
                return;
            }
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
            // 响应
            doResponse(httpServerRequest, rpcResponse, serializer);
        });

    }
    void doResponse(HttpServerRequest request,RPCResponse response, Serializer serializer){
        HttpServerResponse httpServerResponse = request.response()
                .putHeader("content-type", "application/json");
        try {
            // 序列化
            byte[] serialized = serializer.serialize(response);
            httpServerResponse.end(Buffer.buffer(serialized));
        } catch (IOException e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
