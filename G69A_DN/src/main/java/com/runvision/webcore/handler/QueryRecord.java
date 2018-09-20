package com.runvision.webcore.handler;

import android.util.Log;

import com.google.gson.Gson;
import com.runvision.bean.Type;
import com.runvision.bean.WebDataResultJson;
import com.runvision.core.MyApplication;
import com.runvision.db.Record;
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

public class QueryRecord implements RequestHandler {
    @RequestMapping(method = RequestMethod.POST)
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        String beginTime = params.get("time_begin");
        String endTime = params.get("time_end");
        int pageNo = Integer.parseInt(params.get("page_no"));
        int pageSize = Integer.parseInt(params.get("page_size"));
        String cardNo = params.get("fcmp_cardId");
        String name = params.get("fcmp_name");

        int templateType = Integer.parseInt(params.get("tmp_type"));

        int type = Integer.parseInt(params.get("type"));

        StringBuffer sb = new StringBuffer();
        sb.append("select * from tRecord where 1 = 1");

        sb.append(" and comperType='" + (type == 0 ? "人证" : "1:N")+"'");

        if (!beginTime.equals("")) {
            sb.append(" and createTime>" + beginTime);
        }
        if (!endTime.equals("")) {
            sb.append(" and createTime<" + endTime);
        }
        if (!cardNo.equals("")) {
            sb.append(" and cardNo like '%" + cardNo + "%'");
        }
        if (!name.equals("")) {
            sb.append(" and name like '%" + name + "%'");
        }
        if (templateType != -1) {
            sb.append(" and type='" + Type.getType(templateType).getDesc()+"'");
        }


        sb.append(" order by id desc limit " + pageSize + " offset " + (pageNo - 1) * pageSize);

        Log.i("HTTP", "QueryCardRecordCount:sql= " + sb.toString());
        List<User> mList = MyApplication.faceProvider.queryRecord(sb.toString());

        WebDataResultJson<List<User>> web = new WebDataResultJson<>(200, mList);

        Gson gson = new Gson();
        String json = gson.toJson(web);
        Log.i("HTTP", "QueryCardRecordCount:json= " + json);

        response.setEntity(new StringEntity(json, "UTF-8"));
        response.setStatusCode(200);
    }
}
