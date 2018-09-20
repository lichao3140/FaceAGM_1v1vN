package com.runvision.webcore.handler;

import com.runvision.core.Const;
import com.runvision.core.MyApplication;
import com.runvision.db.User;
import com.runvision.util.FileUtils;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.RequestMethod;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;
import java.util.Map;

public class DeleteTemplate implements RequestHandler {

    @RequestMapping(method = RequestMethod.POST)
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        int id = Integer.parseInt(params.get("id"));

        User user = MyApplication.faceProvider.getUserByUserId(id);

        FileUtils.deleteTempter(user.getTemplateImageID(), Const.TEMP_DIR);

        MyApplication.faceProvider.deleteUserById(id);

        response.setStatusCode(200);

    }
}
