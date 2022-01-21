package ru.itmo.java.server.blocking;

import java.io.IOException;

public interface WriteTask {
    void run() throws IOException;
}
