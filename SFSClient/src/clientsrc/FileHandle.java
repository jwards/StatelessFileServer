package clientsrc;


/* You can change implementation as you like. This is a simple one. */

import common.FSRequest;

public class FileHandle {

    private FSNetwork server;
    private int fileIndexPointer = 0;

    //this is set to true when a client writes to a file
    //so we can know whether or not to flush the file on close
    private boolean write = false;

    private int index;

    private static int cnt = 1;

    public FileHandle(FSNetwork server) {
        synchronized (this) {
            index = cnt++;
        }
        this.server = server;
    }

    public FSNetwork getNetwork(){
        return server;
    }

    public int getFileIndexPointer(){
        return fileIndexPointer;
    }

    public void updateFilePointer(int newPointer){
        fileIndexPointer = newPointer;
    }

    public boolean isAlive() {
        return (this.index != 0);
    }

    /* discarding a FileHandle. you do not have to use this. */
    public void discard() {
        index = 0;
        fileIndexPointer = 0;

        //make sure that the connection is closed
        server.closeConnection();
    }

    public void setWrite(){
        write = true;
    }

    public boolean didWrite(){
        return write;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null){
            if (obj instanceof FileHandle) {
                FileHandle fh = (FileHandle) obj;
                return index == fh.index;
            }
        }
        return false;
    }
}