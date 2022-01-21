package ru.itmo.java.server.unblocking;

import ru.itmo.java.message.TestResultFixer;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReaderSelector extends AbstractSelector {
    public final WriterSelector writerSelector;
    public CountDownLatch countDownLatch;
    private final ExecutorService threadPool;
    private final TestResultFixer testResultFixer;

    public ReaderSelector(WriterSelector writerSelector, CountDownLatch countDownLatch, TestResultFixer testResultFixer) {
        super();
        this.countDownLatch = countDownLatch;
        threadPool = Executors.newFixedThreadPool(10);
        this.writerSelector = writerSelector;
        this.testResultFixer = testResultFixer;
    }

    @Override
    public int getOpType() {
        return SelectionKey.OP_READ;
    }

    @Override
    public void run() {
        try {
            countDownLatch.await();
            while (!Thread.interrupted()) {
                selector.select();
                Set<SelectionKey> readySet = selector.selectedKeys();
                Iterator<SelectionKey> it = readySet.iterator();
                while (it.hasNext()) {
                    process(it);
                    it.remove();
                }
                // add new clients
                addNewClients();
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdownNow();
        }
    }

    public void process(Iterator<SelectionKey> it) throws IOException {
        SelectionKey selectionKey = it.next();
        ClientInfo clientInfo = (ClientInfo) selectionKey.attachment();
        if (clientInfo.readFullMessage()) {
            List<Integer> arr = clientInfo.getArray();
            long startServerTime = System.currentTimeMillis();
            threadPool.submit(new Task(arr, clientInfo, writerSelector, testResultFixer, startServerTime));
        }
    }
}
