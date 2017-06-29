package com.acrcloud.rec.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;


import com.acrcloud.rec.JiaHeLogistic;

import java.util.Iterator;
import java.util.Stack;

/**
 * Created by Li Huanling on 2016/7/18 01:12.
 *
 * 普通工具类
 */
public class Utils {
	/**
	 * 提示用户，如有需要，方便自定义
	 * @param c 上下文
	 * @param msg 提示内容
	 * @param duration 显示持续时间
	 */
	public static final void showToast(Context c, String msg, int duration) {
		Toast.makeText(c, msg, duration).show();
	}

	public static final void Exit() {
		Stack<Activity> stack = JiaHeLogistic.getInstance().getStack();
		Iterator<Activity> iterator = stack.iterator();
		while (iterator.hasNext()) {
			Activity activity = iterator.next();
			activity.finish();
		}

		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	/**
	 * 返回当前程序版本号
	 *
	 * @param context 上下文
	 * @return 版本号
	 */
	@SuppressWarnings({"unused", "UnusedAssignment"})
	public static String getAppVersionName(Context context) {
		String versionName = "";
		int versioncode = 0;
		try {
			// ---get the package info---
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			versioncode = pi.versionCode;
			if (versionName == null || versionName.length() <= 0) {
				return "";
			}
		} catch (Exception e) {
			Log.e("VersionInfo", "Exception", e);
		}
		return versionName;
	}
}
