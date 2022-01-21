package ru.itmo.java.server.asyncron;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.itmo.java.message.Request;
import ru.itmo.java.message.Response;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;

public class Context {
    private AsynchronousSocketChannel asyncSocketChannel;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private ByteBuffer writeBuffer;
    private int messageSize;
    private byte[] readyMessage;
    private List<Integer> arr;
    public int countQueries  = 0;

    public Context(AsynchronousSocketChannel asyncSocketChannel) {
        this.asyncSocketChannel = asyncSocketChannel;
    }

    public ByteBuffer getReadBuffer() {
        return buffer;
    }

    public boolean isReadyMessage() {
        buffer.flip();
        if (readyMessage == null) {

            if (buffer.remaining() >= 4) {
                messageSize = buffer.getInt();
                readyMessage = new byte[messageSize];
            }
        }

        if (buffer.remaining() < messageSize) {
            buffer.compact();
            return false;
        }

        buffer.get(readyMessage);
        try {
            arr = new ArrayList<>(Request.parseFrom(readyMessage).getArrList());
            readyMessage = null;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        buffer.compact();
        return true;
    }

    public List<Integer> getArray() {
        return arr;
    }

    public AsynchronousSocketChannel getAsyncSocketChannel() {
        return asyncSocketChannel;
    }

    public ByteBuffer getWriteBuffer() {
        return writeBuffer;
    }

    public void addSortArray(List<Integer> sortArr) {
        Response response = Response.newBuilder().addAllSortArr(sortArr).build();
        writeBuffer = ByteBuffer.allocate(4 + response.getSerializedSize());
        writeBuffer.putInt(response.getSerializedSize());
        writeBuffer.put(response.toByteArray());
        writeBuffer.flip();
    }

}
