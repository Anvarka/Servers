package ru.itmo.java.server.unblocking;

import ru.itmo.java.message.Request;
import ru.itmo.java.message.Response;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClientInfo {
    private final SocketChannel socketChannel;
    // буфер на чтение
    private ByteBuffer bufferReader;
    // буфер на запись
    private ByteBuffer bufferWriter;
    public final ConcurrentLinkedDeque<Snapshot> queueReadyMessage = new ConcurrentLinkedDeque<>();
    public int countQueries = 0;


    private final InetAddress inetAddress;
    private final int port;
    private final ByteBuffer size = ByteBuffer.allocate(4);
    private Request request;

    private final ByteBuffer writeSize = ByteBuffer.allocate(4);
    private Response response;

    public ClientInfo(SocketChannel socketChannel, InetAddress inetAddress, int port) {
        this.socketChannel = socketChannel;
        this.inetAddress = inetAddress;
        this.port = port;
    }

    public boolean readFullMessage() throws IOException {
        if (bufferReader == null) {
            socketChannel.read(size);
            if (size.hasRemaining()) {
                return false;
            }
            size.flip();
            int messageSize = size.getInt();
            bufferReader = ByteBuffer.allocate(messageSize);
            size.clear();
        }
        socketChannel.read(bufferReader);
        if (bufferReader.hasRemaining()) {
            return false;
        }
        bufferReader.flip();
        request = Request.parseFrom(bufferReader.array());
        bufferReader = null;
        return true;
    }

    public boolean writeMessage() throws IOException {
        if (response == null) {
            Snapshot snapshot = queueReadyMessage.getFirst();
            List<Integer> sortArr = snapshot.getTasks();
            response = Response.newBuilder().addAllSortArr(sortArr).build();
            writeSize.putInt(response.getSerializedSize());
            writeSize.flip();
        }
        if (bufferWriter == null) {
            socketChannel.write(writeSize);
            if (writeSize.hasRemaining()) {
                return false;
            }
            bufferWriter = ByteBuffer.allocate(response.getSerializedSize());
            bufferWriter.put(response.toByteArray());
            bufferWriter.flip();
            writeSize.clear();
        }
        socketChannel.write(bufferWriter);
        if(bufferWriter.hasRemaining()){
            return false;
        }
        bufferWriter = null;
        response = null;
        return true;
    }

    public List<Integer> getArray() {
        return new ArrayList<>(request.getArrList());
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return port;
    }

}
