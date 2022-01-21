package ru.itmo.java.server.unblocking;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReaderClientBuffer {
    private Map<ClientInfo, ByteBuffer> map =  new ConcurrentHashMap<>();

    public Map<ClientInfo, ByteBuffer> getMap() {
        return map;
    }
}
