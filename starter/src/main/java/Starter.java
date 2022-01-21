import ru.itmo.java.message.Constants;
import ru.itmo.java.server.AbstractServer;
import ru.itmo.java.server.asyncron.AsyncronousServer;
import ru.itmo.java.server.blocking.BlockingServer;
import ru.itmo.java.server.unblocking.UnblockingServer;
import ru.itmo.java.client.Client;
import java.io.*;


public class Starter {
    public static void main(String[] args) throws InterruptedException {

        int serverType = Integer.parseInt(args[0]);
        int countQueries = Integer.parseInt(args[1]);
        Constants.COUNT_QUERIES = countQueries;


        // params
        int countClient = Integer.parseInt(args[2]);
        int delta = Integer.parseInt(args[3]);
        int countArr = Integer.parseInt(args[4]);
        // unfixed
        int unfixedParam = Integer.parseInt(args[5]);

        Integer startLimit = Integer.parseInt(args[6]);
        Integer endLimit = Integer.parseInt(args[7]);
        Integer step = Integer.parseInt(args[8]);
        Thread serverThread;
        Thread[] clientThreads;

        AbstractServer server;
        int v = 0;
        for (int param = startLimit; param <= endLimit; param += step) {
            switch (unfixedParam) {
                case 1: {
                    countClient = param;
                    break;
                }
                case 2: {
                    delta = param;
                    break;
                }
                case 3: {
                    countArr = param;
                    break;
                }

            }
            System.out.println("Create server");
            switch (serverType) {
                case 1: {
                    server = new BlockingServer(countClient);
                    break;
                }
                case 2: {
                    server = new UnblockingServer(countClient);
                    break;
                }
                case 3: {
                    server = new AsyncronousServer(countClient);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Error");
            }
            AbstractServer finalServer = server;
            serverThread = new Thread(finalServer::run);
            serverThread.start();
//            Thread.sleep(1000);
            clientThreads = new Thread[countClient];
            Client[] clients = new Client[countClient];
            for (int j = 0; j < countClient; j++) {
                Client client = new Client(countQueries, delta, countArr, j);
                clients[j] = client;
                clientThreads[j] = new Thread(client::run);
                clientThreads[j].start();
            }
            System.out.println("join");
            serverThread.join();
            System.out.println("end join " + serverThread.isAlive());
            for (int i = 0; i < clientThreads.length; i++) {
                System.out.println("begin i in for " + i);
                clientThreads[i].interrupt();
                clients[i].stop();
                clientThreads[i].join();
                System.out.println("i in for " + i);
            }
            System.out.println("client interrupt");
            System.out.println("---");
        }
        System.out.println("END");

        try {
            FileWriter writer = new FileWriter("file1", true);
            writer.write("\n");
            writer.write(startLimit.toString() + " ");
            writer.write(endLimit.toString() + " ");
            writer.write(step.toString());
            writer.flush();
            writer.close();

            writer = new FileWriter("file2", true);
            writer.write("\n");
            writer.write(startLimit.toString() + " ");
            writer.write(endLimit.toString() + " ");
            writer.write(step.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
