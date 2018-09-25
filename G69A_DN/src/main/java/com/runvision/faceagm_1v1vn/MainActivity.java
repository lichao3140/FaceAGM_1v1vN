package com.runvision.faceagm_1v1vn;

import com.runvision.bean.AppData;
import com.runvision.bean.Type;
import com.runvision.broadcast.NetWorkStateReceiver;
import com.runvision.core.Const;

import com.runvision.core.LogToFile;

import com.runvision.core.MyApplication;
import com.runvision.db.Record;
import com.runvision.db.User;
import com.runvision.gpio.GPIOHelper;

import com.runvision.myview.MyCameraSuf;
import com.runvision.thread.BatchImport;
import com.runvision.thread.HeartBeatThread;
import com.runvision.thread.ListenOperation;
import com.runvision.thread.ListenRedInfra;
import com.runvision.thread.SocketThread;
import com.runvision.util.ConversionHelp;
import com.runvision.util.DateTimeUtils;
import com.runvision.util.FileUtils;
import com.runvision.util.IDUtils;
import com.runvision.util.SPUtil;
import com.runvision.util.SendData;
import com.runvision.webcore.ServerManager;
import com.runvision.webcore.util.NetUtils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android_serialport_api.SerialPort;

public class MainActivity extends Activity implements NetWorkStateReceiver.INetStatusListener, View.OnClickListener {
    private String TAG = "MainActivity";
    private static final int REGISTER_RESULT_CODE = 1;
    private Context mContext;
    public MyCameraSuf mCameraSurfView;
    private boolean flag = true;
    private MyApplication application;
    private ListenOperation mlisten;

    // -------------视图控件------------------
    // -----------------------------------------
    private View alert;
    private ImageView faceBmp_view, cardBmp_view, idcard_Bmp, isSuccessComper;
    private TextView card_name, card_sex, card_nation, name, year, month, day, addr, cardNumber, version;
    private View pro_xml;
    private RelativeLayout home;
    private FrameLayout index;

    private MediaPlayer mPlayer;
    private ListenRedInfra mRedInfra;
    //1：N的UI
    private View oneVsMoreView;
    private ImageView oneVsMore_face, oneVsMore_temper;
    private TextView oneVsMore_userName, oneVsMore_userID, oneVsMore_userType;
    private View ts_xml;
    private TextView ts_xml_msg;
    //这个按钮是设置或以开关的
    private NetWorkStateReceiver receiver;
    private TextView socket_status;
    private ImageView goRegisterActivity;
    //
    private SocketThread socketThread;
    private HeartBeatThread heartBeatThread;
    private TextView showHttpUrl;
    private ServerManager serverManager;
    private boolean HTTP_Status = false;
    private String currIp;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Const.COMPER_FINISH_FLAG:
                    pro_xml.setVisibility(View.GONE);
                    ListenOperation.cleanTime();
                    showAlertDialog();
                    break;
                case Const.OPEN_HOME:
                    //关闭1：N  中途的线程就算完成也不能显示
                    Const.openOneVsMore = false;
                    Const.isShowOneVsMoreResult = false;

                    LogToFile.e("MainActivity", "打开比对界面，唤醒相机");
                    Log.i(TAG, "OPEN_HOME");
                    // 补光灯
                    SerialPort.openLED();
                    // 唤醒相机
                    mCameraSurfView.openCamera();
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    home.setVisibility(View.GONE);
                    index.setVisibility(View.VISIBLE);

                    handler.removeCallbacks(runnable);
                    handler.removeCallbacks(mResetRunnble);
                    alert.setVisibility(View.GONE);

                    handler.removeCallbacks(oneVsMoreRunnable);
                    oneVsMoreView.setVisibility(View.GONE);
                    handler.removeCallbacks(closeTsRunnable);
                    ts_xml.setVisibility(View.GONE);
                    break;

                case Const.CLOSE_HOME:
                    if (mRedInfra.getRedStatus() == 0) {
                        home.setVisibility(View.VISIBLE);
                        index.setVisibility(View.GONE);
                        mCameraSurfView.releaseCamera();
                        pro_xml.setVisibility(View.GONE);
                        ListenOperation.cleanTime();
                    }
                    break;

