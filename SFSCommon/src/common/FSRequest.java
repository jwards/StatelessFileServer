package common;

import java.io.Serializable;

public class FSRequest implements Serializable {

    private RequestType type;
    private String fname;
    private int offset;
    private int length;
    private byte[] data;


    public FSRequest(RequestType type){
        this.type = type;
    }

    public void setFname(String fname){
        this.fname = fname;
    }

    public void setOffset(int offset){
        this.offset = offset;
    }

    public String getFname() {
        return fname;
    }

    public int getOffset() {
        return offset;
    }

    public byte[] getData() {
        return data;
    }

    public int getLength(){
        return length;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setLength(int length){
        this.length = length;
    }

    public RequestType getType(){
        return type;
    }
}
