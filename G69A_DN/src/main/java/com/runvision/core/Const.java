package com.runvision.core;

public class Const {
    public static final String IP = "192.168.1.2";
    /**
     * 默认开启活体
     */
    public static final boolean OPEN_LIVE = true;

    /**
     * 默认开启语音
     */
    public static final boolean OPEN_MUSIC = true;

    public static final int CLOSE_DOOR_TIME = 1;

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

    public static final String TEMP_DIR = "FaceTemplate";

    /**
     * SharedPreferences 的 KEY
     */
    public static final String KEY_ONEVSMORESCORE = "oneVsMoreScore";
    public static final String KEY_CARDSCORE = "cardScore";
    public static final String KEY_ISOPENLIVE = "isOpenLive";
    public static final String KEY_ISOPENMUSIC = "isOpenMusic";
    public static final String KEY_BACKHOME = "backHome";
    public static final String KEY_OPENDOOR = "openDoor";
    public static final String KEY_VMSIP = "vmsIP";
    public static final String KEY_VMSPROT = "vmsProt";
    public static final String KEY_VMSUSERNAME = "vmsUserName";
    public static final String KEY_VMSPASSWORD = "vmsPassWord";
    public static final String KEY_IP = "IP";
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

    public static final int SOCKET_LOGIN = 9;

    //socket连接超时
    public static final int SOCKET_TIMEOUT = 10;
    public static final int SOCKET_DIDCONNECT = 11;

    //socket接收模版完成
    public static final int SOCKRT_SENDIMAGE = 12;

    public static final int WEB_UPDATE = 13;

    public static final int WEB_BATCHIMAGE_TRUE = 14;

    public static final int WEB_BATCHIMAGE_FALSE = 15;

    public static final int WEB_UPDATE_IP = 16;
    //vms 开始下发模版
    public static final int SOCKET_SEND_TEMPTER=17;

    //socket协议发送消息类型
    //通信协议版本号
    public final static int SOCKET_VERSION = 0x02000000;
    //设备登录
    public final static int NMSG_CNT_DEVLOGIN = 0x00000101;
    //心跳
    public final static int NMSG_DCHNL_STATUS = 0x10020502;
    //上传数据
    public final static int NMSG_FACE_CMPRESULT = 0x00020300;
    //修改设置参数
    public final static int NMSG_DCHNL_SET = 0x10020200;
    //收到模版
    public final static int NMSG_FLIB_ADD = 0x00010100;

    public final static char TYPE_CARD = 0x01;
    public final static char TYPE_ONEVSMORE = 0x02;

}
