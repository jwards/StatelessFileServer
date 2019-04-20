package serversrc;

import com.sun.istack.internal.NotNull;

import java.io.*;
import java.util.Date;

public class FileSystem {

    //all methods are static since there is no state associated with them
    //synchronized to provide mutual exclusion when reading and writing to files

    //for ease of use
    public static synchronized int readData(String filename,int offset,int length,byte[] data,Date lastModified) throws IOException {
        return readData(getFile(filename),offset,length,data,lastModified);
    }

    public static synchronized int readData(File file, int offset, int length, byte[] data,Date lastModified) throws IOException {
        //Create file input stream to read bytes from file
        FileInputStream fin = new FileInputStream(file);
        //get last modified time
        lastModified.setTime(getAttribute(file));

        long fileLen = file.length();

        fin.skip(offset);
        int result = fin.read(data, 0, length);

        fin.close();
        return result;
    }
    public static synchronized void writeData(String filename,int offset,byte[] data,Date lastModified) throws IOException {
        writeData(getFile(filename),offset,data,lastModified);
    }
    public static synchronized void writeData(File file,int offset,byte[] data,Date lastModified) throws IOException {
        //create file output streams to write bytes to file
        RandomAccessFile raf = new RandomAccessFile(file, "rw");

        raf.seek(offset);
        raf.write(data, 0, data.length);
        raf.close();
        //get last modified time
        lastModified.setTime(getAttribute(file));
        raf.close();
    }


    public static long getAttribute(String filename){
        return getAttribute(getFile(filename));
    }

    public static long getAttribute(File f){
        return f.lastModified();
    }

    public static long getFileLength(String filename){
        return getFileLength(getFile(filename));
    }

    public static long getFileLength(File f){
        return f.length();
    }

    public static File getFile(String filename){
        //check if the file name is valid (i.e. in the current directory)
        if (filename.contains("/")) {
            return null;
        } else {
            return new File(filename);
        }
    }



}
