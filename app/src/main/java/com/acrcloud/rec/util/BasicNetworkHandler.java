package com.acrcloud.rec.util;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.acrcloud.rec.JiaHeLogistic;
import com.acrcloud.rec.demo.R;

/**
 * Created by Li Huanling on 2016/7/16 21:23.
 *
 * 基础的网络消息处理
 */
public class BasicNetworkHandler extends Handler {

	private static final String TAG = "BasicNetworkHandler";
	/**
	 * 全局应用
	 */
	protected JiaHeLogistic app;

	/**
	 * 是否需要升级
	 */
	private boolean isNeedUpgrade = false;

	/**
	 * 升级信息，在isNeedUpgrade为true时有用
	 */
	private UpgradeBean bean;

	/**
	 * 升级查询框
	 */
	private Dialog dialog;

	public BasicNetworkHandler(JiaHeLogistic app) {
		this.app = app;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
			case NetConfig.STATUS_NO_INTERNET_CONNECTION:
				// 设置已经提示用户网络状态
				app.setHasPromptNetwork(true);

				// 没有连接网络
				Utils.showToast(app.getStack().lastElement(), app.getString(R.string.jh_state_no_internet), Toast.LENGTH_SHORT);
				break;

			case NetConfig.STATUS_HTTP_TIMEOUT:
				// 设置已经提示用户网络状态
				app.setHasPromptNetwork(true);

				// 连接超时
				Utils.showToast(app.getStack().lastElement(), app.getString(R.string.jh_state_http_timeout), Toast.LENGTH_SHORT);
				break;

			case NetConfig.STATUS_HTTP_NOT_FOUND:
				// 设置已经提示用户网络状态
				app.setHasPromptNetwork(true);

				// 404
				Utils.showToast(app.getStack().lastElement(), app.getString(R.string.jh_state_http_not_found), Toast.LENGTH_SHORT);
				break;

			case SystemConfig.SYSTEM_CHECK_UPGRADE:
				UpgradeBean bean = (UpgradeBean) msg.obj;
				String versionName = Utils.getAppVersionName(this.app);
				if (!TextUtils.equals(versionName, bean.getVersionName())) {
					String className = app.getStack().lastElement().getClass().getSimpleName();
					Log.e(TAG, className);
					Log.e(TAG, app.getStack().toString());
					if ("SplashAppCompatActivity".equals(className)) {
						// 处于启动界面，设置在主界面升级
						isNeedUpgrade = true;
						this.bean = bean;
						// 在当前页面启动更新界面
					} else {
						if (dialog != null) {
							dialog.dismiss();       // 关闭升级查询界面
						}

					}
				}
				break;

			case SystemConfig.UPDATE_DOWNLOAD_PROGRESS:
				String string = String.format(app.getString(R.string.jh_schedule_progress_content),
						msg.arg1) + "%";
				TextView textView = (TextView) msg.obj;
				textView.setText(string);
				break;

			case NetConfig.FINISH_UPLOAD:
				Utils.showToast(app.getStack().lastElement(), "完成上传!", Toast.LENGTH_SHORT);
				break;

			default:
		}
	}

	protected void myStartActivity(Intent intent) {
		Bundle bundle = new Bundle();
		bundle.putBoolean("isNeedUpgrade", isNeedUpgrade);

		// 是否需要升级
		if (isNeedUpgrade) {
			bundle.putParcelable("upgrade", bean);
		}
		intent.putExtras(bundle);
		app.getStack().lastElement().startActivity(intent);
		// 不加入activity栈中，防止在主界面后退回到此界面
		app.getStack().lastElement().finish();
	}

	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}
}
