package com.runvision.core;

public class Const {
    /**
     * 默认开启活体
     */
    public static final boolean OPEN_LIVE = true;

    /**
     * 1：N当前失败次数
     */
    public static int ONE_VS_MORE_TIMEOUT_NUM = 0;
    /**
     * 1:N允许最大失败次数
     */
    public static int ONE_VS_MORE_TIMEOUT_MAXNUM = 3;
    /**
     * 1:N的分数阀值
     */
    public static final int ONEVSMORE_SCORE = 68;
    /**
     * 30S无操作  返回待机页面  相机休眠
     */
    public static final int CLOSE_HOME_TIMEOUT = 30;
    /**
     * 抓拍注册是过滤人脸的角度（注册模板）
     */
    public static final int OFFSET = 2;
    /**
     * 1：N的比对时过滤人脸的角度
     */
    public static final int ONE_VS_MORE_OFFSET = 15;
    /**
     * FACE_SCORE 人脸比分阈值
     */
    public static final int FACE_SCORE = 53;
    /**
     * 身份证图片的宽度
     */
    public final static int CARD_WIDTH = 102;
    /**
     * 身份证图片的高度
     */
    public final static int CARD_HEIGTH = 126;
    /**
     * 相机流的高
     */
    public static final int PRE_HEIGTH = 480;
    /**
     * 相机流的宽
     */
    public static final int PRE_WIDTH = 640;
    /**
     * 实际相机显示的大小
     */
    public static int Panel_width = 0;
    /**
     * 最大比对次数
     */
    public static final int MAX_COMPER_NUM = 10;

    /**
     * 是否注册人脸标志位
     */
    public static boolean is_regFace = false;
    /**
     * 是否打开1：N的线程   默认打开
     */
    public static boolean openOneVsMore = true;

    /**
     * 是否允许显示本次比对结果  默认显示
     */
    public static boolean isShowOneVsMoreResult = true;
    /**
     * 抓拍图片保存未知
     */
    public static final String SNAP_DIR = "Snap";
    /**
     * 身份证照片保存位置
     */
    public static final String CARD_DIR = "Card";

    /**
     * 消息传递的标志
     */
    // 人证比对完成
    public final static int FLAG_CLEAN = 0;
    // 人证比对完成
    public final static int COMPER_FINISH_FLAG = 1;
    public final static int OPEN_HOME = 2;
    public final static int CLOSE_HOME = 3;
    public static final int RED_INFRA = 4;
    public static final int READ_CARD = 5;
    //注册抓拍人脸
    public static final int REG_FACE = 6;
    //1：N比对完成
    public static final int ONE_VS_MORE_SUCCESS = 7;
    public static final int ONE_VS_MORE_FAIL = 8;


}
