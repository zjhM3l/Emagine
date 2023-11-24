package com.demo.opencv.models;

import org.litepal.crud.LitePalSupport;

import java.util.Date;

public class Owns extends LitePalSupport {
    private byte[] image;
    private String username;
    private long timeStamp;
    private String title;
    private String price;

    public String getPrice() {
        return price;
    }

    public String getTitle() {
        return title;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public byte[] getImage() {
        return image;
    }

    public String getUsername() {
        return username;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTimeStamp() {
        this.timeStamp = new Date().getTime();
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
