package com.runvision.thread;

import android.os.Handler;
import android.util.Log;

import com.runvision.bean.AppData;
import com.runvision.core.Const;
import com.runvision.gpio.GPIOHelper;

import android_serialport_api.SerialPort;

/**
 * Created by Administrator on 2018/6/6.
 */

public class ListenRedInfra extends Thread {
    public boolean red_infra_flag = true;
    private Handler handler;
    private int mRedStatus;

    public int getRedStatus() {
        return mRedStatus;
    }

    public ListenRedInfra(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        super.run();
        while (red_infra_flag) {

            mRedStatus = GPIOHelper.readStatus();
            Log.e(this.getClass().getSimpleName(), "当前红外状态：" + (mRedStatus == 1 ? "有人" : "没有人"));
            if (mRedStatus == 1) {
                handler.sendEmptyMessage(Const.RED_INFRA);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}
