package serversrc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread{


    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket connection){
        this.socket = connection;
    }


    @Override
    public void run() {
        super.run();

        try {
            //setup object streams for sending/receiving data
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());








        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
