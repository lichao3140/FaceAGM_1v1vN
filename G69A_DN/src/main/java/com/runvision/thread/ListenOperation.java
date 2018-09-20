package com.runvision.thread;

import android.os.Handler;


import com.runvision.core.Const;
import com.runvision.util.SPUtil;

/**
 * @author Administrator
 */
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
            if (num == SPUtil.getInt(Const.KEY_BACKHOME, Const.CLOSE_HOME_TIMEOUT)) {
                handler.sendEmptyMessage(Const.CLOSE_HOME);
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
