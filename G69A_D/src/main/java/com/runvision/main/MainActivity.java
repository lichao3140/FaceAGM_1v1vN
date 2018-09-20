package com.runvision.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.runvision.bean.AppData;
import com.runvision.core.Const;
import com.runvision.core.LogToFile;
import com.runvision.core.MyApplication;

import com.runvision.gpio.GPIOHelper;
import com.runvision.myview.MyCameraSuf;
import com.runvision.thread.ListenOperation;
import com.runvision.thread.ListenRedInfra;


import android_serialport_api.SerialPort;

public class MainActivity extends Activity {
    private String TAG = "MainActivity";
    private Context mContext;// 上下文
    public static boolean isInitSdk = false;// 是否初始化算法成功

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
    private ImageView home_set;
    private FrameLayout index;

    private RelativeLayout alertView;
    private MediaPlayer mPlayer;
    private ListenRedInfra mRedInfra;


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

                    alert.setVisibility(View.GONE);


                    break;

                case Const.CLOSE_HOME:
                    home.setVisibility(View.VISIBLE);
                    index.setVisibility(View.GONE);
                    mCameraSurfView.releaseCamera();
                    pro_xml.setVisibility(View.GONE);
                    ListenOperation.cleanTime();
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
                    ListenOperation.cleanTime();//
                    break;
                case Const.READ_CARD:
                    pro_xml.setVisibility(View.VISIBLE);
                    playMusic(R.raw.lookcamera);
                    break;
                default:
                    break;
            }
        }

        ;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//
        // 横屏
        // 全屏代码
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideBottomUIMenu();
        mContext = this;
        //
        application = (MyApplication) getApplication();
        application.init();
        application.addActivity(this);
        //
        initView();
        startService(new Intent(MainActivity.this, MainService.class));
        updateView();
        GPIOHelper.init();

        mlisten = new ListenOperation(handler);
        mlisten.start();
//        // 红外线程
        mRedInfra = new ListenRedInfra(handler);
        mRedInfra.start();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        hideBottomUIMenu();


    }


    private void initView() {
        mCameraSurfView = (MyCameraSuf) findViewById(R.id.myCameraView);
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
        version.setText("G69A_D:" + getVersion(mContext));
    }

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

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // 关闭服务
        stopService(new Intent(MainActivity.this, MainService.class));

        // 取消循环监听标志位的线程
        flag = false;
//
//        // 取消两个线程的循环
//        mlisten.flag = false;
        mRedInfra.red_infra_flag = false;
        mRedInfra = null;

        // 关闭相机
        mCameraSurfView.releaseCamera();

        if (mPlayer != null) {
            mPlayer.release();
        }
    }


    // 显示比对信息框
    private void showAlertDialog() {
        System.out.println("我是比对分数:" + AppData.getAppData().getCompareScore() + "," + Const.FACE_SCORE);
        String str = "";
        cardBmp_view.setImageBitmap(AppData.getAppData().getCardBmp());
        faceBmp_view.setImageBitmap(AppData.getAppData().getFaceBmp());
        idcard_Bmp.setImageBitmap(AppData.getAppData().getCardBmp());
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
            faceBmp_view.setImageResource(R.mipmap.tx);
            faceBmp_view.setScaleType(ImageView.ScaleType.FIT_XY);
            playMusic(R.raw.error);

        } else if (AppData.getAppData().getCompareScore() < Const.FACE_SCORE) {

            isSuccessComper.setImageResource(R.mipmap.icon_sb);
            playMusic(R.raw.error);
            str = "失败";
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
            }, 1000);
        }
        alert.setVisibility(View.VISIBLE);
        LogToFile.e("比对记录", AppData.getAppData().getName() + "_" + AppData.getAppData().getSex() + "_" + AppData.getAppData().getCardNo().substring(0, 4) + "************"
                + AppData.getAppData().getCardNo().substring(16, 18) + "_比对结果:" + str + ",比对分数:" + AppData.getAppData().getCompareScore());


        handler.postDelayed(runnable, 3000);

    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            alert.setVisibility(View.GONE);
            System.out.println("1:1  finash");

        }
    };

    public void playMusic(int msuicID) {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.release();
            }
        }
        mPlayer = MediaPlayer.create(mContext, msuicID);
        mPlayer.start();
    }

}
