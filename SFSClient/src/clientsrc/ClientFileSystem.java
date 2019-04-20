package clientsrc;

import common.FSReply;
import common.FSRequest;
import common.RequestType;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

//handles the client's file system
public class ClientFileSystem implements fileSystemAPI{

    private HashMap<FileHandle, File> openFiles;

    public ClientFileSystem(){
        openFiles = new HashMap<>();
    }

    @Override
    public FileHandle open(String url) throws IOException {
        //parse url to get host name, port and file name
        Scanner scanner = new Scanner(url);
        scanner.useDelimiter(":");
        String hostname = scanner.next();
        scanner.useDelimiter("/");
        int port = Integer.parseInt(scanner.next().substring(1));
        String filename = scanner.next();

        System.out.println("Connecting to " + hostname + ":" + port + "/" + filename);

        //retrieve file from server
        FileHandle fileHandle = new FileHandle(new FSNetwork(hostname, port));
        fileHandle.getNetwork().openConnection();

        //The file object doesn't go to a file on the local server
        openFiles.put(fileHandle, new File(filename));

        return fileHandle;
    }

    @Override
    public boolean write(FileHandle fh, byte[] data) throws IOException {
        //get file name
        File f = (File)openFiles.get(fh);
        String filename = f.getName();

        fh.setWrite();

        //check if there is a cached file
        File cached = getCached(filename);
        if (cached != null) {
            if (!isCacheStale(cached, fh)) {
                //cache is not stale so we write to it locally
                writeCached(cached, fh, data);
                return true;
            }
        }

        //there isn't a cache or the cache is stale
        File newCached = createCachedFile(filename, fh);

        //write to new cached file
        return writeCached(newCached, fh, data);
    }

    @Override
    public int read(FileHandle fh, byte[] data) throws IOException {
        //get file name
        File f = (File)openFiles.get(fh);
        String filename = f.getName();

        //check if there is a cached file
        File cached = getCached(filename);
        if (cached != null) {
            if (!isCacheStale(cached,fh)) {
                //do read on cache
                System.out.println("Cache is Fresh");
                return readCached(cached, fh, data);
            }
        }

        //cache doesn't exist or is stale so create a new one
        System.out.println("Cache is stale. Retrieving from server");

        File newCached = createCachedFile(filename, fh);

        //now read the new cached version
        return readCached(newCached, fh, data);
    }

    @Override
    public boolean close(FileHandle fh) throws IOException {
        File f = openFiles.get(fh);
        if(fh.didWrite()) {
            flushCacheToServer(f.getName(), fh);
        }
        openFiles.remove(fh);
        fh.discard();
        return true;
    }

    @Override
    public boolean isEOF(FileHandle fh) throws IOException {
        //get file name
        File f = (File)openFiles.get(fh);
        String filename = f.getName();

        //create request to find length of file
        FSRequest request = createLookupReq(filename);

        FSReply reply = fh.getNetwork().sendRequest(request);

        return (fh.getFileIndexPointer() == reply.getFileSize());
    }

    private FSRequest createWriteReq(String filename,int offset,byte[] data) {
        //creates and returns a FSRequest object for requesting a write on the server
        FSRequest request = new FSRequest(RequestType.WRITE);
        request.setFname(filename);
        request.setOffset(offset);
        request.setData(data);
        return request;
    }

    private FSRequest createReadReg(String filename,int offset, int length) {
        //creates and returns a FSRequest object for requesting a read on the server
        FSRequest request = new FSRequest(RequestType.READ);
        request.setFname(filename);
        request.setOffset(offset);
        request.setLength(length);
        return request;
    }

    private FSRequest createAttrReq(String filename) {
        //creates and returns a FSRequest object for requesting a file's attributes (last modified time)
        FSRequest request = new FSRequest(RequestType.GETATTRIBUTE);
        request.setFname(filename);
        return request;
    }

    private FSRequest createLookupReq(String filename) {
        //creates and returns a FSRequest object for requesting a file lookup.
        //In the spec it says that this request takes a URL with the form IP-address:port/filename.
        //I'm not sure why it would be this way since the server wouldn't care about the ip address and
        //we already specify the server url in the open method.
        //I just use the filename here instead.
        FSRequest request = new FSRequest(RequestType.LOOKUP);
        request.setFname(filename);
        return request;
    }

    //given a file name, checks if the file is cached and returns it if it is
    //returns null if it isn't cached
    private File getCached(String filename){
        //cached files have extension ".FSCACHE"
        File cached = new File(filename + ".FSCACHE");
        if (cached.exists()) {
            return cached;
        } else {
            return null;
        }
    }

    //checks if a file cached file is stale
    private boolean isCacheStale(File cachedFile,FileHandle fileHandle){
        int nameLength = cachedFile.getName().length();
        String filename = cachedFile.getName().substring(0, nameLength - ".FSCACHE".length());
        System.out.println("\nChecking cached file:" +filename);

        FSRequest request = createAttrReq(filename);
        try {
            FSReply reply = fileHandle.getNetwork().sendRequest(request);
            //if the server version was written to after this cached file was made, the cache is stale
            return reply.getLastModified().after(new Date(cachedFile.lastModified()));
        } catch (IOException e) {
            return true;
        }
    }

    //performs read on a cached file
    private int readCached(File cached,FileHandle fh,byte[] data) throws IOException {

        FileInputStream fin = new FileInputStream(cached);
        //skip to where we want to read
        fin.skip(fh.getFileIndexPointer());
        int bytesRead = fin.read(data, 0, data.length);
        fh.updateFilePointer(bytesRead + fh.getFileIndexPointer());
        fin.close();
        //read is done

        return bytesRead;
    }

    //reads the a whole cached file
    private void readFullCached(File cached, byte[] data) throws IOException {
        FileInputStream fin = new FileInputStream(cached);
        int bytesRead = fin.read(data, 0, data.length);
        fin.close();
        //read is done
    }

    //writes to the local cache
    private boolean writeCached(File cached,FileHandle fh,byte[] data) throws IOException {
        //create file output streams to write bytes to file
        RandomAccessFile raf = new RandomAccessFile(cached, "rw");

        raf.seek(fh.getFileIndexPointer());
        raf.write(data, 0, data.length);

        //update file pointer
        fh.updateFilePointer(fh.getFileIndexPointer()+data.length);

        raf.close();
        return true;
    }

    //creates a locally cached file
    private File createCachedFile(String filename,FileHandle fileHandle) throws IOException {
        //create read request for the whole file
        FSRequest request = createReadReg(filename, 0, -1);
        //send and get reply
        FSReply reply = fileHandle.getNetwork().sendRequest(request);

        //write cached file
        File newCached = new File(filename + ".FSCACHE");
        FileOutputStream fout = new FileOutputStream(newCached);
        fout.write(reply.getData());
        fout.close();

        return newCached;
    }

    //writes the cached file to the server
    private void flushCacheToServer(String filename, FileHandle fileHandle) throws IOException {
        File file = getCached(filename);

        if(file==null){
            //no cache exists
            return;
        }

        byte[] data = new byte[(int) file.length()];

        //read from cached file
        readFullCached(file, data);

        //create write req
        FSRequest request = createWriteReq(filename, 0, data);

        //send and get reply
        FSReply reply = fileHandle.getNetwork().sendRequest(request);
        if(reply.isSuccess()){
            System.out.println("Cached file was sent to server");
        } else {
            System.out.println("Error in sending cached file to server");
        }
    }
}
