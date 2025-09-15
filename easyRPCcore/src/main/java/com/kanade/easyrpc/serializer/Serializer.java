package com.kanade.easyrpc.serializer;

import java.io.IOException;

public interface Serializer {
    /**
     * serializable
     */
    <T> byte[] serialize(T data) throws IOException;

    /**
     * deserializable
     */
     <T> T deserialize(byte[] bytes,Class<T> type) throws IOException;
}
