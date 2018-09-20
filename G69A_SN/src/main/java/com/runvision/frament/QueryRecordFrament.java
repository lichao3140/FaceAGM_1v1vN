package com.runvision.frament;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.runvision.core.Const;
import com.runvision.core.MyApplication;
import com.runvision.db.Record;
import com.runvision.db.User;
import com.runvision.faceagm_1v1vn.R;
import com.runvision.util.FileUtils;
import com.squareup.leakcanary.RefWatcher;

import java.util.List;

/**
 * Created by Administrator on 2018/7/10.
 */

public class QueryRecordFrament extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private View view;
    private ListView mListView;
    private int pageIndex = 1;
    private int pageSize = 23;
    private int row;
    private Button qr_next, qr_previous;
    private TextView qr_current, qr_allpagenum;
    MyListViewForRecordAdapter adapter;
    List<Record> mList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null == view) {
            view = inflater.inflate(R.layout.queryuserframent, container, false);
        }
        initView();
        initData();
        return view;
    }

    private void initData() {
        initPageData();

        adapter = new MyListViewForRecordAdapter(mList, getActivity());
        mListView.setAdapter(adapter);
    }

    private void initPageData() {
        mList = MyApplication.faceProvider.queryRecord(pageSize, (pageIndex - 1) * pageSize);
        int num = MyApplication.faceProvider.quaryRecordTableRowCount();
        if (num % pageSize == 0) {
            row = (num / pageSize);
        } else {
            row = (num / pageSize) + 1;
        }
        qr_current.setText(pageIndex + "");
        qr_allpagenum.setText(row + "");
    }

    public void open() {
        initPageData();
        adapter.setmList(mList);
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        mListView = view.findViewById(R.id.qr_listview);
        mListView.setSelector(R.color.listViewSelect);
        mListView.setOnItemClickListener(this);
        qr_next = view.findViewById(R.id.qr_next);
        qr_previous = view.findViewById(R.id.qr_previous);
        qr_current = view.findViewById(R.id.qr_current);
        qr_allpagenum = view.findViewById(R.id.qr_allpagenum);
        qr_next.setOnClickListener(this);
        qr_previous.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.qr_next:
                if (pageIndex == row) {
                    Toast.makeText(getContext(), "已经是最后一页了", Toast.LENGTH_SHORT).show();
                } else {
                    pageIndex++;
                    initPageData();
                    adapter.setmList(mList);
                }
                break;
            case R.id.qr_previous:
                if (pageIndex == 1) {
                    Toast.makeText(getContext(), "已经是第一页了", Toast.LENGTH_SHORT).show();
                } else {
                    pageIndex--;
                    initPageData();
                    adapter.setmList(mList);
                }
                break;
            default:
                break;
        }
    }

    private ImageView onevsmore_txicon, onevsmore_face, onevsmore_temper;
    private TextView onevsmore_userID, onevsmore_userName, onevsmore_userType;

    private Dialog dialog;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);


        View v = null;

        v = showOneVSMoreDialog(mList.get(i));


        //构建  显示
        dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(v);


    }

    private View showOneVSMoreDialog(Record user) {
        //加载视图
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View v = inflater.inflate(R.layout.onevsmorediaolog, null);
        //绑定视图
        onevsmore_txicon = v.findViewById(R.id.onevsmore_txicon);
        onevsmore_txicon.setVisibility(View.GONE);
        onevsmore_face = v.findViewById(R.id.onevsmore_face);
        onevsmore_temper = v.findViewById(R.id.onevsmore_temper);
        onevsmore_userID = v.findViewById(R.id.onevsmore_userID);
        onevsmore_userName = v.findViewById(R.id.onevsmore_userName);
        onevsmore_userType = v.findViewById(R.id.onevsmore_userType);
        //绑定数据
        onevsmore_userID.setText(user.getUserid());
        onevsmore_userName.setText(user.getName());



        Bitmap snapImage = FileUtils.loadTempBitmap(user.getSnapImageID(), Const.SNAP_DIR);
        onevsmore_face.setImageBitmap(snapImage);
        if(user.getType().equals("人证")){
            onevsmore_userType.setText(user.getType());

            Bitmap cardImage = FileUtils.loadTempBitmap(user.getTemplateImageID(), Const.CARD_DIR);
            onevsmore_temper.setImageBitmap(cardImage);
        }else {
            onevsmore_userType.setText("1:N("+user.getType()+")");
            Bitmap tempImage = FileUtils.loadTempBitmap(user.getTemplateImageID(), "FaceTemplate");
            onevsmore_temper.setImageBitmap(tempImage);
        }
        return v;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MyApplication.getRefWatcher(getContext());
        refWatcher.watch(this);
    }

}
