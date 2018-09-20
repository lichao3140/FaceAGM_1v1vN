package com.runvision.webcore.handler;

import android.graphics.Bitmap;
import android.util.Log;

import com.face.sv.FaceInfo;
import com.google.gson.Gson;
import com.runvision.bean.AppData;
import com.runvision.bean.Sex;
import com.runvision.bean.Type;
import com.runvision.bean.WebDataResultJson;
import com.runvision.core.Const;
import com.runvision.core.MyApplication;
import com.runvision.db.User;
import com.runvision.util.DateTimeUtils;
import com.runvision.util.FileUtils;
import com.runvision.util.IDUtils;
import com.runvision.util.JsonUtils;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class InsertTemplate implements RequestHandler {
    @Override
    public void handle(HttpRequest request, final HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        String name = params.get("name");
        int type = Integer.parseInt(params.get("type"));
        int sex = Integer.parseInt(params.get("sex"));
        int age = Integer.parseInt(params.get("age"));
        String workNo = params.get("workNo");
        String cardNo = params.get("cardNo");
        String imageBase64 = params.get("img");
        int currentIndex = Integer.parseInt(params.get("currentIndex"));
        int numSize = Integer.parseInt(params.get("numSize"));

        System.out.println(currentIndex+"/"+numSize);


        if (numSize <= 1) {
            List<User> users = MyApplication.faceProvider.queryUsers("select * from tUser where cardNo=" + cardNo);
            if (users.size() > 0) {
                WebDataResultJson dataResultJson = new WebDataResultJson(404, "该模版已经存在", null);
                response.setStatusCode(200);
                response.setEntity(new StringEntity(JsonUtils.toJson(dataResultJson), "UTF-8"));
                return;
            }

            Bitmap bmp = FileUtils.stringtoBitmap(imageBase64);
            if (bmp == null) {
                WebDataResultJson dataResultJson = new WebDataResultJson(404, "图片转码失败", null);
                response.setStatusCode(200);
                response.setEntity(new StringEntity(JsonUtils.toJson(dataResultJson), "UTF-8"));
            } else {
                User user = new User(name, Type.getType(type).getDesc(), Sex.getSex(sex).getDesc(), age, workNo, cardNo, "", DateTimeUtils.getTime());
                String msg = insertTemplate(bmp, user);
                WebDataResultJson dataResultJson;

                if (msg.equals("success")) {
                    dataResultJson = new WebDataResultJson(200, msg, null);
                } else {
                    dataResultJson = new WebDataResultJson(404, msg, null);
                }
                response.setStatusCode(200);
                response.setEntity(new StringEntity(JsonUtils.toJson(dataResultJson), "UTF-8"));
            }
        } else {
            if (currentIndex == 1 && numSize > 1) {
                AppData.getAppData().setFlag(Const.WEB_BATCHIMAGE_TRUE);
            }
            List<User> users = MyApplication.faceProvider.queryUsers("select * from tUser where cardNo='" + cardNo+"'");
            if (users.size() > 0) {
                WebDataResultJson dataResultJson = new WebDataResultJson(404, "该模版已经存在", null);
                response.setStatusCode(200);
                response.setEntity(new StringEntity(JsonUtils.toJson(dataResultJson), "UTF-8"));
                if (currentIndex == numSize) {
                    Log.i("HTTP", "批量导入完成");
                    AppData.getAppData().setFlag(Const.WEB_BATCHIMAGE_FALSE);
                }
                return;
            }

            Bitmap bmp = FileUtils.stringtoBitmap(imageBase64);
            if (bmp == null) {
                WebDataResultJson dataResultJson = new WebDataResultJson(404, "图片转码失败", null);
                response.setStatusCode(200);
                response.setEntity(new StringEntity(JsonUtils.toJson(dataResultJson), "UTF-8"));

                if (currentIndex == numSize) {
                    Log.i("HTTP", "批量导入完成");
                    AppData.getAppData().setFlag(Const.WEB_BATCHIMAGE_FALSE);
                }
            } else {

                User user = new User(name, Type.getType(type).getDesc(), Sex.getSex(sex).getDesc(), age, workNo, cardNo, "", DateTimeUtils.getTime());
                String msg = insertTemplate(bmp, user);
                WebDataResultJson dataResultJson;

                if (msg.equals("success")) {
                    dataResultJson = new WebDataResultJson(200, msg, null);
                } else {
                    dataResultJson = new WebDataResultJson(404, msg, null);
                }

                if (currentIndex == numSize) {
                    Log.i("HTTP", "批量导入完成");
                    AppData.getAppData().setFlag(Const.WEB_BATCHIMAGE_FALSE);
                }

                response.setStatusCode(200);
                response.setEntity(new StringEntity(JsonUtils.toJson(dataResultJson), "UTF-8"));


            }
        }
    }


    private String insertTemplate(Bitmap bmp, User user) {
        //转RGB
        byte[] mBGR = FileUtils.bitmapToBGR24(bmp);
        //生成随机图片ID
        String imageID = IDUtils.genImageName();
        user.setTemplateImageID(imageID);

        //插入数据库
        int id = MyApplication.faceProvider.addUserOutId(user);

        if (mBGR == null) {
            MyApplication.faceProvider.deleteUserById(id);

            return "图片转码错误";
        }
        FaceInfo faceInfoKj = MyApplication.mDetect.getFacePositionScaleFromGray(mBGR, bmp.getWidth(), bmp.getHeight(), 5);
        if (faceInfoKj.getRet() != 1) {
            MyApplication.faceProvider.deleteUserById(id);
            return "检测不到人脸";
        }
        int ret = MyApplication.mRecognize.registerFaceFeature(id, mBGR, bmp.getWidth(), bmp.getHeight(), faceInfoKj.getFacePosData(0));
        if (ret > 0) {
            //保存到SD下
            FileUtils.saveFile(bmp, imageID, "FaceTemplate");
            return "success";
        } else {
            MyApplication.faceProvider.deleteUserById(id);
            return "注册人脸异常,错误码:" + ret;
        }

    }

    interface ICallBack {
        void getMessage(String msg);
    }
}
