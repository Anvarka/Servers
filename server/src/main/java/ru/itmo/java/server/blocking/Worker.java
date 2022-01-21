package ru.itmo.java.server.blocking;


import ru.itmo.java.message.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Worker implements Runnable {
    private final ExecutorService writePool = Executors.newSingleThreadExecutor();
    private final ExecutorService threadPool;
    private DataInputStream getterMessageFromClient;
    private DataOutputStream senderMessageToClient;
    private Socket socket;
    private TestResultFixer testResultFixer;
    private CountDownLatch countDownLatch;
    private CountDownLatch exitClient;
    private int countQueries = 0;

    public Worker(Socket socket, ExecutorService threadPool, TestResultFixer testResultFixer, CountDownLatch countDownLatch, CountDownLatch exitClient) {
        this.threadPool = threadPool;
        this.socket = socket;
        this.testResultFixer = testResultFixer;
        this.countDownLatch = countDownLatch;
        this.exitClient = exitClient;
        try {
            getterMessageFromClient = new DataInputStream(socket.getInputStream());
            senderMessageToClient = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            countDownLatch.await();
            while (true) {
                int messageSize = getterMessageFromClient.readInt();
                byte[] message = new byte[messageSize];
                getterMessageFromClient.read(message, 0, messageSize);
                List<Integer> arrList = new ArrayList<>(Request.parseFrom(message).getArrList());

                // передаем в pool
                long startServerTime = System.currentTimeMillis();
                threadPool.submit(() -> {
                    long startTaskTime = System.currentTimeMillis();
                    List<Integer> sortArr = ArrayUtils.makeSort(arrList);
                    testResultFixer.writeTaskTime(System.currentTimeMillis() - startTaskTime);

                    // отдельный поток для записи
                    executeWriteTask(() -> {
                        Response response = Response.newBuilder()
                                .addAllSortArr(sortArr)
                                .build();
                        senderMessageToClient.writeInt(response.getSerializedSize());
                        senderMessageToClient.write(response.toByteArray());
                        testResultFixer.writeServerTime(System.currentTimeMillis() - startServerTime);
                        countQueries++;
                        if (countQueries == Constants.COUNT_QUERIES) {
                            synchronized (exitClient) {
                                exitClient.countDown();
                            }
                            testResultFixer.stop();
                        }
                    });
                });

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            writePool.shutdownNow();
            try {
                getterMessageFromClient.close();
                senderMessageToClient.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void executeWriteTask(WriteTask task) {
        writePool.submit(() -> {
            try {
                task.run();
            } catch (IOException ignored) {
            }
        });
    }

}