package easyrpc.registry;

import easyrpc.serializer.*;
import easyrpc.spi.SpiLoader;

import java.util.HashMap;
import java.util.Map;

public class RegistryFactory {

        static {
            SpiLoader.load(Registry.class);
        }

        private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

        public static Registry getInstance(String key){
            System.out.println(key);
            return SpiLoader.getInstance(Registry.class,key);
        }

}
