package com.runvision.frament;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
    private TextView name, phone;
    private MyCameraSuf reg_MyCameraSuf;
    private Spinner type;
    private boolean flag = true;
    private Bitmap reg_bmp = null;
    private String choose_type;

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

    @Override
    public void onPause() {
        super.onPause();
        close();
    }

    private void initView() {
        btn_openCamera = (Button) view.findViewById(R.id.btn_openPhone);
        btn_openCamera.setOnClickListener(this);
        imageView = (ImageView) view.findViewById(R.id.choose_bitmap);
        btn_startImport = view.findViewById(R.id.btn_startImport);
        btn_startImport.setOnClickListener(this);
        reg_chooseOneImage = (LinearLayout) view.findViewById(R.id.reg_chooseOneImage);
        name = (TextView) view.findViewById(R.id.reg_name);
        phone = (TextView) view.findViewById(R.id.reg_phone);
        type = (Spinner) view.findViewById(R.id.reg_type);
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
        addFace = (Button) view.findViewById(R.id.reg_addFace);
        addFace.setOnClickListener(this);
        reg_MyCameraSuf = (MyCameraSuf) view.findViewById(R.id.reg_myCameraSuf);
        reg_MyCameraSuf.setCameraType(1);
        reg_MyCameraSuf.openCamera();
        btn_close = view.findViewById(R.id.reg_close);
        btn_close.setOnClickListener(this);
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
            case R.id.btn_startImport:
                showConfirmDialog();
                break;
            case R.id.reg_close:

                getActivity().finish();
                break;
            default:
                break;
        }
    }


    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("提示");
        builder.setMessage("是否需要继续导入模版，确认后无法停止");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startTime = System.currentTimeMillis();
                batchImport();
                ConfirmDialog.dismiss();
            }
        });
        builder.setNegativeButton("否", null);
        ConfirmDialog = builder.create();
        ConfirmDialog.show();
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
                case 101:
                    int success1 = (int) msg.obj;
                    bacthOk1 = success1;
                    // System.out.println("线程一" + success1 + "完成数目");
                    double a = (double) success1 / (double) dataList1.size();
                    int b = (int) (a * 100);
                    progesssValue1.setText(success1 + "/" + dataList1.size());
                    progesss1.setProgress(b);
                    if (bacthOk1 + bacthOk2 + bacthOk3 == mSum) {
                        batchDialog.dismiss();
                        open();
                        mImportFinish();
                    }
                    break;
                case 102:
                    int success2 = (int) msg.obj;
                    bacthOk2 = success2;
                    //  System.out.println("线程二" + success2 + "完成数目");
                    double a2 = (double) success2 / (double) dataList2.size();
                    int b2 = (int) (a2 * 100);
                    progesssValue2.setText(success2 + "/" + dataList2.size());
                    progesss2.setProgress(b2);
                    if (bacthOk1 + bacthOk2 + bacthOk3 == mSum) {
                        batchDialog.dismiss();
                        open();
                        mImportFinish();
                    }
                    break;
                case 103:
                    int success3 = (int) msg.obj;
                    bacthOk3 = success3;
                    // System.out.println("线程三" + success3 + "完成数目");
                    double a3 = (double) success3 / (double) dataList3.size();
                    int b3 = (int) (a3 * 100);
                    progesssValue3.setText(success3 + "/" + dataList3.size());
                    progesss3.setProgress(b3);
                    if (bacthOk1 + bacthOk2 + bacthOk3 == mSum) {
                        batchDialog.dismiss();
                        open();
                        mImportFinish();
                    }
                    break;
                default:
                    break;
            }

        }
    };

    private void mImportFinish() {
        endTime = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        String str1 = formatter.format(calendar.getTime());
        calendar.setTimeInMillis(endTime);
        String str2 = formatter.format(calendar.getTime());
        System.out.println("str1:" + str1);
        System.out.println("str2:" + str2);

        String useTime = DateTimeUtils.getDistanceTime(str1, str2);
        System.out.println("use:" + useTime);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("提示");
        int error = 0;
        List list = getImagePathFile();
        if (list == null) {
            error = 0;
        } else {
            error = list.size();
        }

        builder.setMessage("批量导入结束,本地一共导入" + mSum + "个模版,失败" + error + "个，用时:" + useTime);
        builder.setNegativeButton("确定", null);
        builder.create().show();


    }


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
        String userPhone = phone.getText().toString().trim();
        if (userName.equals("")) {
            Toast.makeText(mContext, "请输入名字", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userPhone.equals("")) {
            Toast.makeText(mContext, "请输入工号", Toast.LENGTH_SHORT).show();
            return;
        }
        String time = DateTimeUtils.parseDataTimeToFormatString(new Date());
        //保存图片
        //生成随机图片ID
        String imageID = IDUtils.genImageName();
        FileUtils.saveFile(reg_bmp, imageID, "FaceTemplate");
        //封装pojo
        User user = new User(userName, userPhone, choose_type, time, imageID);
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
    private long startTime = 0;
    private long endTime = 0;


    private List<File> getImagePathFile() {
        String strPath = Environment.getExternalStorageDirectory() + "/Image/";
        File file = new File(strPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File[] mListFile = file.listFiles();
        if (mListFile.length == 0) {
            Toast.makeText(mContext, "image文件夹下面没有图片文件", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(mContext, "image文件夹下面没有图片文件", Toast.LENGTH_SHORT).show();
            return null;
        }
        return mImportFile;
    }

    private void batchImport() {

        List<File> mImportFile = getImagePathFile();
        if (mImportFile == null) {
            return;
        }

        //关闭相机流和其他线程
        close();

        System.out.println("一共：" + mSum);
        //将文件数据分成三个集合
        cuttingList(mImportFile);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        //加载视图
        LayoutInflater inflater = LayoutInflater.from(getContext());
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


//        System.out.println(dataList1);
//        System.out.println(dataList3);
//        System.out.println(dataList2);


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

    @Override
    public void onDestroy() {
        super.onDestroy();
        close();
        System.out.println("---onDestroy--");
        RefWatcher refWatcher = MyApplication.getRefWatcher(getContext());
        refWatcher.watch(this);
    }
}
