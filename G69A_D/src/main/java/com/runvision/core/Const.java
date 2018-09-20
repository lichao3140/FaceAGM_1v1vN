package com.runvision.core;

public class Const {


    /**
     * 30S无操作  返回待机页面  相机休眠
     */
    public static final int CLOSE_HOME_TIMEOUT = 30;

    /**
     * FACE_SCORE 人脸比分阈值
     */
    public static final int FACE_SCORE = 53;
    //身份证图片的宽度
    public final static int CARD_WIDTH = 102;
    //身份证图片的高度
    public final static int CARD_HEIGTH = 126;

    /**
     * 相机流的高
     */
    public static final int PRE_HEIGTH = 480;
    /**
     * 相机流的宽
     */
    public static final int PRE_WIDTH = 640;

    public static int Panel_width = 0;


    public static final int MAX_COMPER_NUM = 16;
    // 人证比对完成
    public final static int FLAG_CLEAN = 0;
    // 人证比对完成
    public final static int COMPER_FINISH_FLAG = 1;
    public final static int OPEN_HOME = 2;
    public final static int CLOSE_HOME = 3;
    public static final int RED_INFRA = 4;
    public static final int READ_CARD = 5;




}
