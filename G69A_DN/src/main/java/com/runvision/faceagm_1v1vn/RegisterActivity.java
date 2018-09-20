package com.runvision.faceagm_1v1vn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.runvision.core.MyApplication;
import com.runvision.frament.AppConfigFrament;
import com.runvision.frament.FaceRegisterFrament;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by Administrator on 2018/7/9.
 */

public class RegisterActivity extends FragmentActivity implements View.OnClickListener {
    private TextView showRegFrameLayout, showAppConfigFrameLayput;
    private FrameLayout id_content;
    private Context mContext;
    private int currentItem = 0;
    private MyApplication application;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideBottomUIMenu();
        mContext = this;

        application = (MyApplication) getApplication();
        application.init();
        application.addActivity(this);

        initView();
        initViewPage(0);

    }

    private void initView() {
        showRegFrameLayout = findViewById(R.id.showRegFrameLayout);

        showAppConfigFrameLayput = findViewById(R.id.showAppConfigFrameLayout);
        id_content = findViewById(R.id.id_content);
        showRegFrameLayout.setOnClickListener(this);
        showAppConfigFrameLayput.setOnClickListener(this);
    }


    /**
     * 隐藏虚拟按键，并且全屏
     */
    @SuppressLint("NewApi")
    protected void hideBottomUIMenu() {
        // 隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            // for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.showRegFrameLayout:
                if (currentItem != 0) {
                    showRegFrameLayout.setBackgroundResource(R.color.gray);
                    showAppConfigFrameLayput.setBackgroundResource(R.color.white);
                    initViewPage(0);
                    currentItem = 0;
                }

                break;
            case R.id.showAppConfigFrameLayout:
                showRegFrameLayout.setBackgroundResource(R.color.white);

                showAppConfigFrameLayput.setBackgroundResource(R.color.gray);

                initViewPage(1);
                currentItem = 1;
                break;
            default:
                break;
        }
    }

    private FaceRegisterFrament faceRegister;

    private AppConfigFrament appConfigFrament;

    private void initViewPage(int chooseIndex) {
        FragmentManager fm = getSupportFragmentManager();
        // 创建一个事务
        FragmentTransaction transaction = fm.beginTransaction();


        // 我们先把所有的Fragment隐藏了，然后下面再开始处理具体要显示的Fragment
        hideFragment(transaction);
        switch (chooseIndex) {
            case 0:
                if (faceRegister == null) {
                    faceRegister = new FaceRegisterFrament();
                    transaction.add(R.id.id_content, faceRegister);
                } else {
                    transaction.show(faceRegister);
                }
                break;
            case 1:
                if (appConfigFrament == null) {
                    appConfigFrament = new AppConfigFrament();
                    transaction.add(R.id.id_content, appConfigFrament);
                } else {
                    transaction.show(appConfigFrament);
                }
                break;
            default:
                break;
        }
        transaction.commit();// 提交事务
    }

    /**
     * 隐藏所有的Fragment     *
     *
     * @param transaction
     */
    private void hideFragment(FragmentTransaction transaction) {
        if (faceRegister != null) {
            transaction.hide(faceRegister);
        }
        if (appConfigFrament != null) {
            transaction.hide(appConfigFrament);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        faceRegister.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        application.removeActivity(this);
        RefWatcher refWatcher = MyApplication.getRefWatcher(this);
        refWatcher.watch(this);
    }
}
