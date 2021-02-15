package com.funny.translation.bean;

public class App {
    int image;
    String name;
    String introduction;
    String url;

    public App(int image, String name, String introduction, String url) {
        this.image = image;
        this.name = name;
        this.introduction = introduction;
        this.url = url;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
