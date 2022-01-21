package ru.itmo.java.server;

public abstract class AbstractServer {
    protected int countClients;
    public AbstractServer(int countClients) {
        this.countClients = countClients;
    }
    abstract public void run();
}
