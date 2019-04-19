

/* This is a simple example implementation of fileSystemAPI, 
   using local file system calls. 
*/

/* standard java classes. */
import java.io.*;
import java.util.*;

public class fileSystem implements fileSystemAPI
{ 
    /* It needs a table relating filehandles and real files. */
    Hashtable tbl = new Hashtable();

    /* url SHOULD HAVE form IP:port/path, but here simply a file name.*/
    
    public filehandle open(String url)
	throws java.io.FileNotFoundException 
    {
		FileInputStream in = new FileInputStream(new File(url));
		filehandle fh = new filehandle(); 
        tbl.put(fh, in);
	return fh;
    }
	
    /* write is not implemented. */
    public boolean write(filehandle fh, byte[] data)
	throws java.io.IOException
    {
		return true;
    }

    /* read bytes from the current position. returns the number of bytes read. */
    public int read(filehandle fh, byte[] data)
	throws java.io.IOException
    {
		FileInputStream in = (FileInputStream) tbl.get(fh); 
		int res = in.read(data);
		return res;
    }

    /* close file. */  
    public boolean close(filehandle fh)
	throws java.io.IOException
    {
		((FileInputStream) tbl.get(fh)).close(); 	
		tbl.remove(fh);
		fh.discard();
		return true;
    }

    /* check if it is the end-of-file. */
    public boolean isEOF(filehandle fh)
	throws java.io.IOException
    {
		byte[] dummy={0};
		return (((FileInputStream) tbl.get(fh)).available()==0);
    }
} 
    
	
