package com.runvision.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

import com.face.sv.FaceInfo;
import com.runvision.core.Const;
import com.runvision.core.MyApplication;
import com.runvision.db.User;
import com.runvision.util.DateTimeUtils;
import com.runvision.util.FileUtils;
import com.runvision.util.IDUtils;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/7/23.
 */

public class BatchImport implements Runnable {

    private List<File> mList;

    private Handler handler;

    private int messageID;

    private int num;


    public BatchImport(List<File> mList, Handler handler, int messageID) {
        this.mList = mList;
        this.handler = handler;
        this.messageID = messageID;
        this.num = mList.size();
    }

    @Override
    public void run() {
        for (int i = 1; i <= num; i++) {

            File file = mList.get(i - 1);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), FileUtils.getBitmapOption(2));
            byte[] mBGR = FileUtils.bitmapToBGR24(bitmap);


            String userName = file.getName().substring(0, file.getName().indexOf("."));
            String[] strs = userName.split("&");
            if (strs.length != 6) {
                System.out.println("命名不规范");
                sendMsg(i);
                continue;
            }
            //保存图片
            //生成随机图片ID
            String imageID = IDUtils.genImageName();
            FileUtils.saveFile(bitmap, imageID, Const.TEMP_DIR);
            User user = new User(strs[0], strs[1], strs[2], Integer.parseInt(strs[3]), strs[4], strs[5], imageID,DateTimeUtils.getTime());
            int id = MyApplication.faceProvider.addUserOutId(user);

            if (mBGR == null) {
                MyApplication.faceProvider.deleteUserById(id);
                //FileUtils.saveFile(bitmap, userName, "errorImage");
                FileUtils.deleteTempter(imageID, Const.TEMP_DIR);
                sendMsg(i);
//                publishProgress();
                System.out.println("RBG==NULL");
                continue;
            }
            FaceInfo faceInfoKj = MyApplication.mDetect.getFacePositionScaleFromGray(mBGR, bitmap.getWidth(), bitmap.getHeight(), 5);
            if (faceInfoKj.getRet() != 1) {
                MyApplication.faceProvider.deleteUserById(id);
                //error++;
                FileUtils.deleteTempter(imageID, Const.TEMP_DIR);
                //FileUtils.saveFile(bitmap, userName, "errorImage");
                // publishProgress();
                System.out.println("人脸定位失败");
                sendMsg(i);
                continue;
            }
            int ret = MyApplication.mRecognize.registerFaceFeature(id, mBGR, bitmap.getWidth(), bitmap.getHeight(), faceInfoKj.getFacePosData(0));
            if (ret > 0) {
                file.delete();
                sendMsg(i);

//                FileUtils.saveFile(bitmap, id + "", "FaceTemplate");
                ///success++;
                //publishProgress();
            } else {
                System.out.println("注册模版error");
                MyApplication.faceProvider.deleteUserById(id);
                // error++;
                FileUtils.deleteTempter(imageID, "FaceTemplate");
                //FileUtils.saveFile(bitmap, userName, "errorImage");
                /// publishProgress();
                sendMsg(i);
                continue;
            }

        }

    }

    private void sendMsg(int i) {

        Message msg = new Message();
        msg.what = messageID;
        msg.obj = i;
        handler.sendMessage(msg);
    }
}
