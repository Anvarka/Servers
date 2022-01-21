package ru.itmo.java.server.unblocking;

import java.util.List;

public class Snapshot {
    private List<Integer> tasks;
    private long startTime;
    public Snapshot(List<Integer> tasks, long startTime){
        this.tasks =tasks;
        this.startTime = startTime;
    }

    public List<Integer> getTasks() {
        return tasks;
    }

    public long getStartTime() {
        return startTime;
    }
}
