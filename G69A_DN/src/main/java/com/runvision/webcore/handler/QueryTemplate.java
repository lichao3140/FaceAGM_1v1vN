package com.runvision.webcore.handler;

import android.util.Log;

import com.google.gson.Gson;
import com.runvision.bean.Type;
import com.runvision.bean.WebDataResultJson;
import com.runvision.core.MyApplication;
import com.runvision.db.User;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.RequestMethod;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryTemplate implements RequestHandler {

    @RequestMapping(method = RequestMethod.POST)
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        String name = params.get("ff_name");
        int type = Integer.parseInt(params.get("ff_type"));
        int sex = Integer.parseInt(params.get("ff_sex"));
        String sexStr = "";
        if (sex == 0) {
            sexStr = "未知";
        }
        if (sex == 1) {
            sexStr = "男";
        }
        if (sex == 2) {
            sexStr = "女";
        }
        String age_min = params.get("ff_age_min");
        String age_max = params.get("ff_age_max");
        String workNo = params.get("ff_job_number");
        String cardNo = params.get("ff_card_id");




        int pageNo = Integer.parseInt(params.get("ff_page_no"));
        int pageSize = Integer.parseInt(params.get("ff_page_size"));


        StringBuffer sb = new StringBuffer();
        sb.append("select * from tUser where 1 = 1");
        if (!name.equals("")) {
            sb.append(" and name like '" + name + "'");
        }
        if (!sexStr.equals("")) {
            sb.append(" and sex ='" + sexStr+"'");
        }
        if (type != -1) {
            sb.append(" and type='" + Type.getType(type).getDesc()+"'");
        }

        if (!age_min.equals("")) {
            sb.append(" and age>=" + Integer.parseInt(age_min));
        }

        if (!age_max.equals("")) {
            sb.append(" and age<=" + Integer.parseInt(age_max));
        }
        if (!cardNo.equals("")) {
            sb.append(" and cardNo like '%" + cardNo + "%'");
        }
        if (!workNo.equals("")) {
            sb.append(" and workNo like '%" + workNo + "%'");
        }

        sb.append(" order by id desc limit " + pageSize + " offset " + (pageNo - 1) * pageSize);

        Log.i("HTTP", "QueryTemplateCount:sql= " + sb.toString());
        List<User> mList = MyApplication.faceProvider.queryUsers(sb.toString());

        WebDataResultJson<List<User>> web = new WebDataResultJson<>(200, mList);

        Gson gson = new Gson();
        String json = gson.toJson(web);
        Log.i("HTTP", "QueryTemplateCount:json= " + json);

        response.setEntity(new StringEntity(json, "UTF-8"));
        response.setStatusCode(200);
    }
}
