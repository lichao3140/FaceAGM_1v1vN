package com.runvision.thread;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.face.sv.FaceAngle;
import com.face.sv.FaceInfo;
import com.runvision.bean.AppData;
import com.runvision.core.Const;
import com.runvision.core.FaceIDCardCompareLib;
import com.runvision.core.LogToFile;
import com.runvision.core.MyApplication;
import com.runvision.db.User;
import com.runvision.faceagm_1v1vn.MainActivity;
import com.runvision.myview.MyCameraSuf;
import com.runvision.util.SPUtil;

/**
 * Created by Administrator on 2018/7/6.
 */

public class OneVSMoreTask extends AsyncTask<Void, Void, Void> {




    public OneVSMoreTask() {


    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //本次线程不执行完成，不允许下次进来
        Const.openOneVsMore = false;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        byte[] data = MyCameraSuf.getCameraData();
        byte[] redData = MyCameraSuf.getmRedCameraData();
        Bitmap bmp = FaceIDCardCompareLib.getInstance().getBitMap(data);
        byte[] BRG24Kj = null;
        BRG24Kj = MyApplication.mImgUtil.encodeYuv420pToBGR(data, Const.PRE_WIDTH, Const.PRE_HEIGTH);
        if (BRG24Kj == null) {
            // System.out.println("转化BRG24 error");
            Const.openOneVsMore = true;
            if (bmp != null) {
                bmp.recycle();
            }
            return null;
        }
        FaceInfo faceInfoKj = MyApplication.mDetect.faceDetectScale(BRG24Kj, Const.PRE_WIDTH, Const.PRE_HEIGTH, 0, 20);
        if (faceInfoKj.getRet() <= 0) {
            // System.out.println("单目没有人脸");
            Const.openOneVsMore = true;
            if (bmp != null) {
                bmp.recycle();
            }
            return null;
        }


        FaceAngle angle = faceInfoKj.getFacePos(0).getAngle();
        if (angle.getYaw() <= Const.ONE_VS_MORE_OFFSET && angle.getYaw() >= -Const.ONE_VS_MORE_OFFSET && angle.getPitch() <= Const.ONE_VS_MORE_OFFSET && angle.getPitch() >= -Const.ONE_VS_MORE_OFFSET) {
        } else {
            //角度过滤
            //System.out.println("角度过滤");
            Const.openOneVsMore = true;
            if (bmp != null) {
                bmp.recycle();
            }
            return null;
        }
        if (SPUtil.getBoolean(Const.KEY_ISOPENLIVE, Const.OPEN_LIVE)) {
            byte[] BRG24Kj_live = null;
            BRG24Kj_live = MyApplication.mImgUtil.encodeYuv420pToBGR(redData, Const.PRE_WIDTH, Const.PRE_HEIGTH);

            if (BRG24Kj_live == null) {
                // System.out.println("红外流转化失败");
                Const.openOneVsMore = true;
                if (bmp != null) {
                    bmp.recycle();
                }
                return null;
            }
            FaceInfo faceInfoKj_live = MyApplication.mDetect.faceDetectScale(BRG24Kj_live, Const.PRE_WIDTH, Const.PRE_HEIGTH, 0, 5);
            if (faceInfoKj_live.getRet() <= 0) {
                 System.out.println("活体没有人脸");
                Const.openOneVsMore = true;
                if (bmp != null) {
                    bmp.recycle();
                }
                return null;
            }

            int ret = MyApplication.mLive.getFaceLive(BRG24Kj, BRG24Kj_live, Const.PRE_WIDTH, Const.PRE_HEIGTH, faceInfoKj.getFacePosData(0), faceInfoKj_live.getFacePosData(0), 30);
            if (ret != 1) {
                Const.openOneVsMore = true;
                 System.out.println("不是活体");
                if (bmp != null) {
                    bmp.recycle();
                }
                return null;
            }
        }

        int result[] = MyApplication.mRecognize.recognizeFaceMore(BRG24Kj, Const.PRE_WIDTH, Const.PRE_HEIGTH, faceInfoKj.getFacePosData(0));


        Log.i("Gavin_test","result[0]:"+result[0]+"result[1]:"+result[1]);
        System.out.println(result[1]);


        if (result[1] < SPUtil.getInt(Const.KEY_ONEVSMORESCORE,Const.ONEVSMORE_SCORE) && Const.ONE_VS_MORE_TIMEOUT_NUM >= Const.ONE_VS_MORE_TIMEOUT_MAXNUM) {
            Const.ONE_VS_MORE_TIMEOUT_NUM = 0;
            AppData.getAppData().setCompareScore(result[1]);
            AppData.getAppData().setFlag(Const.ONE_VS_MORE_FAIL);
            LogToFile.e("1:N", "1:N分数:" + result[1]);

        } else if (result[1] >= SPUtil.getInt(Const.KEY_ONEVSMORESCORE,Const.ONEVSMORE_SCORE)) {
            Const.ONE_VS_MORE_TIMEOUT_NUM = 0;
            Rect rect = faceInfoKj.getFacePos(0).getFace();

            AppData.getAppData().setFaceBmp(FaceIDCardCompareLib.getInstance().getFaceImgByInfraredJpg(rect.left, rect.top, rect.right, rect.bottom, bmp));
            User user = new User();
            user.setId(result[0]);
            AppData.getAppData().setUser(user);
            AppData.getAppData().setCompareScore(result[1]);
            AppData.getAppData().setFlag(Const.ONE_VS_MORE_SUCCESS);
            System.out.println("此时oneVsMoreStatus=" + true);

        } else {
            LogToFile.e("1:N", "1:N分数:" + result[1]);
            Const.openOneVsMore = true;
            Const.ONE_VS_MORE_TIMEOUT_NUM++;
        }
        if (bmp != null) {
            bmp.recycle();
        }
        return null;
    }
}
