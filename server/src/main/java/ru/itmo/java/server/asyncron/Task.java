package ru.itmo.java.server.asyncron;

import ru.itmo.java.message.ArrayUtils;
import ru.itmo.java.message.Constants;
import ru.itmo.java.message.TestResultFixer;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Task implements Runnable {
    private final Context context;
    private final CompletionHandler<Integer, Context> completionHandler;
    private final CountDownLatch exitClient;
    private final TestResultFixer testResultFixer;
    private long startServerTime;


    public Task(Context context, CompletionHandler<Integer, Context> completionHandler,
                CountDownLatch exitClient, TestResultFixer testResultFixer, long startServerTime) {
        this.context = context;
        this.completionHandler = completionHandler;
        this.exitClient = exitClient;
        this.testResultFixer = testResultFixer;
        this.startServerTime= startServerTime;
    }

    @Override
    public void run() {
//        System.out.println("context.getArray() " + context.getArray().size());
        long startTaskTime = System.currentTimeMillis();
        List<Integer> sortArr = ArrayUtils.makeSort(context.getArray());
        context.addSortArray(sortArr);
        testResultFixer.writeTaskTime(System.currentTimeMillis() - startTaskTime);
        AsynchronousSocketChannel asynchronousSocketChannel = context.getAsyncSocketChannel();
        asynchronousSocketChannel.write(context.getWriteBuffer(), context, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, Context attachment) {
                if (context.getWriteBuffer().hasRemaining()) {
                    asynchronousSocketChannel.write(context.getWriteBuffer(), context, this);
                } else {
                    testResultFixer.writeServerTime(System.currentTimeMillis() - startServerTime);
                    context.countQueries++;
                    if (context.countQueries == Constants.COUNT_QUERIES) {
                        System.out.println("countDown");
                        synchronized (exitClient) {
                            exitClient.countDown();
                        }
                        testResultFixer.stop();
                    }

                    asynchronousSocketChannel.read(context.getReadBuffer(), context, completionHandler);
                }
            }

            @Override
            public void failed(Throwable exc, Context attachment) {
                System.out.println("error in Task");
            }
        });
    }
}
