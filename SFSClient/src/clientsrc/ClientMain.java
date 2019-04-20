package clientsrc;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientMain {


    private final static String localhost = "127.0.0.1";
    private final static int portNum = 10010;

    public static void main(String[] args) throws UnknownHostException, IOException {

        int res;
        byte[] data = new byte[100];



        System.out.println("Defalt IP: localhost; Default Server PortNum: 7777");
        Scanner scan = new Scanner(System.in);

        System.out.println("Please specify filename to Open: ");
        String fileName = scan.nextLine();

		/* Initialise the client-side file system. The following line should be replaced by something like:
	     fileSystemAPI fs = new YourClientSideFileSystem() in your version.
		 */

        fileSystemAPI fs = new ClientFileSystem();

        String url = localhost+":"+portNum+"/" + fileName;
        FileHandle fh = fs.open(url);

        long time = System.currentTimeMillis();
        System.out.println("Start reading file...");
        while (!fs.isEOF(fh)) {

            // read data.
            res = fs.read(fh, data);
            //show data that was read
            System.out.print(new String(data,"UTF-8"));
        }
        System.out.println("Done reading file in..." + ((System.currentTimeMillis() - time))+"ms" );

        System.out.println("Please enter data to write to file or 'q' to stop");
        String contents = scan.nextLine();

        while (!(contents.equals("q"))) {
            contents = contents + "\n";
            byte[] toWrite = contents.getBytes();
            boolean write_res = fs.write(fh, toWrite);
            System.out.println("Data is written to file");
            System.out.println("Please enter data to write to file or 'q' to stop");
            contents = scan.nextLine();
        }

        fs.close(fh);
        return;

    }
}
