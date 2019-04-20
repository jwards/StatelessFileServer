package serversrc;


public class FileServerMain {


    public static final int PORT = 10010;


    public static void main(String[] args){
        ServerThread serverThread = new ServerThread(PORT);

        //start server
        serverThread.start();
    }

}

