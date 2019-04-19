package serversrc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {

    private final int PORT;

    private ServerSocket socket;

    public ServerThread(int port){
        this.PORT = port;
    }


    @Override
    public void run() {
        super.run();

        //setup socket
        try {
            socket = new ServerSocket(PORT);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't open server port. Aborting");
            return;
        }


        //listen for client connections and spawn new threads to handle them
        while(true){
            try {
                Socket incomingClient = socket.accept();
                System.out.println("Client connected: " + incomingClient.toString());
                //create thread to handle new connection
                ClientHandler ch = new ClientHandler(incomingClient);
                //run thread
                ch.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
