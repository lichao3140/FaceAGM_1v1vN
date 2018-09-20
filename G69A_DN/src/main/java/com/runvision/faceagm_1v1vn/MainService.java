package com.runvision.faceagm_1v1vn;

import java.util.HashMap;
import java.util.Map;

import com.runvision.bean.AppData;
import com.runvision.core.Const;

import com.runvision.core.LogToFile;

import com.runvision.myview.MyCameraSuf;
import com.runvision.thread.ComperThread;
import com.runvision.thread.ListenOperation;
import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.module.idcard.IDCardReader;
import com.zkteco.android.biometric.module.idcard.IDCardReaderFactory;
import com.zkteco.android.biometric.module.idcard.exception.IDCardReaderException;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainService extends Service {
    private String TAG = "MainService";
    // ----------------------------------------读卡器参数-------------------------------------------------
    private static final int VID = 1024; // IDR VID
    private static final int PID = 50010; // IDR PID

    private IDCardReader idCardReader = null;
    private UsbManager musbManager = null;
    private boolean bStop = false;

    private MediaPlayer music;
    // -----------------------------------------end------------------------------------------------
    private static MainService myService = null;
    private Context mContext;

    public static MainService getService() {
        return myService;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mContext = this;
        myService = this;


        // 读卡器
        startIDCardReader();

        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, usbDeviceStateFilter);

    }

    // -----------------------读卡器器模块----------------------------------//
    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.e(TAG, "拔出usb了");
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    Log.e(TAG, "设备的ProductId值为：" + device.getProductId());
                    Log.e(TAG, "设备的VendorId值为：" + device.getVendorId());
                    if (device.getProductId() == PID && device.getVendorId() == VID) {
                        Toast.makeText(mContext, "读卡器拔出", Toast.LENGTH_LONG).show();
                        LogToFile.e(TAG, "读卡器拔出");
                        bStop = true;
                        try {
                            idCardReader.close(0);
                        } catch (IDCardReaderException e) {
                            // TODO Auto-generated catch block
                            Log.i(TAG, "关闭失败");
                        }
                        IDCardReaderFactory.destroy(idCardReader);
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (device.getProductId() == PID && device.getVendorId() == VID) {
                    // 读卡器
                    Toast.makeText(mContext, "读卡器插入", Toast.LENGTH_LONG).show();
                    LogToFile.e(TAG, "读卡器插入");
                    startIDCardReader();
                }
            }
        }
    };

    /**
     * 读卡器 初始化
     */
    private void startIDCardReader() {
        LogHelper.setLevel(Log.ASSERT);
        Map idrparams = new HashMap();
        idrparams.put(ParameterHelper.PARAM_KEY_VID, VID);
        idrparams.put(ParameterHelper.PARAM_KEY_PID, PID);
        idCardReader = IDCardReaderFactory.createIDCardReader(this, TransportType.USB, idrparams);
        readCard();
    }

    private void readCard() {
        try {
            idCardReader.open(0);
            bStop = false;
            Log.i(TAG, "设备连接成功");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!bStop) {
                        long begin = System.currentTimeMillis();
                        IDCardInfo idCardInfo = new IDCardInfo();
                        boolean ret = false;
                        try {
                            idCardReader.findCard(0);
                            idCardReader.selectCard(0);
                        } catch (IDCardReaderException e) {
                            continue;
                        }

                        try {
                            ret = idCardReader.readCard(0, 0, idCardInfo);
                        } catch (IDCardReaderException e) {
                            Log.i(TAG, "读卡失败，错误信息：" + e.getMessage());
                        }

                        AppData.getAppData().setFlag(Const.OPEN_HOME);
                        ListenOperation.cleanTime();
                        if (ret) {
                            final long nTickUsed = (System.currentTimeMillis() - begin);
                            Log.i(TAG, "success>>>" + nTickUsed + ",name:" + idCardInfo.getName() + "," + idCardInfo.getValidityTime() + "，" + idCardInfo.getDepart());
                            Message msg = new Message();
                            msg.what = 2;
                            msg.obj = idCardInfo;
                            mhandler.sendMessage(msg);
                        }
                    }

                }
            }).start();

        } catch (IDCardReaderException e) {
            Log.i(TAG, "连接设备失败");
            Log.i(TAG, "开始读卡失败，错误码：" + e.getErrorCode() + "\n错误信息：" + e.getMessage() + "\n内部代码=" + e.getInternalErrorCode());
            LogToFile.e(TAG, "开始读卡失败，错误码：" + e.getErrorCode() + "\n错误信息：" + e.getMessage() + "\n内部代码=" + e.getInternalErrorCode());
        }
    }

    private Handler mhandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    // 收到读卡器的信息
                    AppData.getAppData().setFlag(Const.READ_CARD);
                    IDCardInfo info = (IDCardInfo) msg.obj;
                    toComperFace(info);
                    ListenOperation.cleanTime();
                    break;
                default:
                    break;
            }

        }

        ;
    };

    private ComperThread comperThread = null;

    private void toComperFace(IDCardInfo idCardInfo) {
        if (idCardInfo.getPhotolength() > 0) {
            byte[] buf = new byte[WLTService.imgLength];
            if (1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(), buf)) {
                Bitmap cardBmp = IDPhotoHelper.Bgr2Bitmap(buf);
                if (cardBmp != null) {
                    if (comperThread != null) {
                        if (comperThread.Comper_status == true) {
                            System.out.println("aa");
                            return;
                        }
                    }
                    comperThread = new ComperThread(cardBmp, idCardInfo);
                    MyCameraSuf.exec.execute(comperThread);
                    AppData.getAppData().setCardBmp(cardBmp);

                } else {
                    Log.i(TAG, "读卡器解码得到的图片为空");
                }
            } else {
                Log.i(TAG, "图片解码 error");
                Toast.makeText(mContext, "身份证图片解码失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.i(TAG, "图片数据长度为0");
            Toast.makeText(mContext, "图片数据长度为0" + idCardInfo.getName(), Toast.LENGTH_SHORT).show();

        }
    }

    // end-----------------------读卡器器模块----------------------------------//

    @Override
    public void onDestroy() {
        bStop = true;
        try {
            idCardReader.close(0);
        } catch (IDCardReaderException e) {
            // TODO Auto-generated catch block
            Log.i(TAG, "关闭失败");
        }
        IDCardReaderFactory.destroy(idCardReader);
        unregisterReceiver(mUsbReceiver);
    }


}
