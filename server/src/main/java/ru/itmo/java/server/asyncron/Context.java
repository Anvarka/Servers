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
    private ByteBuffer buffer;
    private ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
    private ByteBuffer writeBuffer;
    private int messageSize;
    private byte[] readyMessage;
    private List<Integer> arr;
    public int countQueries = 0;

    public Context(AsynchronousSocketChannel asyncSocketChannel) {
        this.asyncSocketChannel = asyncSocketChannel;
    }

    public ByteBuffer getReadBuffer() {
        if (buffer == null) {
            return sizeBuffer;
        }
        return buffer;
    }

    public boolean isReadyMessage() {
        if (buffer == null) {
            if (sizeBuffer.hasRemaining()) {
                return false;
            }
            sizeBuffer.flip();
            messageSize = sizeBuffer.getInt();
            buffer = ByteBuffer.allocate(messageSize);
            sizeBuffer.clear();
            return false;
        }

//        System.out.println("messageSize " + messageSize + " buffer.remaining(): " + buffer.remaining());
        if (buffer.hasRemaining()) {
            return false;
        }
        buffer.flip();

        try {
            arr = new ArrayList<>(Request.parseFrom(buffer.array()).getArrList());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        buffer = null;
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
