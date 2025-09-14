package easyrpc.model;

import easyrpc.constant.RPCConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RPCRequest implements Serializable {



    /**
     * service name
     */
    private String serviceName;

    /**
     * method
     */
    private String methodName;

    /**
     * parameter type
     */
    private Class<?>[] parameterTypes;

    /**
     * argument
     */
    private Object[] args;

    private String serviceVersion = RPCConstant.DEFAULT_SERVICE_VERSION;

}
