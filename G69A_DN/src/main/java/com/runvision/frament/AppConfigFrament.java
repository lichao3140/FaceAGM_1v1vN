package com.runvision.frament;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.runvision.core.Const;
import com.runvision.faceagm_1v1vn.R;
import com.runvision.util.ConversionHelp;
import com.runvision.util.SPUtil;
import com.runvision.webcore.util.NetUtils;

public class AppConfigFrament extends Fragment implements View.OnClickListener {
    private Context mContext;
    private View view;
    private EditText onevsonescore, onevsmorescore, waithome, closedoor, vmsip, vmsport, vmsusername, vmspassword, lochost;
    private CheckBox live, music;
    private Button btn_Sure, btn_Refresh;
    private Spinner Preservation_time;
    private int preservation_day;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        if (null == view) {
            view = inflater.inflate(R.layout.appconfigframent, container, false);
        }
        initView();
        initData();
        return view;
    }

    /**
     * 初始化的时候加载当前参数
     */
    private void initData() {
        //1:N的阀值
        int oneVsMoreScore = SPUtil.getInt(Const.KEY_ONEVSMORESCORE, Const.ONEVSMORE_SCORE);
        onevsmorescore.setText(oneVsMoreScore + "");
        //人证的阀值
        int cardScore = SPUtil.getInt(Const.KEY_CARDSCORE, Const.FACE_SCORE);
        onevsonescore.setText(cardScore + "");
        //是否开启活体检测
        boolean isOpenLive = SPUtil.getBoolean(Const.KEY_ISOPENLIVE, Const.OPEN_LIVE);
        live.setChecked(isOpenLive);

        //是否开启语音播报
        boolean isOpenMusic = SPUtil.getBoolean(Const.KEY_ISOPENMUSIC, Const.OPEN_MUSIC);
        music.setChecked(isOpenMusic);
        //
        int backHome = SPUtil.getInt(Const.KEY_BACKHOME, Const.CLOSE_HOME_TIMEOUT);
        waithome.setText(backHome + "");
        //开门延时时间
        int closeDoorTime = SPUtil.getInt(Const.KEY_OPENDOOR, Const.CLOSE_DOOR_TIME);
        closedoor.setText(closeDoorTime + "");
        //IP
        String ip = SPUtil.getString(Const.KEY_VMSIP, "");
        vmsip.setText(ip);
        //PROT
        int prot = SPUtil.getInt(Const.KEY_VMSPROT, 0);
        vmsport.setText(prot + "");
        //USERNAME
        String userName = SPUtil.getString(Const.KEY_VMSUSERNAME, "");
        vmsusername.setText(userName);
        //PASSWORD
        String password = SPUtil.getString(Const.KEY_VMSPASSWORD, "");
        vmspassword.setText(password);
        //lochost
        lochost.setText(NetUtils.getIpAddress(mContext) == null ? "" : NetUtils.getIpAddress(mContext));

        if(SPUtil.getInt(Const.KEY_PRESERVATION_DAY,90)==90) {
            Preservation_time.setSelection(0);
        } else if(SPUtil.getInt(Const.KEY_PRESERVATION_DAY,90)==60) {
            Preservation_time.setSelection(1);
        } else if(SPUtil.getInt(Const.KEY_PRESERVATION_DAY,90)==30) {
            Preservation_time.setSelection(2);
        }
    }

    private void initView() {
        lochost = view.findViewById(R.id.ed_localhost);
        onevsonescore = view.findViewById(R.id.ed_onevsonescore);
        onevsmorescore = view.findViewById(R.id.ed_onevsmorescore);
        waithome = view.findViewById(R.id.ed_waithome);
        closedoor = view.findViewById(R.id.ed_closedoor);
        vmsip = view.findViewById(R.id.ed_vmsip);
        vmsport = view.findViewById(R.id.ed_vmsport);
        vmsusername = view.findViewById(R.id.ed_vmsusername);
        vmspassword = view.findViewById(R.id.ed_vmspassword);
        live = view.findViewById(R.id.cb_live);
        music = view.findViewById(R.id.cb_music);
        btn_Sure=(Button)view.findViewById(R.id.btn_Sure);
        btn_Sure.setOnClickListener(this);

        btn_Refresh=(Button)view.findViewById(R.id.btn_Refresh);
        btn_Refresh.setOnClickListener(this);

        Preservation_time = (Spinner) view.findViewById(R.id.Preservation_time);
        Preservation_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sexNumber = AppConfigFrament.this.getResources().getStringArray(R.array.user_time)[i];
                System.out.println(sexNumber);
                if(sexNumber.equals("90天"))
                {
                    preservation_day=90;
                }else if(sexNumber.equals("60天"))
                {
                    preservation_day=60;
                }
                else if(sexNumber.equals("30天"))
                {
                    preservation_day=30;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setData() {
        //修改人证分数
        String oneVsOneScore = onevsonescore.getText().toString().trim();
        if (!"".equals(oneVsOneScore) && Integer.parseInt(oneVsOneScore) != SPUtil.getInt(Const.KEY_CARDSCORE, Const.FACE_SCORE)) {
            SPUtil.putInt(Const.KEY_CARDSCORE, Integer.parseInt(oneVsOneScore));
        }
        //修改1:N分数
        String oneVsMoreScore = onevsmorescore.getText().toString().trim();
        if (!"".equals(oneVsMoreScore) && Integer.parseInt(oneVsMoreScore) != SPUtil.getInt(Const.KEY_ONEVSMORESCORE, Const.ONEVSMORE_SCORE)) {
            SPUtil.putInt(Const.KEY_ONEVSMORESCORE, Integer.parseInt(oneVsMoreScore));
        }
        //修改等待待机时间长
        String waitStr = waithome.getText().toString().trim();
        if (!"".equals(waitStr) && Integer.parseInt(waitStr) != 0 && Integer.parseInt(waitStr) != SPUtil.getInt(Const.KEY_BACKHOME, Const.CLOSE_HOME_TIMEOUT)) {
            SPUtil.putInt(Const.KEY_BACKHOME, Integer.parseInt(waitStr));
        }
        //修改开门延时时间
        String colseDoorStr = closedoor.getText().toString().trim();
        if (!"".equals(colseDoorStr) && Integer.parseInt(colseDoorStr) != 0 && Integer.parseInt(colseDoorStr) != SPUtil.getInt(Const.KEY_OPENDOOR, Const.CLOSE_DOOR_TIME)) {
            SPUtil.putInt(Const.KEY_OPENDOOR, Integer.parseInt(colseDoorStr));
        }
        //修改VMS
        String vmsIpStr = vmsip.getText().toString().trim();
        if (!"".equals(vmsIpStr) && !vmsIpStr.equals(SPUtil.getString(Const.KEY_VMSIP, ""))) {
            SPUtil.putString(Const.KEY_VMSIP, vmsIpStr);
        }
        String vmsPortStr = vmsport.getText().toString().trim();
        if (!"".equals(vmsPortStr) && Integer.parseInt(vmsPortStr) != 0 && Integer.parseInt(vmsPortStr) != SPUtil.getInt(Const.KEY_VMSPROT, 0)) {
            SPUtil.putInt(Const.KEY_VMSPROT, Integer.parseInt(vmsPortStr));
        }
        String userNameStr = vmsusername.getText().toString().trim();
        if (!"".equals(userNameStr) && !SPUtil.getString(Const.KEY_VMSUSERNAME, "").equals(userNameStr)) {
            SPUtil.putString(Const.KEY_VMSUSERNAME, userNameStr);
        }
        String passStr = vmspassword.getText().toString().trim();
        if (!"".equals(passStr) && !SPUtil.getString(Const.KEY_VMSPASSWORD, "").equals(passStr)) {
            SPUtil.putString(Const.KEY_VMSPASSWORD, passStr);
        }
        //是否开启活体
        boolean isLive = live.isChecked();
        if (isLive != SPUtil.getBoolean(Const.KEY_ISOPENLIVE, Const.OPEN_LIVE)) {
            SPUtil.putBoolean(Const.KEY_ISOPENLIVE, isLive);
        }
        //语音播放
        boolean isMusic = music.isChecked();
        if (isMusic != SPUtil.getBoolean(Const.KEY_ISOPENMUSIC, Const.OPEN_MUSIC)) {
            SPUtil.putBoolean(Const.KEY_ISOPENMUSIC, isMusic);
        }

        //修改本机IP
        String lochostStr = lochost.getText().toString().trim();
        if (!"".equals(lochostStr) && !lochostStr.equals(SPUtil.getString(Const.KEY_IP, "")) && !lochostStr.equals(NetUtils.getIpAddress(mContext) == null ? "" : NetUtils.getIpAddress(mContext))) {
            boolean isNetWork = ConversionHelp.isNetworkConnected(mContext);
            if (isNetWork == false) {
                Toast.makeText(mContext, "无网络状态下无法修改IP", Toast.LENGTH_SHORT).show();
            } else {
                if (ipCheck(lochostStr)) {
                    System.out.println("修改IP");
                    ConversionHelp.updateIp(lochostStr, mContext);
                    try {
                        Thread.sleep(3 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(mContext, "修改本机IP成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "IP不合法", Toast.LENGTH_SHORT).show();
                    System.out.println("IP不合法");
                }
            }
        } else {
            Toast.makeText(mContext, "修改成功", Toast.LENGTH_SHORT).show();
        }

        // 修改保存数据天数
        SPUtil.putInt(Const.KEY_PRESERVATION_DAY, preservation_day);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            initData();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_Sure: // 确认修改
                setData();
                break;
            case R.id.btn_Refresh: //重置
                initData();
                break;
            default:
                break;
        }
    }


    /**
     * 验证IP的合法性
     *
     * @param text
     * @return
     */
    public boolean ipCheck(String text) {
        if (text != null && !text.isEmpty()) {
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            if (text.matches(regex)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
