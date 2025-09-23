package com.kanade.easyrpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProtocolMessage<T> {
    private Header header;
    private T body;
@Data
public static
class Header{
        private byte magic;

        private byte version;

        private byte serializer;
        private byte type;
        private byte state;
        private long requestId;
        private int bodyLength;

    }
}
