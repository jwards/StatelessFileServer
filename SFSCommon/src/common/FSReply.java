package common;

import java.io.Serializable;
import java.util.Date;

public class FSReply implements Serializable {

    private RequestType requested;
    private boolean success;
    private Date lastModified;
    private long fileSize;
    private long bytesRead;
    private byte[] data;

    public FSReply(RequestType replyTo){
        this.requested = replyTo;
    }

    public RequestType getRequested() {
        return requested;
    }

    public void setRequested(RequestType requested) {
        this.requested = requested;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setBytesRead(long bytesRead){
        this.bytesRead = bytesRead;
    }

    public long getBytesRead(){
        return bytesRead;
    }
}
