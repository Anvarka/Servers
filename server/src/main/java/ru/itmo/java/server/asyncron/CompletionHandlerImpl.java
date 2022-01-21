package ru.itmo.java.server.asyncron;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import ru.itmo.java.message.TestResultFixer;
import ru.itmo.java.server.asyncron.Context;

public class CompletionHandlerImpl implements CompletionHandler<Integer, Context> {
    private final ExecutorService threadPool;
    private final AsynchronousSocketChannel asyncSocketChannel;
    private final CountDownLatch exitClient;
    private final TestResultFixer testResultFixer;



    public CompletionHandlerImpl(ExecutorService threadPool,
                                 AsynchronousSocketChannel asyncSocketChannel,
                                 CountDownLatch countDownLatch,
                                 CountDownLatch exitClient,
                                 TestResultFixer testResultFixer) {
        this.exitClient = exitClient;
        this.threadPool = threadPool;
        this.asyncSocketChannel = asyncSocketChannel;
        this.testResultFixer = testResultFixer;
        try {
            System.out.println("before wait");
            countDownLatch.await();
            System.out.println("after wait");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void completed(Integer result, Context context) {
        if (context.isReadyMessage()) {
            threadPool.submit(new Task(context, this, exitClient, testResultFixer, 0));
        } else {
            asyncSocketChannel.read(context.getReadBuffer(), context, this);
        }
    }

    @Override
    public void failed(Throwable exc, Context attachment) {
        System.out.println("error in failed");
    }
}
