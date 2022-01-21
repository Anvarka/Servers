package ru.itmo.java.server.unblocking;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class AbstractSelector implements Runnable {
    protected Selector selector;
    private final ConcurrentLinkedDeque<ClientInfo> newClients = new ConcurrentLinkedDeque<>();

    public AbstractSelector() {
        try {
            selector = Selector.open();
        } catch (IOException ignored) {
        }
    }

    abstract public int getOpType();

    public void addNew(ClientInfo clientInfo) {
        newClients.add(clientInfo);
        selector.wakeup();
    }

    public void addNewClients() {
        while (!newClients.isEmpty()) {
            try {
                ClientInfo clientInfo = newClients.removeFirst();
                SocketChannel socketChannel = clientInfo.getSocketChannel();
                socketChannel.register(selector, getOpType(), clientInfo);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    abstract public void run();
}
