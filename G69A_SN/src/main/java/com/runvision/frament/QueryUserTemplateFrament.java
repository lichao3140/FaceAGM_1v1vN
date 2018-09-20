package com.runvision.frament;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.runvision.core.MyApplication;
import com.runvision.db.User;
import com.runvision.faceagm_1v1vn.R;
import com.runvision.util.FileUtils;
import com.squareup.leakcanary.RefWatcher;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Administrator on 2018/7/9.
 */

public class QueryUserTemplateFrament extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private View view;
    private ListView mlistView;
    private MyListViewForTempAdapter adapter;
    private List<User> mList;
    private Button qt_next, qt_previous;
    private TextView qt_allpagenum,qt_current;
    private int pageIndex = 1;
    private int pageSize = 18;
    private int row;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null == view) {
            view = inflater.inflate(R.layout.queryusertemplateframent, container, false);

        }
        initView();
        initData();
        return view;
    }

    private void initData() {
        initPageData();
        adapter = new MyListViewForTempAdapter(mList, getActivity());
        mlistView.setAdapter(adapter);

    }

    private void initPageData() {
        mList = MyApplication.faceProvider.queryUsers((pageIndex - 1) * pageSize);
        int num = MyApplication.faceProvider.quaryUserTableRowCount();
        if (num % pageSize == 0) {
            row = (num / pageSize);
        } else {
            row = (num / pageSize) + 1;
        }
        qt_current.setText(pageIndex + "");
        qt_allpagenum.setText(row + "");
    }


    public void open() {
        initPageData();
        adapter.setmList(mList);
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        mlistView = view.findViewById(R.id.temp_listview);

        mlistView.setSelector(R.color.listViewSelect);
        mlistView.setOnItemClickListener(this);

        qt_next = view.findViewById(R.id.qt_next);
        qt_previous = view.findViewById(R.id.qt_previous);
        qt_current = view.findViewById(R.id.qt_current);

        qt_allpagenum = view.findViewById(R.id.qt_allpagenum);
        qt_next.setOnClickListener(this);
        qt_previous.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.qt_next:

                if (pageIndex == row) {
                    Toast.makeText(getContext(), "已经是最后一页了", Toast.LENGTH_SHORT).show();
                } else {
                    pageIndex++;
                    initPageData();
                    adapter.setmList(mList);
                }
                break;
            case R.id.qt_previous:
                if (pageIndex == 1) {
                    Toast.makeText(getContext(), "已经是第一页了", Toast.LENGTH_SHORT).show();
                } else {
                    pageIndex--;
                    initPageData();
                    adapter.setmList(mList);
                }
                break;
            case R.id.tempdetail_close:
                dialog.dismiss();
                dialog_flag=false;
                break;
            case R.id.tempdetail_updateFace:
                //修改模版信息
                System.out.println("修改模版信息");
                String name =tempdetail_name.getText().toString().trim();
                String workNumber=tempdetail_worknumber.getText().toString().trim();
                User userUpdate=new User(user.getId(),name,workNumber,choose_type);
                int ret=MyApplication.faceProvider.updateUserById(userUpdate);
                if(ret==0){
                    Toast.makeText(getContext(),"修改成功",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(),"修改失败",Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                open();
                dialog_flag=false;
                break;
            default:
                break;
        }
    }

    private String[] types = {"未知","VIP","员工"};
    private Dialog dialog = null;
    private ImageView tempdetail_bitmap;
    private TextView tempdetail_name, tempdetail_worknumber;
    private Spinner tempdetail_type;
    private String choose_type;
    private User user;
    private boolean dialog_flag=false;
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(dialog_flag){
            return;
        }
        dialog_flag=true;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        //加载视图
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.templaterdetails, null);
        tempdetail_bitmap = v.findViewById(R.id.tempdetail_bitmap);
        tempdetail_name = v.findViewById(R.id.tempdetail_name);
        tempdetail_worknumber = v.findViewById(R.id.tempdetail_worknumber);
        tempdetail_type = v.findViewById(R.id.tempdetail_type);
        tempdetail_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String cardNumber = QueryUserTemplateFrament.this.getResources().getStringArray(R.array.user_type)[i];
                choose_type = cardNumber;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Button tempdetail_close = v.findViewById(R.id.tempdetail_close);
        Button tempdetail_updateFace = v.findViewById(R.id.tempdetail_updateFace);
        tempdetail_close.setOnClickListener(this);
        tempdetail_updateFace.setOnClickListener(this);
        //加载数据
        user = mList.get(i);
        Bitmap temp_image = FileUtils.loadTempBitmap(user.getImagepath(), "FaceTemplate");
        if (temp_image == null) {
            tempdetail_bitmap.setImageResource(R.mipmap.ic_launcher);
        } else {
            tempdetail_bitmap.setImageBitmap(temp_image);
        }
        tempdetail_name.setText(user.getName());
        tempdetail_worknumber.setText(user.getUserid());
        for (int ii = 0; ii < types.length; ii++) {
            if (types[ii].equals(user.getType())) {
                tempdetail_type.setSelection(ii);
            }
        }
        //
        dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(v);
        //可以设置显示的位置
        dialog.getWindow().setGravity(Gravity.CENTER);
        //这段代码是为了解决不弹出输入法
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MyApplication.getRefWatcher(getContext());
        refWatcher.watch(this);
    }
}
