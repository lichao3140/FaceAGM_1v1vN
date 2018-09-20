package com.runvision.core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import com.runvision.faceagm_1v1vn.MainActivity;


/**
 * Created by Administrator on 2016/10/26.
 *
 * @auther madreain
 */

public class UnCeHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    public static final String TAG = "MyApplication";
    MyApplication application;

    public UnCeHandler(MyApplication application) {

        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.application = application;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {

            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }
            Intent intent = new Intent(application.getApplicationContext(),
                    MainActivity.class);
            PendingIntent restartIntent = PendingIntent.getActivity(
                    application.getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);

            AlarmManager mgr = (AlarmManager) application
                    .getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                    restartIntent);

            application.finishActivity();

        }
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        Log.e(TAG, "handleException: "+ex.getMessage());
        LogToFile.e(TAG,ex.getMessage());
        LogToFile.e(TAG,ex.toString());
        LogToFile.e(TAG,ex.getLocalizedMessage());
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Looper.loop();
            }
        }.start();
        return true;
    }
}