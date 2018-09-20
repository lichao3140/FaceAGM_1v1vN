package com.runvision.faceagm_1v1vn;

import com.runvision.bean.AppData;
import com.runvision.core.Const;

import com.runvision.core.LogToFile;

import com.runvision.core.MyApplication;
import com.runvision.db.Record;
import com.runvision.db.User;
import com.runvision.gpio.GPIOHelper;

import com.runvision.myview.MyCameraSuf;
import com.runvision.thread.ListenOperation;
import com.runvision.thread.ListenRedInfra;
import com.runvision.util.DateTimeUtils;
import com.runvision.util.FileUtils;
import com.runvision.util.IDUtils;
import com.runvision.util.SPUtil;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import android_serialport_api.SerialPort;

public class MainActivity extends Activity {
    private String TAG = "MainActivity";
    private Context mContext;// 上下文
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
    //跳转去注册
    private ImageView goRegister;
    //1：N的UI
    private View oneVsMoreView;
    private ImageView oneVsMore_face, oneVsMore_temper;
    private TextView oneVsMore_userName, oneVsMore_userID, oneVsMore_userType;
    private View ts_xml;
    //这个按钮是设置或以开关的
    private ImageView openSetSorce;


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
//                    //重置两个开关  防止无法打开1：N的线程
//                    Const.openOneVsMore = true;
//                    oneVsMoreStatus = false;

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
//
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
//        application = (MyApplication) getApplication();
//        application.init();
//        application.addActivity(this);
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

