package ru.itmo.java.server.unblocking;

import ru.itmo.java.message.ArrayUtils;
import ru.itmo.java.message.TestResultFixer;

import java.util.List;

public class Task implements Runnable {
    private final List<Integer> arr;
    private final ClientInfo clientInfo;
    private final WriterSelector writerSelector;
    private TestResultFixer testResultFixer;
    private long startServerTime;

    public Task(List<Integer> arr, ClientInfo clientInfo, WriterSelector writerSelector, TestResultFixer testResultFixer, long startServerTime) {
        this.arr = arr;
        this.clientInfo = clientInfo;
        this.writerSelector = writerSelector;
        this.testResultFixer = testResultFixer;
        this.startServerTime = startServerTime;
    }

    @Override
    public void run() {
        long startTaskTime = System.currentTimeMillis();
        List<Integer> sortArr = ArrayUtils.makeSort(arr);
        testResultFixer.writeTaskTime(System.currentTimeMillis() - startTaskTime);
        synchronized (clientInfo.queueReadyMessage) {
            clientInfo.queueReadyMessage.addLast(new Snapshot(sortArr, startServerTime));
            if (clientInfo.queueReadyMessage.size() == 1) {
                writerSelector.addNew(clientInfo);
            }
        }
    }
}
