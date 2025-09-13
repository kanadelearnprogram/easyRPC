package easyrpc;

import easyrpc.config.RPCConfig;
import easyrpc.constant.RPCConstant;
import easyrpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RPCApplication {
    private static volatile RPCConfig rpcConfig;

    public static void init(RPCConfig rpcConfig1){
        rpcConfig = rpcConfig1;
        log.info("rpc init {}",rpcConfig1.toString());
    }

    public static void init(){
        RPCConfig rpcConfig1;
        rpcConfig1 = ConfigUtils.loadConfig(RPCConfig.class, RPCConstant.DEFAULT_CONFIG_PREFIX);
        init(rpcConfig1);
    }
    public static RPCConfig getRpcConfig(){
        if (rpcConfig == null){ // 提高性能 已经创建了直接返回
            // 在多线程下 保证只有一个线程能执行初始化代码
            synchronized (RPCApplication.class){
                if (rpcConfig == null){ //防止多个线程重复初始化
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
