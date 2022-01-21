package ru.itmo.java.message;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class TestResultFixer {
    long serverT = 0;
    long taskT = 0;
    float countTask = 0;
    float countServer = 0;
    private AtomicBoolean stopTest = new AtomicBoolean(false);
    private FileWriter fileTask;
    private FileWriter fileServer;

    public TestResultFixer(String filename1, String filename2) {
        try {
            fileTask = new FileWriter(filename1, true);
            fileServer = new FileWriter(filename2, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void writeTaskTime(long time) {
        if (!stopTest.get()) {
            countTask += 1;
            taskT += time;
        }
    }

    public synchronized void writeServerTime(long time) {
        if (!stopTest.get()) {
            countServer += 1;
            serverT += time;
        }
    }

    public synchronized void stop() {
        stopTest.set(true);
    }

    public float getAverageTask() {
        return taskT / countTask;
    }

    public float getAverageServer() {
        return serverT / countServer;
    }

    public synchronized void save() throws IOException {
        System.out.println("in save file " + getAverageServer());
        System.out.println("in save file " + getAverageTask());

        Float f = getAverageTask();
        Float g = getAverageServer();
        fileTask.write(f.toString() + " ");
        fileServer.write(g.toString() + " ");
        fileTask.flush();
        fileServer.flush();
        fileTask.close();
        fileServer.close();
    }
}