    private static final int REGISTER_RESULT_CODE = 1;

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
        version.setText("G69A_SN:" + getVersion(mContext));
        goRegister = findViewById(R.id.goRegister);
        goRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                Const.openOneVsMore = false;
                startActivityForResult(new Intent(MainActivity.this, RegisterActivity.class), REGISTER_RESULT_CODE);


            }
        });
        oneVsMoreView = findViewById(R.id.onevsmore);
        oneVsMore_face = oneVsMoreView.findViewById(R.id.onevsmore_face);
        oneVsMore_temper = oneVsMoreView.findViewById(R.id.onevsmore_temper);
        oneVsMore_userName = oneVsMoreView.findViewById(R.id.onevsmore_userName);
        oneVsMore_userID = oneVsMoreView.findViewById(R.id.onevsmore_userID);
        oneVsMore_userType = oneVsMoreView.findViewById(R.id.onevsmore_userType);
        ts_xml = findViewById(R.id.ts_xml);

        openSetSorce = findViewById(R.id.goSetting);
        openSetSorce.setOnClickListener(openLivingClick);
    }

    private Dialog liveDialog = null;
    private View.OnClickListener openLivingClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setCancelable(false);
            builder.setTitle("设置比对阀值");
            //加载视图
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View v = inflater.inflate(R.layout.opensetsorce, null);

            final EditText setforcard = v.findViewById(R.id.setforcard);
            final EditText setforonevsmore = v.findViewById(R.id.setforonevsmore);
            setforcard.setText(SPUtil.getInt("cardScore", Const.FACE_SCORE)+"");
            setforonevsmore.setText(SPUtil.getInt("oneVsMoreScore", Const.ONEVSMORE_SCORE)+"");
            builder.setView(v);
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    hideBottomUIMenu();
                    liveDialog.dismiss();
                }
            });
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (liveDialog != null) {
                        //做保存操作
                        String cardScore = setforcard.getText().toString().trim();
                        String oneVsMoreScore = setforonevsmore.getText().toString().trim();
                        //
                        if (!cardScore.equals("") && !oneVsMoreScore.equals("")) {
                            SPUtil.putInt("cardScore",Integer.parseInt(cardScore));
                            SPUtil.putInt("oneVsMoreScore",Integer.parseInt(oneVsMoreScore));
                            hideBottomUIMenu();
                            liveDialog.dismiss();
                        } else {
                            Toast.makeText(mContext, "请填写阀值", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });
            liveDialog = builder.create();
            liveDialog.show();

        }
    };

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

        mlisten.flag = false;
        mlisten = null;

        // 关闭相机
        mCameraSurfView.releaseCamera();

        if (mPlayer != null) {
            mPlayer.release();
        }
    }

    //显示1比N比对信息
    private void showOneVSMore() {
        if (AppData.getAppData().getCompareScore() >= SPUtil.getInt("oneVsMoreScore", Const.ONEVSMORE_SCORE)) {
            oneVsMore_face.setImageBitmap(AppData.getAppData().getFaceBmp());
            String snapImageID = IDUtils.genImageName();
            FileUtils.saveFile(AppData.getAppData().getFaceBmp(), snapImageID, Const.SNAP_DIR);
            User user = MyApplication.faceProvider.getUserByUserId(AppData.getAppData().getUser().getId());
            String sdCardDir = Environment.getExternalStorageDirectory() + "/FaceAndroid/FaceTemplate/" + user.getImagepath() + ".jpg";
            try {
                Bitmap bmp = BitmapFactory.decodeFile(sdCardDir);
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
            }, 1000);

            oneVsMore_userName.setText(user.getName());
            oneVsMore_userType.setText(user.getType());
            oneVsMore_userID.setText(user.getUserid());
            LogToFile.e("1:N", "1:N成功: 姓名：" + user.getName() + ",分数：" + AppData.getAppData().getCompareScore());

            Record record = new Record(user.getName(), user.getUserid(), user.getType(), DateTimeUtils.parseDataTimeToFormatString(new Date()), AppData.getAppData().getCompareScore() + "", "通过", snapImageID, user.getImagepath());

            MyApplication.faceProvider.addRecord(record);


            oneVsMoreView.setVisibility(View.VISIBLE);
            playMusic(R.raw.success);
            handler.postDelayed(oneVsMoreRunnable, 2000);

        } else {
            playMusic(R.raw.burlcard);
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

            System.out.println("1:N  finash");
        }
    };
    public Runnable closeTsRunnable = new Runnable() {
        @Override
        public void run() {
            ts_xml.setVisibility(View.GONE);
            //此处1:N 完成
            Const.openOneVsMore = true;
            // System.out.println("1:N  finash");
        }
    };


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

        } else if (AppData.getAppData().getCompareScore() < SPUtil.getInt("cardScore", Const.FACE_SCORE)) {

            isSuccessComper.setImageResource(R.mipmap.icon_sb);
            playMusic(R.raw.error);
            str = "失败";

            //保存抓拍图片
            String snapImageID = IDUtils.genImageName();
            FileUtils.saveFile(AppData.getAppData().getFaceBmp(), snapImageID, Const.SNAP_DIR);
            //保存身份证图片
            String cardImageID = snapImageID + "_card";
            FileUtils.saveFile(AppData.getAppData().getCardBmp(), cardImageID, Const.CARD_DIR);

            Record record = new Record(AppData.getAppData().getName(), "无", "访客", DateTimeUtils.parseDataTimeToFormatString(new Date()),
                    AppData.getAppData().getCompareScore() + "", "失败", snapImageID, cardImageID);
            MyApplication.faceProvider.addRecord(record);

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

            //保存抓拍图片
            String snapImageID = IDUtils.genImageName();
            FileUtils.saveFile(AppData.getAppData().getFaceBmp(), snapImageID, Const.SNAP_DIR);
            //保存身份证图片
            String cardImageID = snapImageID + "_card";
            FileUtils.saveFile(AppData.getAppData().getCardBmp(), cardImageID, Const.CARD_DIR);

            Record record = new Record(AppData.getAppData().getName(), "无", "访客", DateTimeUtils.parseDataTimeToFormatString(new Date()),
                    AppData.getAppData().getCompareScore() + "", "通过", snapImageID, cardImageID);
            MyApplication.faceProvider.addRecord(record);
        }
        alert.setVisibility(View.VISIBLE);
        LogToFile.e("比对记录", AppData.getAppData().getName() + "_" + AppData.getAppData().getSex() + "_" + AppData.getAppData().getCardNo().substring(0, 4) + "************"
                + AppData.getAppData().getCardNo().substring(16, 18) + "_比对结果:" + str + ",比对分数:" + AppData.getAppData().getCompareScore());


        handler.postDelayed(runnable, 3000);
        handler.postDelayed(mResetRunnble, 3000);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println(requestCode + "," + resultCode);
        if (requestCode == REGISTER_RESULT_CODE) {

            mCameraSurfView.setCameraType(0);
            mCameraSurfView.openCamera();


            System.out.println("dsasd");

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
            handler.sendEmptyMessage(Const.RED_INFRA);
        }

    }

}
