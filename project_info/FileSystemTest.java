import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

public class FileSystemTest {

	private final static String localhost = "localhost";
	private final static int portNum = 7777;

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
		 
		fileSystemAPI fs; 
		
		String url = "127.0.0.1:7777/" + fileName;
		filehandle fh = fs.open(url);
		
		System.out.println("Start reading file...");
		while (!fs.isEOF(fh)) {

			// read data.
			res = fs.read(fh, data);
				
		}
		System.out.println("Done reading file...");	
		
		
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
