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
    public ListenRedInfra(Handler handler){
        this.handler=handler;
    }
    private int frist = -1;

    @Override
    public void run() {
        super.run();
        while (red_infra_flag) {
            int a = GPIOHelper.readStatus();
            Log.d("ListenRedInfra", "红外状态:" + a + ",上一次：" + frist);
            if (a == 1) {
                if (frist == 0) {
//                    SerialPort.openLED();
                    handler.sendEmptyMessage(Const.RED_INFRA);
                }
                try {
                    Thread.sleep(4 * 1000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    Thread.sleep(1 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            frist = a;

        }

    }
}
