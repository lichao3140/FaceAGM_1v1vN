package com.runvision.thread;


import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;

import com.face.sv.FaceAngle;
import com.face.sv.FaceInfo;
import com.runvision.bean.AppData;
import com.runvision.core.Const;
import com.runvision.core.FaceIDCardCompareLib;
import com.runvision.core.MyApplication;
import com.runvision.myview.MyCameraSuf;


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
        BRG24Kj = MyApplication.mImgUtil.encodeYuv420pToBGR(data, Const.PRE_WIDTH, Const.PRE_HEIGTH);
        if (BRG24Kj == null) {
            System.out.println("转化BRG24 error");
            return null;
        }
        faceInfoKj = MyApplication.mDetect.getFacePositionScaleFromGray(BRG24Kj, Const.PRE_WIDTH, Const.PRE_HEIGTH, 20);
        if (faceInfoKj.getRet() > 0) {
            Rect rect = faceInfoKj.getFacePos(0).getFace();
            publishProgress(rect);
        } else {
            faceInfoKj = null;
            publishProgress(new Rect(0, 0, 0, 0));
        }


        return null;
    }


    @Override
    protected void onProgressUpdate(Rect... values) {
        super.onProgressUpdate(values);
        myCameraSuf.setFacePamaer(values[0]);


    }

    @Override
    protected void onPostExecute(Void result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
    }

}
