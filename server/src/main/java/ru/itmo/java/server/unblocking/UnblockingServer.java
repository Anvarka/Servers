package ru.itmo.java.server.unblocking;

import ru.itmo.java.message.Constants;
import ru.itmo.java.message.TestResultFixer;
import ru.itmo.java.server.AbstractServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

public class UnblockingServer extends AbstractServer {
    private final WriterSelector writerSelector;
    private final ReaderSelector readerSelector;
    private CountDownLatch countDownLatch;
    private final CountDownLatch exitClient = new CountDownLatch(1);
    private TestResultFixer testResultFixer = new TestResultFixer(Constants.deltaUnBlockTaskT, Constants.deltaUnBlockServerT);
    private final Thread threadReader;
    private final Thread threadWriter;

    public UnblockingServer(int countClients) {
        super(countClients);
        countDownLatch = new CountDownLatch(countClients);
        writerSelector = new WriterSelector(exitClient, testResultFixer);
        readerSelector = new ReaderSelector(writerSelector, countDownLatch, testResultFixer);
        threadReader = new Thread(readerSelector);
        threadReader.start();
        threadWriter = new Thread(writerSelector);
        threadWriter.start();
    }

    public static void main(String[] args) {
        UnblockingServer server = new UnblockingServer(Constants.COUNT_CLIENTS);
        server.run();
    }
    @Override
    public void run() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(Constants.SERVER_PORT));
            int clientId = 0;
            while (clientId < countClients) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                countDownLatch.countDown();
                socketChannel.configureBlocking(false);
                ClientInfo clientInfo = getClientInfo(socketChannel);
                readerSelector.addNew(clientInfo);
                clientId++;
            }
            exitClient.await();
            testResultFixer.save();
            threadReader.interrupt();
            threadWriter.interrupt();
            serverSocketChannel.close();
        } catch (IOException | InterruptedException e) {
            System.out.println("Not connection with client");
            e.printStackTrace();
        }
    }

    public ClientInfo getClientInfo(SocketChannel socketChannel) {
        InetAddress inetAddress = socketChannel.socket().getInetAddress();
        int port = socketChannel.socket().getPort();
        return new ClientInfo(socketChannel, inetAddress, port);
    }
}
