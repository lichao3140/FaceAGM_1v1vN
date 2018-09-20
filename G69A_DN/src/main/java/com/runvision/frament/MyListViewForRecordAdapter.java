//package com.runvision.frament;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.runvision.core.MyApplication;
//import com.runvision.db.Record;
//import com.runvision.db.User;
//import com.runvision.faceagm_1v1vn.R;
//import com.runvision.util.DateTimeUtils;
//
//import java.util.List;
//
///**
// * Created by Administrator on 2018/7/9.
// */
//
//public class MyListViewForRecordAdapter extends BaseAdapter {
//    private List<User> mList;
//    private LayoutInflater mInflater;
//    private Context mContext;
//
//    public void setmList(List<User> mList) {
//        this.mList = mList;
//        notifyDataSetChanged();
//    }
//
//    public MyListViewForRecordAdapter(List<User> mList, Context mContext) {
//        this.mList = mList;
//        this.mInflater = LayoutInflater.from(mContext);
//        this.mContext = mContext;
//    }
//
//
//    @Override
//    public int getCount() {
//        return mList.size();
//    }
//
//    @Override
//    public Object getItem(int i) {
//        return mList.get(i);
//    }
//
//    @Override
//    public long getItemId(int i) {
//        return i;
//    }
//
//
//    @Override
//    public View getView(final int position, View convertView, ViewGroup parent) {
//        ViewHolder holder = null;
//        if (convertView == null) {
//            //加载布局
//            convertView = mInflater.inflate(R.layout.listview_item_record, parent, false);
//            holder = new ViewHolder(convertView);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//        User user = mList.get(position);
//        holder.id.setText(user.getId() + "");
//        holder.userID.setText(user.getCardNo());
//        holder.userName.setText(user.getName() + "");
//
//        holder.userType.setText(user.getRecord().getComperType());
//
//
//        holder.creatTime.setText(DateTimeUtils.parseDataTimeToFormatString(user.getTime() * 1000));
//        holder.socre.setText(user.getRecord().getScore()+"");
//        holder.comperResult.setText(user.getRecord().getCompertResult()+"");
//
//        return convertView;
//    }
//
//
//    private class ViewHolder {
//        TextView id, userID, userName, userType, socre, comperResult, creatTime;
//
//        public ViewHolder(View v) {
//            id = v.findViewById(R.id.qr_item_id);
//            userID = v.findViewById(R.id.qr_item_userID);
//            userName = v.findViewById(R.id.qr_item_name);
//            userType = v.findViewById(R.id.qr_item_type);
//            creatTime = v.findViewById(R.id.qr_item_time);
//            comperResult = v.findViewById(R.id.qr_item_comperResult);
//            socre = v.findViewById(R.id.qr_item_sorce);
//
//        }
//    }
//}
