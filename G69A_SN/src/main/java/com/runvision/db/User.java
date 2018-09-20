package com.runvision.db;

/**
 * Created by Administrator on 2018/7/5.
 */

public class User {
    private int id;
    private String name;
    private String userid;
    private String type;
    private String time;
    private String imagepath;

    private String score;
    private String compertResult;

    public User(int id, String name, String userid, String type) {
        this.id = id;
        this.name = name;
        this.userid = userid;
        this.type = type;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getCompertResult() {
        return compertResult;
    }

    public void setCompertResult(String compertResult) {
        this.compertResult = compertResult;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public User() {
    }

    public User(String name, String userid, String type, String time, String imagepath) {
        this.name = name;
        this.userid = userid;
        this.type = type;
        this.time = time;
        this.imagepath = imagepath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
