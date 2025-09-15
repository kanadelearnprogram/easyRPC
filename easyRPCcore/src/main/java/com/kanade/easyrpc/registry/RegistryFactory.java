package com.kanade.easyrpc.registry;

import com.kanade.easyrpc.spi.SpiLoader;

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
