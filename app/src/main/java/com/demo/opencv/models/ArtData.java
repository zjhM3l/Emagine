package com.demo.opencv.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.litepal.crud.LitePalSupport;

public class ArtData extends LitePalSupport {
    private String name;
    private String price;
    private byte[] image;
    private int imgHeight;
    private Boolean show;


    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
    }

    public byte[] getImage() {
        return image;
    }

    public String getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getImgHeight() {
        Bitmap bp = Bytes2Bimap(image);
        return bp.getHeight();
    }

    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
