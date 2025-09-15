package com.kanade.easyrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import com.kanade.easyrpc.RPCApplication;
import com.kanade.easyrpc.config.RPCConfig;
import com.kanade.easyrpc.constant.RPCConstant;
import com.kanade.easyrpc.model.RPCRequest;
import com.kanade.easyrpc.model.RPCResponse;
import com.kanade.easyrpc.model.ServiceMetaInfo;
import com.kanade.easyrpc.registry.Registry;
import com.kanade.easyrpc.registry.RegistryFactory;
import com.kanade.easyrpc.serializer.Serializer;
import com.kanade.easyrpc.serializer.SerializerFactory;


import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

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

            HttpResponse httpResponse = HttpRequest.post(serviceMetaInfo1.getServiceAddress())
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
