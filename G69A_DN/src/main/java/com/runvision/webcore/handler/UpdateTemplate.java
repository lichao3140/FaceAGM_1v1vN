package com.runvision.webcore.handler;

import android.graphics.Bitmap;

import com.face.sv.FaceInfo;
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
import java.util.Map;

public class UpdateTemplate implements RequestHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        String name = params.get("name");
        int type = Integer.parseInt(params.get("type"));
        int sex = Integer.parseInt(params.get("sex"));
        int age = Integer.parseInt(params.get("age"));
        String workNo = params.get("workNo");
        String cardNo = params.get("cardNo");
        String imageBase64 = params.get("img");
        int id = Integer.parseInt(params.get("id"));

        String templateName = MyApplication.faceProvider.getUserByUserId(id).getTemplateImageID();
        User user = new User(id, name, Type.getType(type).getDesc(), Sex.getSex(sex).getDesc(), age, workNo, cardNo, templateName);
        WebDataResultJson dataResultJson;
        int ret = MyApplication.faceProvider.updateUserById(user);
        if(ret==0){
            dataResultJson = new WebDataResultJson(200, "success", null);
        }else{
            dataResultJson = new WebDataResultJson(404, "error", null);
        }
        response.setStatusCode(200);
        response.setEntity(new StringEntity(JsonUtils.toJson(dataResultJson), "UTF-8"));
    }

//    private String updateTemplate(Bitmap bmp, String templateName, int id, String imageID) {
//        //转RGB
//        byte[] mBGR = FileUtils.bitmapToBGR24(bmp);
//        //生成随机图片ID
//
//        if (mBGR == null) {
//            return "图片转码错误";
//        }
//        FaceInfo faceInfoKj = MyApplication.mDetect.getFacePositionScaleFromGray(mBGR, bmp.getWidth(), bmp.getHeight(), 5);
//        if (faceInfoKj.getRet() != 1) {
//            return "检测不到人脸";
//        }
//        int ret = MyApplication.mRecognize.updateFaceFeature(id, mBGR, bmp.getWidth(), bmp.getHeight(), faceInfoKj.getFacePosData(0));
//        if (ret >= 0) {
//            FileUtils.deleteTempter(templateName, Const.TEMP_DIR);
//            //保存到SD下
//            FileUtils.saveFile(bmp, imageID, Const.TEMP_DIR);
//            return "success";
//        } else {
//            return "修改脸特征,错误码:" + ret;
//        }
//    }
}
