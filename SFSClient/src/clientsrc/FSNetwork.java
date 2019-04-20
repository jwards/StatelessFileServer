package clientsrc;

import common.FSReply;
import common.FSRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class FSNetwork {

    private final String HOSTNAME;
    private final int PORT;

    private Socket connection;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public FSNetwork(String hostname,int port){
        this.HOSTNAME = hostname;
        this.PORT = port;
    }

    //send request to server, wait for a reply, and then return the reply
    //synchronized for mutual exclusion on the socket
    public synchronized FSReply sendRequest(FSRequest request) throws IOException {

        if(connection.isClosed()){
            openConnection();
        }

        //write reply to socket
        out.writeUnshared(request);
        //send it to server
        out.flush();

        try {
            //wait for reply
            Object obj = in.readUnshared();

            if(obj != null){
                if(obj instanceof FSReply){
                    FSReply reply = (FSReply) obj;
                    //return the reply to the caller
                    return reply;
                } else {
                    System.out.println("Error in receiving reply. Expected object of type FSReply. Actual: "+obj.getClass());
                }
            } else {
                System.out.println("Error in receiving reply. Received null");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeConnection(){
        try {
            System.out.println("Closing connection to " + HOSTNAME + ":" + PORT);
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openConnection(){
        //setup socket connection to server

        //first check if connection is already open
        if(connection != null && !connection.isClosed()){
            return;
        }
        try {
            System.out.println("Opening connection to " + HOSTNAME + " : " + PORT);
            connection = new Socket(HOSTNAME, PORT);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
