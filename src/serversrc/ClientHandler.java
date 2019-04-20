package serversrc;

import common.FSReply;
import common.FSRequest;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;


//handles a client connection
public class ClientHandler extends Thread {


    private Socket socket;
    private ObjectOutputStream streamOut;
    private ObjectInputStream streamIn;

    public ClientHandler(Socket connection) {
        this.socket = connection;
    }


    //thread begin
    @Override
    public void run() {
        super.run();

        try {
            //setup object streams for sending/receiving data
            streamOut = new ObjectOutputStream(socket.getOutputStream());
            streamIn = new ObjectInputStream(socket.getInputStream());
            streamOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //request handling loop
        while (true) {
            try {
                //wait for request
                Object obj = streamIn.readUnshared();
                if (obj != null) {
                    if (obj instanceof FSRequest) {
                        FSRequest request = (FSRequest) obj;

                        System.out.println(request.getType() + " received from client " + socket.getInetAddress());
                        //handle request
                        FSReply reply = handleRequest(request);

                        //respond to client with reply
                        streamOut.writeUnshared(reply);
                        //make sure it is sent right away
                        streamOut.flush();
                    } else {
                        System.out.println("Unexpected class recieved. Expected FSRequest.class, Was: " + obj.getClass());
                    }
                } else {
                    System.out.println("Object recieved was null.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Closing connection");
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Closing connection");
                break;
            }
        }
    }

    private FSReply handleRequest(FSRequest request) {

        //create reply message with the same type as the request
        FSReply reply = new FSReply(request.getType());

        //create reply object for the request type
        switch (request.getType()) {
            case READ:
                formReadReply(reply, request);
                break;
            case WRITE:
                formWriteReply(reply, request);
                break;
            case LOOKUP:
                formLookupReply(reply, request);
                break;
            case GETATTRIBUTE:
                formAttrReply(reply, request);
                break;
        }
        return reply;
    }

    //forms reply to send back to client
    private void formReadReply(FSReply reply, FSRequest request) {
        Date lastModified = new Date();

        byte[] data;
        //if length is -1 read whole file
        if(request.getLength()== -1) {
            data = new byte[(int) FileSystem.getFile(request.getFname()).length()];
        } else {
            data = new byte[request.getLength()];
        }
        try {
            //perform read operation and form the reply
            int bytesRead = FileSystem.readData(request.getFname(), request.getOffset(), request.getLength(), data, lastModified);
            reply.setData(data);
            reply.setBytesRead(bytesRead);
            reply.setLastModified(lastModified);
            reply.setSuccess(true);
        } catch (IOException e) {
            e.printStackTrace();
            //tell the client the read failed
            reply.setSuccess(false);
        }
    }

    //forms reply to send back to client
    private void formWriteReply(FSReply reply, FSRequest request) {
        Date lastModified = new Date();
        try {
            //perform write operation and form the reply
            FileSystem.writeData(request.getFname(), request.getOffset(), request.getData(), lastModified);
            reply.setLastModified(lastModified);
            reply.setSuccess(true);
        } catch (IOException e) {
            e.printStackTrace();
            //tell the client the write failed
            reply.setSuccess(false);
        }
    }

    //forms reply to send back to client
    private void formLookupReply(FSReply reply, FSRequest request) {
        Date lastModified = new Date();

        //get file
        File file = new File(request.getFname());

        //check if file exists
        if (file.exists()) {
            //set last modified time in reply
            lastModified.setTime(FileSystem.getAttribute(file));
            reply.setLastModified(lastModified);

            //set file length
            reply.setFileSize(file.length());

            //file exists
            reply.setSuccess(true);
        } else {
            //tell client file doesnt exist
            reply.setSuccess(false);
        }

    }

    //forms reply to send back to client
    private void formAttrReply(FSReply reply, FSRequest request) {
        Date lastModified = new Date();
        File file = FileSystem.getFile(request.getFname());
        if (file.exists()) {
            lastModified.setTime(file.lastModified());
            reply.setLastModified(lastModified);
            reply.setSuccess(true);
        } else {
            //file doesn't exist
            reply.setSuccess(false);
        }
    }

}
