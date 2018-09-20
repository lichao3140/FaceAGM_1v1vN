package com.runvision.thread;

import android.graphics.Bitmap;

import com.runvision.bean.AppData;
import com.runvision.core.Const;
import com.runvision.core.FaceIDCardCompareLib;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

/**
 * Created by Administrator on 2018/7/5.
 */

public class ComperThread implements Runnable {
    private Bitmap cardBmp;
    private IDCardInfo idCardInfo;
    public boolean Comper_status=false;
    public ComperThread(Bitmap cardBmp, IDCardInfo idCardInfo) {
        this.cardBmp = cardBmp;
        this.idCardInfo = idCardInfo;
    }

    @Override
    public void run() {
        Comper_status=true;
        long satrt = System.currentTimeMillis();
        int sorce = FaceIDCardCompareLib.getInstance().faceComperFrame(cardBmp);
        System.out.println("socre:"+sorce);
        System.out.println("用时：" + (System.currentTimeMillis() - satrt));
        AppData.getAppData().setName(idCardInfo.getName());
        AppData.getAppData().setSex(idCardInfo.getSex());
        AppData.getAppData().setNation(idCardInfo.getNation());
        AppData.getAppData().setBirthday(idCardInfo.getBirth());
        AppData.getAppData().setAddress(idCardInfo.getAddress());
        AppData.getAppData().setCardNo(idCardInfo.getId());
        AppData.getAppData().setCompareScore(sorce);
        // 更改标志位 让Activity修改UI
        AppData.getAppData().setFlag(Const.COMPER_FINISH_FLAG);
        Comper_status=false;
    }
}
