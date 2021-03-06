package com.runvision.thread;


import com.face.sv.FaceAngle;
import com.face.sv.FaceInfo;
import com.runvision.bean.AppData;
import com.runvision.core.Const;
import com.runvision.core.FaceIDCardCompareLib;
import com.runvision.core.MyApplication;
import com.runvision.myview.MyCameraSuf;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;


public class PreviewTask extends AsyncTask<Void, Rect, Void> {
    private MyCameraSuf myCameraSuf;
    FaceInfo faceInfoKj = null;
    byte[] data;

    public PreviewTask(MyCameraSuf myCameraSuf) {
        this.myCameraSuf = myCameraSuf;
        data = MyCameraSuf.getCameraData();
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        byte[] BRG24Kj = null;
        BRG24Kj = MyApplication.mImgUtil.encodeYuv420pToBGR(data,Const.PRE_HEIGTH, Const.PRE_WIDTH);
        if (BRG24Kj == null) {
            Log.e("人脸框", "转化BRG24 error");
            return null;
        }
        faceInfoKj = MyApplication.mDetect.faceDetectScale(BRG24Kj, Const.PRE_HEIGTH, Const.PRE_WIDTH, 0, 20);
        if (faceInfoKj.getRet() > 0) {
            Rect rect = faceInfoKj.getFacePos(0).getFace();
            publishProgress(rect);
        } else {
            Log.e("人脸框", "无人脸");
            faceInfoKj = null;
            publishProgress(new Rect(0, 0, 0, 0));
        }


        return null;
    }


    @Override
    protected void onProgressUpdate(Rect... values) {
        super.onProgressUpdate(values);
        myCameraSuf.setFacePamaer(values[0]);
        if (myCameraSuf.getCamerType() == 1 && faceInfoKj != null && faceInfoKj.getRet() > 0 && Const.is_regFace) {
            FaceAngle angle = faceInfoKj.getFacePos(0).getAngle();
            if (angle.getYaw() <= Const.OFFSET && angle.getYaw() >= -Const.OFFSET && angle.getPitch() <= Const.OFFSET && angle.getPitch() >= -Const.OFFSET) {
                System.out.println("----2-----");
                Bitmap map = FaceIDCardCompareLib.getInstance().getBitMap(data);
                AppData.getAppData().setFaceBmp(FaceIDCardCompareLib.getInstance().getFaceImgByInfraredJpg(values[0].left, values[0].top, values[0].right, values[0].bottom, map));
                AppData.getAppData().setFlag(Const.REG_FACE);
                Const.is_regFace = false;
            }
        }

    }

    @Override
    protected void onPostExecute(Void result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
    }

}
