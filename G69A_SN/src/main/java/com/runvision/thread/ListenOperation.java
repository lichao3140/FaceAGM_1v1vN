package com.runvision.thread;

import android.os.Handler;

import com.runvision.bean.AppData;
import com.runvision.core.Const;

public class ListenOperation extends Thread {
    private static int num = 0;
    public boolean flag = true;

    public static void cleanTime() {
        num = 0;
    }

    private Handler handler;

    public ListenOperation(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();

        while (flag) {
            num++;
            if (num == Const.CLOSE_HOME_TIMEOUT) {
                handler.sendEmptyMessage(Const.CLOSE_HOME);
//                AppData.getAppData().setFlag(Const.CLOSE_HOME);
//                cleanTime();
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
