package com.runvision.db;

/**
 * Created by Administrator on 2018/7/23.
 */

public class Record {
    private int id;
    private String name;
    private String userid;
    private String type;
    private String time;
    private String score;
    private String compertResult;
    private String snapImageID;
    private String templateImageID;

    public Record() {
    }

    public Record(String name, String userid, String type, String time, String score, String compertResult, String snapImageID, String templateImageID) {
        this.name = name;
        this.userid = userid;
        this.type = type;
        this.time = time;
        this.score = score;
        this.compertResult = compertResult;
        this.snapImageID = snapImageID;
        this.templateImageID = templateImageID;
    }

    public Record(int id, String name, String userid, String type, String time, String score, String compertResult, String snapImageID, String templateImageID) {
        this.id = id;
        this.name = name;
        this.userid = userid;
        this.type = type;
        this.time = time;
        this.score = score;
        this.compertResult = compertResult;
        this.snapImageID = snapImageID;
        this.templateImageID = templateImageID;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public String getSnapImageID() {
        return snapImageID;
    }

    public void setSnapImageID(String snapImageID) {
        this.snapImageID = snapImageID;
    }

    public String getTemplateImageID() {
        return templateImageID;
    }

    public void setTemplateImageID(String templateImageID) {
        this.templateImageID = templateImageID;
    }
}
