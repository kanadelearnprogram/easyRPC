package easyrpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RPCResponse implements Serializable {

    /**
     * response data
     */
    private Object data;

    /**
     * response data type
     */
    private Class<?> dataType;

    /**
     * res message
     */
    private String message;

    /**
     * exception info
     */
    private Exception exception;

}
