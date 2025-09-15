package com.kanade.easyrpc.config;

import com.kanade.easyrpc.serializer.Serializers;
import lombok.Data;

@Data
public class RPCConfig {
    private String name = "easyRPC";
    private String version = "1.0";
    private String serverHost = "localhost";
    private int serverPort = 8080;
    private boolean mock = false;
    private String serializer = Serializers.JDK;
    private RegistryConfig registryConfig = new RegistryConfig();
}
