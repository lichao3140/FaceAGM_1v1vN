/*
 * Copyright 2017, Tnno Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.runvision.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.runvision.bean.AppData;
import com.runvision.core.MyApplication;

/**
 * Created by Tnno Wu on 2017/4/8.
 */

public class SPUtil {

	private static final String CONFIG = "config";

	/**
	 * 获取SharedPreferences实例对象
	 * 
	 * @param fileName
	 */
	private static SharedPreferences getSharedPreference(String fileName) {
		return MyApplication.getContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
	}

	/**
	 * 保存一个String类型的值！
	 */
	@SuppressLint("NewApi")
	public static void putString(String key, String value) {
		SharedPreferences.Editor editor = getSharedPreference(CONFIG).edit();
		editor.putString(key, value).apply();
	}
	
	/**
	 * 保存一个String类型的值！
	 */
	@SuppressLint("NewApi")
	public static void putInt(String key, int value) {
		SharedPreferences.Editor editor = getSharedPreference(CONFIG).edit();
		editor.putInt(key, value).apply();
	}

	/**
	 * 保存一个String类型的值！
	 */
	@SuppressLint("NewApi")
	public static void putBoolean(String key, boolean value) {
		SharedPreferences.Editor editor = getSharedPreference(CONFIG).edit();
		editor.putBoolean(key, value).apply();
	}

	/**
	 * 获取String的value
	 */
	public static String getString(String key, String defValue) {
		SharedPreferences sharedPreference = getSharedPreference(CONFIG);
		return sharedPreference.getString(key, defValue);
	}
	
	/**
	 * 获取String的value
	 */
	public static int getInt(String key, int defValue) {
		SharedPreferences sharedPreference = getSharedPreference(CONFIG);
		return sharedPreference.getInt(key, defValue);
	}


	public static boolean getBoolean(String key, boolean defValue) {
		SharedPreferences sharedPreference = getSharedPreference(CONFIG);
		return sharedPreference.getBoolean(key, defValue);
	}


	public static void addFloat(String key, float value) {
		SharedPreferences pref = getSharedPreference(CONFIG);
		SharedPreferences.Editor editor = pref.edit();
		editor.putFloat(key, value);
		editor.commit();
	}


	public static float getFloat(String key, Float defValue) {
		SharedPreferences pref = getSharedPreference(CONFIG);
		Float name = pref.getFloat(key, defValue);

		return name;
	}

	public static void setIp(String ip){
		String[] Sip = ip.split("\\.");

		String str = Sip[0] + "." + Sip[1] + "." + Sip[2] + ".1";
		Log.i("aa", str + "," + ip);

		String[] staticIP = new String[]{ip,
				"255.255.255.0", str, "8.8.8.8"};

        for (int i = 0; i < staticIP.length; i++) {
            System.out.println(staticIP[i]);
        }
		
		Intent closeIntent = new Intent("com.snstar.networkparameters.ETH_CLOSE");
		MyApplication.getContext().sendBroadcast(closeIntent);

		Intent i = new Intent("com.snstar.networkparameters.ETHSETINGS");
		Bundle bundle = new Bundle();
		bundle.putSerializable("STATIC_IP", staticIP);
		i.putExtras(bundle);
		MyApplication.getContext().sendBroadcast(i);
		Intent iopen = new Intent("com.snstar.networkparameters.ETH_OPEN");
		MyApplication.getContext().sendBroadcast(iopen);
	}
}
