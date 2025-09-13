package easyrpc.config;

import lombok.Data;

@Data
public class RPCConfig {
    private String name = "easyRPC";
    private String version = "1.0";
    private String serverHost = "localhost";
    private int serverPort = 8080;
}
