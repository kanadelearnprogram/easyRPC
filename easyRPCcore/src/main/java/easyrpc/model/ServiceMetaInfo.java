package easyrpc.model;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

@Data
public class ServiceMetaInfo {
    private String serviceName;
    private String serviceVersion;
    private String serviceHost;
    private int servicePort;
    public String getServiceKey(){
        return String.format("%s %s",serviceName,serviceVersion);
    }
    public String getServiceNode(){
        return String.format("%s/%s:%s",getServiceKey(),serviceHost,servicePort);
    }
    /**
     * 获取完整服务地址
     *
     * @return
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }

}
