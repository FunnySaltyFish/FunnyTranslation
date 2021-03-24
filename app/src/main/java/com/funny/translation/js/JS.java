package com.funny.translation.js;

public class JS {
    //JS配置文件
    public int id;
    public String fileName;
    public String code;
    public int version;
    public String about;
    public int enabled;//1已开启 0未启用
    public String author;

    //就是一些属性
    public boolean isOffline = false;
    public boolean isDebugMode = false;

    public JS(String code){
        this(0,"tempName",code,1,"未知作者","无更多信息",1);
    }

    public JS(String fileName,int id,String code){
        this(id,fileName,code,1,"未知作者","无更多信息",1);
    }

    public JS(int id, String fileName, String code, int version,String author, String about, int enabled) {
        this.id = id;
        this.fileName = fileName;
        this.code = code;
        this.author = author;
        this.version = version;
        this.about = about;
        this.enabled = enabled;
    }
}
