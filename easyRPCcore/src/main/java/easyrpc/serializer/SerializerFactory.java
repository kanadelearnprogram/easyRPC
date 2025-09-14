package easyrpc.serializer;

import easyrpc.spi.SpiLoader;

import java.util.HashMap;
import java.util.Map;

public class SerializerFactory {
    static {
        SpiLoader.load(Serializer.class);
    }
    private static final Map<String, Serializer> SERIALIZER_MAP = new HashMap<String, Serializer>() {{
        put(Serializers.JDK, new JDKSerializer());
        put(Serializers.JSON, new JSONSerializer());
        put(Serializers.KRYO, new KryoSerializer());
        put(Serializers.HESSIAN, new HessianSerializer());
    }};
    private static final Serializer DEFAULT_SERIALIZER = SERIALIZER_MAP.get("jdk");

    public static Serializer getInstance(String key){
        return SERIALIZER_MAP.getOrDefault(key,DEFAULT_SERIALIZER);
    }
}
