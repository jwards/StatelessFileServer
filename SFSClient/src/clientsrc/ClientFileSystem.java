package clientsrc;

import common.FSReply;
import common.FSRequest;
import common.RequestType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

//handles the client's file system
public class ClientFileSystem implements fileSystemAPI{

    private FSNetwork network;

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

        //create write req
        FSRequest request = createWriteReq(filename, fh.getFileIndexPointer(), data);

        //send and get reply
        FSReply reply = fh.getNetwork().sendRequest(request);

        //check for success
        if (reply.isSuccess()) {
            //update file pointer
            fh.updateFilePointer(data.length+fh.getFileIndexPointer());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int read(FileHandle fh, byte[] data) throws IOException {
        //get file name
        File f = (File)openFiles.get(fh);
        String filename = f.getName();

        //create read request
        FSRequest request = createReadReg(filename, fh.getFileIndexPointer(), data.length);

        //send and get reply
        FSReply reply = fh.getNetwork().sendRequest(request);

        if (reply.isSuccess()) {
            //update filePointer
            fh.updateFilePointer((int) (reply.getBytesRead()+fh.getFileIndexPointer()));

            //copy data into array
            System.arraycopy(reply.getData(), 0, data,0, data.length);

            return reply.getData().length;
        } else {
            return 0;
        }
    }

    @Override
    public boolean close(FileHandle fh) throws IOException {
        openFiles.remove(fh);
        fh.discard();
        //not sure how a file close could fail. It either closes or it wasn't open in the first place
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
}
