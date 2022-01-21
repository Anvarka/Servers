package ru.itmo.java.server.blocking;

import ru.itmo.java.message.Constants;
import ru.itmo.java.message.TestResultFixer;
import ru.itmo.java.server.AbstractServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockingServer extends AbstractServer {
    private final ExecutorService readPool = Executors.newCachedThreadPool();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private final TestResultFixer testResultFixer = new TestResultFixer(Constants.deltaBlockTaskT, Constants.deltaBlockServerT);
    private CountDownLatch countDownLatch;
    private final CountDownLatch exitClient = new CountDownLatch(1);

    public BlockingServer(int countClients){
        super(countClients);
        countDownLatch = new CountDownLatch(countClients);
    }

    public static void main(String[] args) {
        BlockingServer server = new BlockingServer(Constants.COUNT_CLIENTS);
        server.run();
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            int clientId = 0;
            while (clientId < countClients) {
                Socket socket = serverSocket.accept();
                countDownLatch.countDown();
                readPool.submit(new Worker(socket, threadPool, testResultFixer, countDownLatch, exitClient));
                clientId++;
            }
            exitClient.await();
            testResultFixer.save();
            threadPool.shutdownNow();
            readPool.shutdownNow();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Connection error with client");
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            readPool.shutdownNow();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