                case Const.RED_INFRA:
                    LogToFile.e("MainActivity", "收到红外感应的信息");
                    mCameraSurfView.openCamera();
                    SerialPort.openLED();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    home.setVisibility(View.GONE);
                    index.setVisibility(View.VISIBLE);
                    ListenOperation.cleanTime();
                    break;

                case Const.READ_CARD:
                    pro_xml.setVisibility(View.VISIBLE);
                    playMusic(R.raw.lookcamera);
                    break;
                case Const.ONE_VS_MORE_SUCCESS:
                case Const.ONE_VS_MORE_FAIL:
                    ListenOperation.cleanTime();
                    if (Const.isShowOneVsMoreResult) {
                        showOneVSMore();
                    }
                    break;

                case Const.SOCKET_LOGIN:
                    boolean isSuccess = (boolean) msg.obj;
                    if (isSuccess) {
                        Toast.makeText(mContext, "socket登录成功", Toast.LENGTH_SHORT).show();
                        LogToFile.i("MainActivity", "socket登录成功");
                        socket_status.setBackgroundResource(R.drawable.socket_true);
                        //开启心跳
                        if (heartBeatThread != null) {
                            heartBeatThread.HeartBeatThread_flag = false;
                            heartBeatThread = null;
                        }
                        heartBeatThread = new HeartBeatThread(socketThread);
                        heartBeatThread.start();
                    } else {
                        socket_status.setBackgroundResource(R.drawable.socket_false);
                        LogToFile.i("MainActivity", "socket登录失败");
                        Toast.makeText(mContext, "socket登录失败", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case Const.SOCKET_TIMEOUT:
                    socket_status.setBackgroundResource(R.drawable.socket_false);
                    String prompt = (String) msg.obj;
                    LogToFile.i("MainActivity", prompt);
                    Toast.makeText(mContext, prompt, Toast.LENGTH_SHORT).show();
                    break;

                case Const.SOCKET_DIDCONNECT:
                    socket_status.setBackgroundResource(R.drawable.socket_false);
                    closeSocket();
                    break;

                case Const.SOCKET_SEND_TEMPTER:
                    if (receiver != null) {
                        unregisterReceiver(receiver);
                        receiver = null;
                    }
                    mCameraSurfView.releaseCamera();
                    stopService(new Intent(MainActivity.this, MainService.class));
                    // 取消循环监听标志位的线程
                    flag = false;
                    if (mPlayer != null) {
                        mPlayer.release();
                        mPlayer = null;
                    }
                    //关闭红外感应线程
                    if (mRedInfra != null) {
                        mRedInfra.red_infra_flag = false;
                        mRedInfra = null;
                    }
                    if (mlisten != null) {
                        mlisten.flag = false;
                        mlisten = null;
                    }
                    if (mPlayer != null) {
                        mPlayer.release();
                        mPlayer = null;
                    }
                    Const.openOneVsMore = false;
                    home.setVisibility(View.VISIBLE);
                    ts_xml_msg.setText("模版导入中");
                    ts_xml.setVisibility(View.VISIBLE);
                    break;

                case Const.SOCKRT_SENDIMAGE:
                    batchImport();
                    break;

                case Const.WEB_UPDATE:
                    if (!SPUtil.getString(Const.KEY_VMSIP, "").equals("") && SPUtil.getInt(Const.KEY_VMSPROT, 0) != 0 && !SPUtil.getString(Const.KEY_VMSUSERNAME, "").equals("") && !SPUtil.getString(Const.KEY_VMSPASSWORD, "").equals("")) {
                        //开启socket线程
                        socketReconnect(SPUtil.getString(Const.KEY_VMSIP, ""), SPUtil.getInt(Const.KEY_VMSPROT, 0));
                    }
                    break;

                case 101:
                    int success1 = (int) msg.obj;
                    bacthOk1 = success1;
                    double a = (double) success1 / (double) dataList1.size();
                    int b = (int) (a * 100);
                    progesssValue1.setText(success1 + "/" + dataList1.size());
                    progesss1.setProgress(b);
                    if (bacthOk1 + bacthOk2 + bacthOk3 == mSum) {
                        batchDialog.dismiss();
                        vmsInertTempterOk();
                    }
                    break;

                case 102:
                    int success2 = (int) msg.obj;
                    bacthOk2 = success2;
                    double a2 = (double) success2 / (double) dataList2.size();
                    int b2 = (int) (a2 * 100);
                    progesssValue2.setText(success2 + "/" + dataList2.size());
                    progesss2.setProgress(b2);
                    if (bacthOk1 + bacthOk2 + bacthOk3 == mSum) {
                        batchDialog.dismiss();
                        vmsInertTempterOk();
                    }
                    break;

                case 103:
                    int success3 = (int) msg.obj;
                    bacthOk3 = success3;
                    double a3 = (double) success3 / (double) dataList3.size();
                    int b3 = (int) (a3 * 100);
                    progesssValue3.setText(success3 + "/" + dataList3.size());
                    progesss3.setProgress(b3);
                    if (bacthOk1 + bacthOk2 + bacthOk3 == mSum) {
                        batchDialog.dismiss();
                        vmsInertTempterOk();
                    }
                    break;

                case Const.WEB_BATCHIMAGE_FALSE:
                    ts_xml_msg.setText("请刷身份证");
                    ts_xml.setVisibility(View.GONE);
                    flag = false;
                    onResume();
                    openNetStatusReceiver();
                    break;

                case Const.WEB_BATCHIMAGE_TRUE:
                    onPause();
                    flag = true;
                    updateView();
                    home.setVisibility(View.VISIBLE);
                    ts_xml_msg.setText("模版导入中");
                    ts_xml.setVisibility(View.VISIBLE);
                    break;

                case Const.WEB_UPDATE_IP:
                    ConversionHelp.updateIp(SPUtil.getString(Const.KEY_IP, ""), mContext);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            closeHttpServer();
                            openHttpServer();
                        }
                    }, 3000);
                    break;
                default:
                    break;
            }
        }

        ;
    };

    private void vmsInertTempterOk() {
        openNetStatusReceiver();
        hideBottomUIMenu();
        mCameraSurfView.setCameraType(0);
        GPIOHelper.init();
        startService(new Intent(MainActivity.this, MainService.class));
        flag = true;
        updateView();
        Const.openOneVsMore = true;
        if (mRedInfra == null) {
            mRedInfra = new ListenRedInfra(handler);
            mRedInfra.start();
        }
        if (mlisten == null) {
            mlisten = new ListenOperation(handler);
            mlisten.start();
        }
        ts_xml.setVisibility(View.GONE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 横屏
        // 全屏代码
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideBottomUIMenu();
        mContext = this;

        application = (MyApplication) getApplication();
        application.init();
        application.addActivity(this);

        initView();


        if (!HTTP_Status && ConversionHelp.isNetworkConnected(mContext)) {
            openHttpServer();
        }


    }


    private void initView() {
        mCameraSurfView = (MyCameraSuf) findViewById(R.id.myCameraView);
        mCameraSurfView.setCameraType(0);
        alert = findViewById(R.id.alert_xml);
        faceBmp_view = (ImageView) alert.findViewById(R.id.comperFacebm);
        cardBmp_view = (ImageView) alert.findViewById(R.id.comperCardbm);
        idcard_Bmp = (ImageView) alert.findViewById(R.id.cardImage);
        card_name = (TextView) alert.findViewById(R.id.name);
        name = (TextView) alert.findViewById(R.id.userName);
        card_sex = (TextView) alert.findViewById(R.id.sex);
        card_nation = (TextView) alert.findViewById(R.id.nation);
        year = (TextView) alert.findViewById(R.id.year);
        day = (TextView) alert.findViewById(R.id.day);
        month = (TextView) alert.findViewById(R.id.month);
        addr = (TextView) alert.findViewById(R.id.addr);
        cardNumber = (TextView) alert.findViewById(R.id.cardNumber);
        isSuccessComper = (ImageView) alert.findViewById(R.id.isSuccessComper);
        pro_xml = findViewById(R.id.pro);
        home = (RelativeLayout) findViewById(R.id.home_layout);
        index = (FrameLayout) findViewById(R.id.index);
        version = (TextView) findViewById(R.id.version);
        version.setText("G69A_DN:" + getVersion(mContext));

        oneVsMoreView = findViewById(R.id.onevsmore);
        oneVsMore_face = oneVsMoreView.findViewById(R.id.onevsmore_face);
        oneVsMore_temper = oneVsMoreView.findViewById(R.id.onevsmore_temper);
        oneVsMore_userName = oneVsMoreView.findViewById(R.id.onevsmore_userName);
        oneVsMore_userID = oneVsMoreView.findViewById(R.id.onevsmore_userID);
        oneVsMore_userType = oneVsMoreView.findViewById(R.id.onevsmore_userType);
        ts_xml = findViewById(R.id.ts_xml);
        ts_xml_msg = ts_xml.findViewById(R.id.onevsmore_dialog_showmsg);

        socket_status = findViewById(R.id.socket_status);
        showHttpUrl = findViewById(R.id.showHttpUrl);
        goRegisterActivity = findViewById(R.id.goRegisterActivity);
        goRegisterActivity.setOnClickListener(this);
    }


    /**
     * 获取软件当前版本
     *
     * @param mContext
     * @return
     */
    public String getVersion(Context mContext) {
        String version = "";

        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo info = pm.getPackageInfo(mContext.getPackageName(), 0);
            version = info.versionName;
            if (version == null || version.length() <= 0) {
                return "";
            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.e("MainActivity", "getVersion: ", e);

        }
        return version;
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    @SuppressLint("NewApi")
    protected void hideBottomUIMenu() {
        // 隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            // for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public void updateView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag) {
                    if (MainService.getService() == null) {
                        continue;
                    }
                    if (AppData.getAppData().getFlag() == Const.FLAG_CLEAN) {
                        continue;
                    }
                    handler.sendEmptyMessage(AppData.getAppData().getFlag());
                    AppData.getAppData().setFlag(Const.FLAG_CLEAN);

                }
            }
        }).start();
    }


    /**
     * 显示1比N比对信息
     */
    private void showOneVSMore() {
        if (AppData.getAppData().getCompareScore() >= SPUtil.getInt(Const.KEY_ONEVSMORESCORE, Const.ONEVSMORE_SCORE)) {
            oneVsMore_face.setImageBitmap(AppData.getAppData().getFaceBmp());
            String snapImageID = IDUtils.genImageName();
            FileUtils.saveFile(AppData.getAppData().getFaceBmp(), snapImageID, Const.SNAP_DIR);
            User user = MyApplication.faceProvider.getUserByUserId(AppData.getAppData().getUser().getId());

            if (user.getType().equals(Type.黑名单.getDesc())) {
                ts_xml_msg.setText("黑名单用户");
                ts_xml.setVisibility(View.VISIBLE);
                handler.postDelayed(closeTsRunnable, 3000);
                return;
            }
            AppData.getAppData().setUser(user);
            String sdCardDir = Environment.getExternalStorageDirectory() + "/FaceAndroid/FaceTemplate/" + user.getTemplateImageID() + ".jpg";
            try {
                Bitmap bmp = BitmapFactory.decodeFile(sdCardDir);
                AppData.getAppData().setCardBmp(bmp);
                oneVsMore_temper.setImageBitmap(bmp);
            } catch (Exception e) {
                oneVsMore_temper.setImageResource(R.mipmap.ic_launcher);
            }
            GPIOHelper.openDoor(true);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    GPIOHelper.openDoor(false);
                }
            }, SPUtil.getInt(Const.KEY_OPENDOOR, Const.CLOSE_DOOR_TIME) * 1000);

            oneVsMore_userName.setText(user.getName());
            oneVsMore_userType.setText(user.getType());
            oneVsMore_userID.setText(user.getWordNo());
            LogToFile.e("1:N", "1:N成功: 姓名：" + user.getName() + ",分数：" + AppData.getAppData().getCompareScore());
            user.setTime(DateTimeUtils.getTime());
            Record record = new Record(AppData.getAppData().getCompareScore() + "", "成功", snapImageID, "1:N");
            user.setRecord(record);
            MyApplication.faceProvider.addRecord(user);

            oneVsMoreView.setVisibility(View.VISIBLE);
            playMusic(R.raw.success);
            handler.postDelayed(oneVsMoreRunnable, 2000);


            if (socketThread != null) {
                SendData.sendComperMsgInfo(socketThread, true, Const.TYPE_ONEVSMORE);
            }

        } else {
            playMusic(R.raw.burlcard);
            ts_xml_msg.setText("请刷身份证");
            ts_xml.setVisibility(View.VISIBLE);
            handler.postDelayed(closeTsRunnable, 5000);
        }


    }

    public Runnable oneVsMoreRunnable = new Runnable() {
        @Override
        public void run() {
            oneVsMoreView.setVisibility(View.GONE);
            //此处1:N 完成
            Const.openOneVsMore = true;
        }
    };
    public Runnable closeTsRunnable = new Runnable() {
        @Override
        public void run() {
            ts_xml.setVisibility(View.GONE);
            //此处1:N 完成
            Const.openOneVsMore = true;
        }
    };


    /**
     * 显示人证比对信息框
     */
    private void showAlertDialog() {
        Bitmap cardBmp = AppData.getAppData().getCardBmp();
        System.out.println("我是比对分数:" + AppData.getAppData().getCompareScore() + "," + Const.FACE_SCORE);
        String str = "";
        cardBmp_view.setImageBitmap(cardBmp);
        faceBmp_view.setImageBitmap(AppData.getAppData().getFaceBmp());
        idcard_Bmp.setImageBitmap(cardBmp);
        card_name.setText(AppData.getAppData().getName());
        card_sex.setText(AppData.getAppData().getSex());
        name.setText(AppData.getAppData().getName());
        year.setText(AppData.getAppData().getBirthday().substring(0, 4));
        month.setText(AppData.getAppData().getBirthday().substring(5, 7));
        day.setText(AppData.getAppData().getBirthday().substring(8, 10));
        addr.setText(AppData.getAppData().getAddress());
        cardNumber.setText(AppData.getAppData().getCardNo().substring(0, 4) + "************" + AppData.getAppData().getCardNo().substring(16, 18));
        card_nation.setText(AppData.getAppData().getNation());
        faceBmp_view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (AppData.getAppData().getCompareScore() == 0) {
            str = "失败";
            isSuccessComper.setImageResource(R.mipmap.icon_sb);

            if (AppData.getAppData().getFaceBmp() == null) {
                faceBmp_view.setImageResource(R.mipmap.tx);
                faceBmp_view.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                //保存抓拍图片
                String snapImageID = IDUtils.genImageName();
                FileUtils.saveFile(AppData.getAppData().getFaceBmp(), snapImageID, Const.SNAP_DIR);
                //保存身份证图片
                String cardImageID = snapImageID + "_card";
                FileUtils.saveFile(cardBmp, cardImageID, Const.CARD_DIR);

                Record record = new Record(AppData.getAppData().getCompareScore() + "", str, snapImageID, "人证");
                User user = new User(AppData.getAppData().getName(), "无", AppData.getAppData().getSex(), 0, "无", AppData.getAppData().getCardNo(), cardImageID, DateTimeUtils.getTime());
                user.setRecord(record);
                MyApplication.faceProvider.addRecord(user);
            }

            playMusic(R.raw.error);

        } else if (AppData.getAppData().getCompareScore() < SPUtil.getInt(Const.KEY_CARDSCORE, Const.FACE_SCORE)) {

            isSuccessComper.setImageResource(R.mipmap.icon_sb);
            playMusic(R.raw.error);
            str = "失败";


            //保存抓拍图片
            String snapImageID = IDUtils.genImageName();
            FileUtils.saveFile(AppData.getAppData().getFaceBmp(), snapImageID, Const.SNAP_DIR);
            //保存身份证图片
            String cardImageID = snapImageID + "_card";
            FileUtils.saveFile(cardBmp, cardImageID, Const.CARD_DIR);

            Record record = new Record(AppData.getAppData().getCompareScore() + "", str, snapImageID, "人证");
            User user = new User(AppData.getAppData().getName(), "无", AppData.getAppData().getSex(), 0, "无", AppData.getAppData().getCardNo(), cardImageID, DateTimeUtils.getTime());
            user.setRecord(record);
            MyApplication.faceProvider.addRecord(user);

        } else {
            str = "成功";
            playMusic(R.raw.success);
            isSuccessComper.setImageResource(R.mipmap.icon_tg);
            GPIOHelper.openDoor(true);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    GPIOHelper.openDoor(false);
                }
            }, SPUtil.getInt(Const.KEY_OPENDOOR, Const.CLOSE_DOOR_TIME) * 1000);

            //保存抓拍图片
            String snapImageID = IDUtils.genImageName();
            FileUtils.saveFile(AppData.getAppData().getFaceBmp(), snapImageID, Const.SNAP_DIR);
            //保存身份证图片
            String cardImageID = snapImageID + "_card";
            FileUtils.saveFile(cardBmp, cardImageID, Const.CARD_DIR);

            Record record = new Record(AppData.getAppData().getCompareScore() + "", str, snapImageID, "人证");
            User user = new User(AppData.getAppData().getName(), "无", AppData.getAppData().getSex(), 0, "无", AppData.getAppData().getCardNo(), cardImageID, DateTimeUtils.getTime());
            user.setRecord(record);
            MyApplication.faceProvider.addRecord(user);
        }
        alert.setVisibility(View.VISIBLE);
        LogToFile.e("比对记录", AppData.getAppData().getName() + "_" + AppData.getAppData().getSex() + "_" + AppData.getAppData().getCardNo().substring(0, 4) + "************"
                + AppData.getAppData().getCardNo().substring(16, 18) + "_比对结果:" + str + ",比对分数:" + AppData.getAppData().getCompareScore());


        handler.postDelayed(runnable, 3000);
        handler.postDelayed(mResetRunnble, 3000);

        if (AppData.getAppData().getCompareScore() < SPUtil.getInt(Const.KEY_CARDSCORE, Const.FACE_SCORE) && AppData.getAppData().getFaceBmp() != null) {
            if (socketThread != null) {
                SendData.sendComperMsgInfo(socketThread, false, Const.TYPE_CARD);
            }
        }
        if (AppData.getAppData().getCompareScore() >= SPUtil.getInt(Const.KEY_CARDSCORE, Const.FACE_SCORE)) {
            if (socketThread != null) {
                SendData.sendComperMsgInfo(socketThread, true, Const.TYPE_CARD);
            }
        }

    }

    private Runnable mResetRunnble = new Runnable() {
        @Override
        public void run() {
            //1:1完成  2s后打开1:N的线程
            Const.openOneVsMore = true;
            Const.isShowOneVsMoreResult = true;
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            alert.setVisibility(View.GONE);
        }
    };

    /**
     * 播放音频文件
     *
     * @param msuicID
     */
    public void playMusic(int msuicID) {

        if (!SPUtil.getBoolean(Const.KEY_ISOPENMUSIC, Const.OPEN_MUSIC)) {
            return;
        }

        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.release();
            }
        }
        mPlayer = MediaPlayer.create(mContext, msuicID);
        mPlayer.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println(requestCode + "," + resultCode);
        if (requestCode == REGISTER_RESULT_CODE) {
            if (ConversionHelp.isNetworkConnected(mContext) && !currIp.equals(NetUtils.getIpAddress(mContext)) && !NetUtils.getIpAddress(mContext).equals("")) {
                openHttpServer();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("---------onPause---------");
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        mCameraSurfView.releaseCamera();
        stopService(new Intent(MainActivity.this, MainService.class));
        // 取消循环监听标志位的线程
        flag = false;
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        //关闭红外感应线程
        if (mRedInfra != null) {
            mRedInfra.red_infra_flag = false;
            mRedInfra = null;
        }
        if (mlisten != null) {
            mlisten.flag = false;
            mlisten = null;
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        Const.openOneVsMore = false;

        closeSocket();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        application.finishActivity();
        closeHttpServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.onStart();

        openNetStatusReceiver();
        openSocket();

        System.out.println("---------onResume---------");
        hideBottomUIMenu();
        mCameraSurfView.setCameraType(0);
        mCameraSurfView.openCamera();
        GPIOHelper.init();
        startService(new Intent(MainActivity.this, MainService.class));
        flag = true;
        updateView();
        Const.openOneVsMore = true;
        if (mRedInfra == null) {
            mRedInfra = new ListenRedInfra(handler);
            mRedInfra.start();
        }
        if (mlisten == null) {
            mlisten = new ListenOperation(handler);
            mlisten.start();
        }
    }

    /**
     * 注册网络监听广播
     */
    private void openNetStatusReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        if (receiver == null) {
            receiver = new NetWorkStateReceiver();
        }
        receiver.setmINetStatusListener(this);
        registerReceiver(receiver, filter);
    }

    /**
     * 网络状态改变  接口回调的数据     *
     *
     * @param state
     */
    @Override
    public void getNetState(int state) {
        if (state == 0) {
            System.out.println("conn");
            openSocket();
            openHttpServer();
        } else {
            System.out.println("dis conn");
            closeSocket();
            closeHttpServer();
        }
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.goRegisterActivity:
                //跳转到注册页面
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivityForResult(i, REGISTER_RESULT_CODE);
                break;
            default:
                break;
        }
    }


    /**
     * 打开socket连接
     */
    private void openSocket() {
        boolean conn = ConversionHelp.isNetworkConnected(mContext);
        receiver.setIs_conn(conn);
        if (!conn) {
            Toast.makeText(mContext, "没有网络,不开启socket连接", Toast.LENGTH_SHORT).show();
            LogToFile.i("MainActivity", "没有网络,不开启socket连接");
            return;
        }

        if (!SPUtil.getString(Const.KEY_VMSIP, "").equals("") && SPUtil.getInt(Const.KEY_VMSPROT, 0) != 0 && !SPUtil.getString(Const.KEY_VMSUSERNAME, "").equals("") && !SPUtil.getString(Const.KEY_VMSPASSWORD, "").equals("")) {
            //开启socket线程
            socketReconnect(SPUtil.getString(Const.KEY_VMSIP, ""), SPUtil.getInt(Const.KEY_VMSPROT, 0));

        }
    }


    /**
     * socket重连接
     *
     * @param ip
     * @param port
     */
    private void socketReconnect(String ip, int port) {
        if (socketThread == null) {
            socketThread = new SocketThread(ip, port, handler);
        } else {
            socketThread.close();
            if (heartBeatThread != null) {
                heartBeatThread.HeartBeatThread_flag = false;
                heartBeatThread = null;
            }
            socketThread = new SocketThread(ip, port, handler);
        }
        socketThread.start();
    }

    /**
     * 结束socket
     *
     * @param
     */
    public void closeSocket() {
        if (heartBeatThread != null) {
            //取消心跳
            heartBeatThread.HeartBeatThread_flag = false;
            heartBeatThread = null;
        }
        //结束socket
        if (socketThread != null) {
            socketThread.close();
            socketThread = null;
        }

    }

    /**
     * -----------------------------------------------------------------------------------------------
     */
    //上传的所有数据长度大小
    private int mSum = 0;
    //切割后的数据
    private List<File> dataList1 = null;
    private List<File> dataList2 = null;
    private List<File> dataList3 = null;
    //三个线程消息传递对应的标志为
    private int[] loadFlag = {101, 102, 103};
    private int bacthOk1, bacthOk2, bacthOk3 = 0;
    private int parts = 0;
    private ProgressBar progesss1, progesss2, progesss3;
    private TextView progesssValue1, progesssValue2, progesssValue3;
    private Dialog batchDialog;

    private List<File> getImagePathFile() {
        String strPath = Environment.getExternalStorageDirectory() + "/SocketImage/";
        File file = new File(strPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File[] mListFile = file.listFiles();
        if (mListFile.length == 0) {
            //Toast.makeText(mContext, "SocketImage文件夹下面没有图片文件", Toast.LENGTH_SHORT).show();
            return null;
        }
        List<File> mImportFile = new ArrayList<>();
        for (File file1 : mListFile) {
            if (checkIsImageFile(file1.getName())) {
                mImportFile.add(file1);
            }
        }
        //得到图片文件
        if ((mSum = mImportFile.size()) == 0) {
            //Toast.makeText(mContext, "image文件夹下面没有图片文件", Toast.LENGTH_SHORT).show();
            return null;
        }
        return mImportFile;
    }

    private void batchImport() {

        List<File> mImportFile = getImagePathFile();
        if (mImportFile == null) {
            return;
        }
//
//        //关闭相机流和其他线程
//        onPause();
//        //

        System.out.println("一共：" + mSum);
        //将文件数据分成三个集合
        cuttingList(mImportFile);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        //加载视图
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.bacthimport, null);
        //初始化数据
        progesss1 = (ProgressBar) v.findViewById(R.id.progesss1);
        progesssValue1 = v.findViewById(R.id.pro_text1);
        progesss1.setProgress(0);
        progesss2 = (ProgressBar) v.findViewById(R.id.progesss2);
        progesssValue2 = v.findViewById(R.id.pro_text2);
        progesss2.setProgress(0);
        progesss3 = (ProgressBar) v.findViewById(R.id.progesss3);
        progesssValue3 = v.findViewById(R.id.pro_text3);
        progesss3.setProgress(0);

        batchDialog = builder.create();
        batchDialog.show();
        batchDialog.getWindow().setContentView(v);
        if (parts == 1) {
            BatchImport impory = new BatchImport(dataList1, handler, loadFlag[0]);
            Thread thread = new Thread(impory);
            thread.start();
        } else if (parts == 3) {
            BatchImport impory1 = new BatchImport(dataList1, handler, loadFlag[0]);
            Thread thread1 = new Thread(impory1);
            thread1.start();

            BatchImport impory2 = new BatchImport(dataList2, handler, loadFlag[1]);
            Thread thread2 = new Thread(impory2);
            thread2.start();

            BatchImport impory3 = new BatchImport(dataList3, handler, loadFlag[2]);
            Thread thread3 = new Thread(impory3);
            thread3.start();
        }
    }


    /**
     * 检查扩展名，得到图片格式的文件
     *
     * @param fName 文件名
     * @return
     */
    private boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
                || FileEnd.equals("jpeg") || FileEnd.equals("bmp")) {
            isImageFile = true;
        } else {
            isImageFile = false;
        }
        return isImageFile;
    }

    private void cuttingList(List<File> list) {
        //我们数据之分三批
        int part = 3;
        int dataList = list.size();
        int minBatchImprot = 10;
        int pointsDataLimit = dataList % part == 0 ? dataList / part : (dataList / part) + 1;
        if (dataList > minBatchImprot) {
            parts = 3;
            System.out.println("开启三个线程");
            dataList1 = list.subList(0, pointsDataLimit);
            dataList2 = list.subList(pointsDataLimit, pointsDataLimit * 2);
            if (!list.isEmpty()) {
                dataList3 = list.subList(pointsDataLimit * 2, list.size());
            }
        } else {
            parts = 1;
            //只开启一个线程
            System.out.println("只开启一个线程");
            dataList1 = list;
        }
    }

    //________Http部分

    /**
     * 打开HTTP服务器
     */
    public void openHttpServer() {
        //开启HTTP服务
        if (serverManager != null) {
            closeHttpServer();
        }
        serverManager = new ServerManager(this);
        serverManager.register();
        serverManager.startService();

    }

    public void closeHttpServer() {
        if (serverManager != null) {
            serverManager.unRegister();
            serverManager.stopService();
            serverManager = null;
        }
    }

    /**
     * 开启HTTP服务时显示IP
     *
     * @param ip
     */
    public void httpStrat(String ip) {
        if (!TextUtils.isEmpty(ip)) {
            currIp = ip;
            showHttpUrl.setText(ip + ":8088");
            HTTP_Status = true;
        } else {
            showHttpUrl.setText("");
            HTTP_Status = false;
        }
    }

    /**
     * HTTP服务开启异常时
     *
     * @param msg
     */
    public void httpError(String msg) {
        showHttpUrl.setText(msg);
        HTTP_Status = false;
    }

    /**
     * 关闭服务
     */
    public void httpStop() {
        HTTP_Status = false;
        showHttpUrl.setText("The HTTP Server is stopped");
    }


}
