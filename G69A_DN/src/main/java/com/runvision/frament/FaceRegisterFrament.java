package com.runvision.frament;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.face.sv.FaceInfo;
import com.runvision.bean.AppData;
import com.runvision.core.Const;
import com.runvision.core.FaceIDCardCompareLib;
import com.runvision.core.MyApplication;
import com.runvision.db.User;
import com.runvision.faceagm_1v1vn.R;

import com.runvision.myview.MyCameraSuf;
import com.runvision.thread.BatchImport;
import com.runvision.thread.OneVSMoreTask;
import com.runvision.util.DateTimeUtils;
import com.runvision.util.FileUtils;
import com.runvision.util.IDUtils;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/7/9.
 */

public class FaceRegisterFrament extends Fragment implements View.OnClickListener {
    private View view;
    private Context mContext;
    private String TAG = this.getClass().getSimpleName();
    private Button btn_openCamera, addFace, btn_startImport, btn_close;
    private ImageView imageView;
    private LinearLayout reg_chooseOneImage;
    private EditText name, workNo, cardNo, age;
    private MyCameraSuf reg_MyCameraSuf;
    private Spinner type, sex;

    private boolean flag = true;
    private Bitmap reg_bmp = null;
    private String choose_type;
    private String choose_sex;
    private View userInfoView;

