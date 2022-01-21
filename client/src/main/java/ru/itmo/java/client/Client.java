package ru.itmo.java.client;

import ru.itmo.java.message.ArrayUtils;
import ru.itmo.java.message.Constants;
import ru.itmo.java.message.Request;
import ru.itmo.java.message.Response;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class Client {
    private final int countQueries;
    private final int timeBetweenMessages;
    private int countElements;
    private int id;
    private DataOutputStream senderToServer;
    private DataInputStream getterFromServer;

    public Client(int countQueries, int timeBetweenMessages, int countElements, int id) {
        this.countQueries = countQueries;
        this.timeBetweenMessages = timeBetweenMessages;
        this.countElements = countElements;
        this.id = id;
    }

    public static void main(String[] args) {
        Client client = new Client(Constants.COUNT_QUERIES, Constants.WAIT_BETWEEN_MESSAGES, Constants.COUNT_ELEMENTS_IN_ARRAY, 2);
        client.run();
    }

    public void run() {
        try {
            Socket socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
            senderToServer = new DataOutputStream(socket.getOutputStream());
            getterFromServer = new DataInputStream(socket.getInputStream());
            int query_id = 0;
            while (query_id < countQueries) {
                if (Thread.interrupted()) {
                    break;
                }
                // create random array
                List<Integer> arr = ArrayUtils.createRandomArray(countElements);
                // send message to server
                Request messageToServer = Request.newBuilder().addAllArr(arr).build();
//                messageToServer.writeDelimitedTo(socket.getOutputStream());
                senderToServer.writeInt(messageToServer.getSerializedSize());
                senderToServer.write(messageToServer.toByteArray());
                senderToServer.flush();
//                 get message from server
//                Response messageFromServer = Response.parseDelimitedFrom(socket.getInputStream());
                int messageSize = getterFromServer.readInt();
                byte[] messageFromServer = new byte[messageSize];

                getterFromServer.read(messageFromServer, 0, messageSize);
                List<Integer> sortArr = Response.parseFrom(messageFromServer).getSortArrList();
                query_id += 1;
                Thread.sleep(timeBetweenMessages);
            }
        } catch (IOException e) {
            System.out.println("Not connection with Server " + id);
        } catch (InterruptedException e){
            System.out.println("Interrupt from catch " + id);
        }
    }

    public void stop() {
        try {
            senderToServer.close();
            getterFromServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
