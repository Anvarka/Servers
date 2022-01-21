package ru.itmo.java.server.unblocking;

import ru.itmo.java.message.Constants;
import ru.itmo.java.message.TestResultFixer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class WriterSelector extends AbstractSelector {
    private CountDownLatch exitClients;
    private TestResultFixer testResultFixer;

    public WriterSelector(CountDownLatch exitClients, TestResultFixer testResultFixer) {
        super();
        this.exitClients = exitClients;
        this.testResultFixer = testResultFixer;
    }

    @Override
    public int getOpType() {
        return SelectionKey.OP_WRITE;
    }

    @Override
    public void run() {
        try {
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void process(Iterator<SelectionKey> it) throws IOException {
        SelectionKey selectionKey = it.next();
        ClientInfo clientInfo = (ClientInfo) selectionKey.attachment();
        if (clientInfo.writeMessage()) {
            synchronized (clientInfo.queueReadyMessage) {
                Snapshot snapshot = clientInfo.queueReadyMessage.removeFirst();
                testResultFixer.writeServerTime(System.currentTimeMillis() - snapshot.getStartTime());
                if (clientInfo.queueReadyMessage.isEmpty()) {
                    selectionKey.cancel();
                }
            }
            clientInfo.countQueries++;
            if (clientInfo.countQueries == Constants.COUNT_QUERIES) {
                testResultFixer.stop();
                exitClients.countDown();
            }
        }
    }
}