    private Dialog ConfirmDialog;
    private ProgressBar progesss1, progesss2, progesss3;
    private TextView progesssValue1, progesssValue2, progesssValue3;
    private Dialog batchDialog;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        if (null == view) {
            view = inflater.inflate(R.layout.faceregisterframent, container, false);
            initView();
        }
        updateView();
        return view;
    }


    private void initView() {
        btn_openCamera = (Button) view.findViewById(R.id.btn_openPhone);
        btn_openCamera.setOnClickListener(this);
        imageView = (ImageView) view.findViewById(R.id.choose_bitmap);

        reg_chooseOneImage = (LinearLayout) view.findViewById(R.id.reg_chooseOneImage);


        name = view.findViewById(R.id.reg_name);
        workNo = view.findViewById(R.id.reg_phone);
        cardNo = view.findViewById(R.id.cardNo);
        age = view.findViewById(R.id.reg_age);
        type = view.findViewById(R.id.reg_type);
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String cardNumber = FaceRegisterFrament.this.getResources().getStringArray(R.array.user_type)[i];
                System.out.println(cardNumber);
                choose_type = cardNumber;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sex = view.findViewById(R.id.reg_sex);
        sex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                choose_sex = FaceRegisterFrament.this.getResources().getStringArray(R.array.user_sex)[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        addFace = (Button) view.findViewById(R.id.reg_addFace);
        addFace.setOnClickListener(this);
        reg_MyCameraSuf = (MyCameraSuf) view.findViewById(R.id.reg_myCameraSuf);
        reg_MyCameraSuf.setCameraType(1);
        reg_MyCameraSuf.openCamera();
        btn_close = view.findViewById(R.id.reg_close);
        btn_close.setOnClickListener(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i(TAG, "onHiddenChanged: " + hidden);
        if (hidden) {
            close();
        } else {
            open();
        }
    }

    public void open() {
        flag = true;
        updateView();
        reg_MyCameraSuf.setCameraType(1);
        reg_MyCameraSuf.openCamera();
    }

    public void close() {
        System.out.println("close");
        reg_MyCameraSuf.releaseCamera();
        flag = false;
        Const.is_regFace = false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_openPhone:
                openCamera();
                break;

            case R.id.reg_addFace:
                addFace();
                break;
            case R.id.reg_close:
                getActivity().finish();
                break;
            default:
                break;
        }
    }





    private void openCamera() {
        if (!Const.is_regFace) {
            Const.is_regFace = true;
            btn_openCamera.setText("正在抓拍,请正视相机");
            btn_openCamera.setBackgroundColor(Color.GREEN);
        } else {
            Const.is_regFace = false;
            btn_openCamera.setText("开始抓拍");
            btn_openCamera.setBackgroundColor(Color.parseColor("#cccccc"));
        }

    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Const.REG_FACE:
                    System.out.println("-----------3");
                    btn_openCamera.setText("开始抓拍");
                    btn_openCamera.setBackgroundColor(Color.parseColor("#cccccc"));
                    reg_bmp = AppData.getAppData().getFaceBmp();
                    AppData.getAppData().setFaceBmp(null);
                    imageView.setImageBitmap(reg_bmp);
                    break;
                default:
                    break;
            }

        }
    };




    public void updateView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag) {
                    if (AppData.getAppData().getFlag() == Const.FLAG_CLEAN) {
                        continue;
                    }
                    handler.sendEmptyMessage(AppData.getAppData().getFlag());
                    AppData.getAppData().setFlag(Const.FLAG_CLEAN);

                }
            }
        }).start();
    }


    private void addFace() {
        if (reg_bmp == null) {
            Toast.makeText(mContext, "图片不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String userName = name.getText().toString().trim();
        String userWorkNo = workNo.getText().toString().trim();
        String userCardNo = cardNo.getText().toString().trim();
        String userAge = age.getText().toString().trim();
        if (userName.equals("")) {
            Toast.makeText(mContext, "请输入名字", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userWorkNo.equals("")) {
            Toast.makeText(mContext, "请输入工号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userCardNo.equals("")) {
            Toast.makeText(mContext, "请输入证件号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userAge.equals("")) {
            Toast.makeText(mContext, "请输入年龄", Toast.LENGTH_SHORT).show();
            return;
        }

        List<User> mList = MyApplication.faceProvider.queryUsers("select * from tUser where cardNo='" + userCardNo+"'");
        if (mList.size() > 0) {
            Toast.makeText(mContext, "此人的模版已存在，请勿重复添加", Toast.LENGTH_SHORT).show();
            return;
        }
        //保存图片
        //生成随机图片ID
        String imageID = IDUtils.genImageName();
        FileUtils.saveFile(reg_bmp, imageID, Const.TEMP_DIR);
        //封装pojo
        User user = new User(userName, choose_type, choose_sex, Integer.parseInt(userAge), userWorkNo, userCardNo, imageID, DateTimeUtils.getTime());
        //添加
        int id = MyApplication.faceProvider.addUserOutId(user);
        System.out.println("add id:" + id);
        byte[] BRG24Kj = null;
        BRG24Kj = FaceIDCardCompareLib.getInstance().bitmapToBGR24(reg_bmp);
        if (BRG24Kj == null) {
            MyApplication.faceProvider.deleteUserById(id);
            FileUtils.deleteTempter(imageID, "FaceTemplate");
            Toast.makeText(mContext, "添加失败", Toast.LENGTH_SHORT).show();
            return;
        }
        FaceInfo faceInfoKj = MyApplication.mDetect.getFacePositionScaleFromGray(BRG24Kj, reg_bmp.getWidth(), reg_bmp.getHeight(), 5);
        if (faceInfoKj.getRet() != 1) {
            MyApplication.faceProvider.deleteUserById(id);
            FileUtils.deleteTempter(imageID, "FaceTemplate");
            Toast.makeText(mContext, "添加失败:" + faceInfoKj.getRet(), Toast.LENGTH_SHORT).show();
            return;
        }
        int ret = MyApplication.mRecognize.registerFaceFeature(id, BRG24Kj, reg_bmp.getWidth(), reg_bmp.getHeight(), faceInfoKj.getFacePosData(0));
        if (ret > 0) {
            Toast.makeText(mContext, "添加成功", Toast.LENGTH_SHORT).show();
        } else {
            MyApplication.faceProvider.deleteUserById(id);
            FileUtils.deleteTempter(imageID, "FaceTemplate");
            Toast.makeText(mContext, "添加失败" + ret, Toast.LENGTH_SHORT).show();
            return;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MyApplication.getRefWatcher(getContext());
        refWatcher.watch(this);
    }
}
