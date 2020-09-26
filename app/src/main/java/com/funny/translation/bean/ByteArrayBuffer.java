package com.funny.translation.bean;

import java.util.Arrays;

public class ByteArrayBuffer{
    /**
     * 一个byte容器，存储读取的字节流
     */
    private byte[] bytes;
    public byte[] getBytes() {
        return bytes;
    }
    public int getByteslen() {
        return byteslen;
    }
    private int byteslen;
    public ByteArrayBuffer(){
        bytes = new byte[1024];
        byteslen = 0;
    }
    public ByteArrayBuffer append(byte[] bytesAppend, int len){
        int freespace = bytes.length - byteslen;
        if(freespace < len){
            resize(len + byteslen);
        }
        System.arraycopy(bytesAppend, 0, bytes, byteslen, len);
        byteslen += len;
        return this;
    }
    private void resize(int newsize){
        int newsize1 = 1;
        while(newsize1 < newsize)
            newsize1 <<= 1;
        bytes = Arrays.copyOf(bytes, newsize1);
    }
}
