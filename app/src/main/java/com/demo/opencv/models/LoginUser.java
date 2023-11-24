package com.demo.opencv.models;

import org.litepal.crud.LitePalSupport;

//This is where a copy of the user information is stored, whether this is appropriate is open to consideration, consider using the User class
//LoginUser as opposed to a simulated login and as a buffer for the storage database
public class LoginUser extends LitePalSupport {
    private static LoginUser login_user = new LoginUser();
    private static UserData _user;
    private String name;
    private byte[] portrait;
    private String region;
    private String gender;
    private String brithday;

    public static LoginUser getInstance(){
        return login_user;
    }

    public UserData getUser(){
        return _user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPortrait() {
        return portrait;
    }

    public void setPortrait(byte[] portrait) {
        this.portrait = portrait;
    }

    public String getRegion() {
        return region;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBrithday() {
        return brithday;
    }

    public void setBrithday(String brithday) {
        this.brithday = brithday;
    }
}
