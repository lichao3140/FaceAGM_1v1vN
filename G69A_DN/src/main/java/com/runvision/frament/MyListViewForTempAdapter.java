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
//import com.runvision.core.Const;
//import com.runvision.core.MyApplication;
//import com.runvision.db.User;
//import com.runvision.faceagm_1v1vn.R;
//import com.runvision.util.DateTimeUtils;
//import com.runvision.util.FileUtils;
//
//import java.util.List;
//
///**
// * Created by Administrator on 2018/7/9.
// */
//
//public class MyListViewForTempAdapter extends BaseAdapter {
//    private List<User> mList;
//    private LayoutInflater mInflater;
//    private Context mContext;
//
//    public void setmList(List<User> mList) {
//        this.mList = mList;
//        notifyDataSetChanged();
//    }
//
//    public MyListViewForTempAdapter(List<User> mList, Context mContext) {
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
//            convertView = mInflater.inflate(R.layout.listview_item, parent, false);
//            holder = new ViewHolder(convertView);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//        final User user = mList.get(position);
//        holder.id.setText(user.getId() + "");
//        holder.userID.setText(user.getWordNo());
//        holder.userName.setText(user.getName() + "");
//        holder.userType.setText(user.getType() + "");
//        holder.cardNo.setText(user.getCardNo());
//        holder.delete_temp_item.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                final AlertDialog alert = new AlertDialog.Builder(mContext).create();
//                alert.setTitle("提示");
//                alert.setMessage("是否删除模版");
//                alert.setButton(DialogInterface.BUTTON_NEGATIVE, "否", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        alert.dismiss();
//                    }
//                });
//                alert.setButton(DialogInterface.BUTTON_POSITIVE, "是", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        //删除模版库和数据
//                        int ret = MyApplication.mRecognize.deleteFaceFeature(user.getId());
//                        if (ret == 0) {
//                            MyApplication.faceProvider.deleteUserById(user.getId());
//                            //删除本地模版图片
//                            System.out.println("imageid:" + user.getTemplateImageID());
//                            FileUtils.deleteTempter(user.getTemplateImageID(), Const.TEMP_DIR);
//                            mList.remove(position);
//                            notifyDataSetChanged();
//                            Toast.makeText(mContext, "成功", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(mContext, "模版删除失败", Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                });
//                alert.show();
//            }
//        });
//
//        return convertView;
//    }
//
//
//    private class ViewHolder {
//        TextView id, userID, userName, userType, cardNo, delete_temp_item;
//
//
//        public ViewHolder(View v) {
//            id = v.findViewById(R.id.listview_id);
//            userID = v.findViewById(R.id.listview_userID);
//            userName = v.findViewById(R.id.listview_userName);
//            userType = v.findViewById(R.id.listview_Type);
//            cardNo = v.findViewById(R.id.listview_time);
//            delete_temp_item = v.findViewById(R.id.delete_temp_item);
//
//        }
//    }
//}
