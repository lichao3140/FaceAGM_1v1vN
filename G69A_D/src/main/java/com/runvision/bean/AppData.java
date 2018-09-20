package com.runvision.bean;

import android.graphics.Bitmap;


/**
 * Created by Administrator on 2018/5/31.
 */

public class AppData {


    private String CardNo;// 证件号
    private String Name;// 姓名
    private String sex;// 性别
    private String Nation;// 名族代码

    private String Birthday;// 生日
    private String Address;// 地址
    private float CompareScore;// 人证比对的比分

    private Bitmap faceBmp;
    private Bitmap cardBmp;


    private int flag=0;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    private static AppData appData=new AppData();

    public static AppData getAppData(){
        return appData;
    }

    public Bitmap getFaceBmp() {
        return faceBmp;
    }



    public String getCardNo() {
        return CardNo;
    }

    public void setCardNo(String cardNo) {
        CardNo = cardNo;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNation() {
        return Nation;
    }

    public void setNation(String nation) {
        Nation = nation;
    }

    public String getBirthday() {
        return Birthday;
    }

    public void setBirthday(String birthday) {
        Birthday = birthday;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public float getCompareScore() {
        return CompareScore;
    }

    public void setCompareScore(float compareScore) {
        CompareScore = compareScore;
    }

    public void setFaceBmp(Bitmap faceBmp) {
        this.faceBmp = faceBmp;
    }

    public Bitmap getCardBmp() {
        return cardBmp;
    }

    public void setCardBmp(Bitmap cardBmp) {
        this.cardBmp = cardBmp;
    }
}
