package ru.itmo.java.server.asyncron;

import ru.itmo.java.message.Constants;
import ru.itmo.java.message.TestResultFixer;
import ru.itmo.java.server.AbstractServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.*;

public class AsyncronousServer extends AbstractServer {
    private ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private CountDownLatch countDownLatch = new CountDownLatch(countClients);
    private final CountDownLatch exitClient = new CountDownLatch(1);
    private final TestResultFixer testResultFixer = new TestResultFixer(Constants.countClientAsyncTaskT, Constants.countClientAsyncServerT);
    public AsyncronousServer(int countClients) {
        super(countClients);
    }

    public static void main(String[] args) {
        AsyncronousServer server = new AsyncronousServer(Constants.COUNT_CLIENTS);
        server.run();
    }

    public void run() {
        try {
            AsynchronousServerSocketChannel asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
            asynchronousServerSocketChannel.bind(new InetSocketAddress(Constants.SERVER_PORT));

            int clientId = 0;
            while (clientId < countClients) {
                Future<AsynchronousSocketChannel> future = asynchronousServerSocketChannel.accept();
                AsynchronousSocketChannel asyncSocketChannel = future.get();
                countDownLatch.countDown();

                Context context = new Context(asyncSocketChannel);
                asyncSocketChannel.read(context.getReadBuffer(), context,
                        new CompletionHandler<>() {
                            @Override
                            public void completed(Integer result, Context attachment) {
                                try {
                                    countDownLatch.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (context.isReadyMessage()) {
                                    long startServerTime = System.currentTimeMillis();
                                    threadPool.submit(new Task(context, this, exitClient, testResultFixer, startServerTime));
                                } else {
                                    asyncSocketChannel.read(context.getReadBuffer(), context, this);
                                }
                            }

                            @Override
                            public void failed(Throwable exc, Context attachment) {
                                System.out.println("error in failed");
                            }
                        }
                );
                clientId++;
            }
            exitClient.await();
            System.out.println("--------------EXIT-----------");
            testResultFixer.save();
            threadPool.shutdownNow();
            asynchronousServerSocketChannel.close();
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
