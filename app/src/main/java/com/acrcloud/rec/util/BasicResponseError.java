package com.acrcloud.rec.util;

import android.os.Message;
import android.util.Log;

import com.acrcloud.rec.JiaHeLogistic;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by Huanling on 2016/07/19 0:38.
 * Volley与Okhttp3通用失败处理，只处理失败情况
 */
public class BasicResponseError implements Response.ErrorListener, Callback {

	private static final String TAG = "BasicResponseError";

	/**
	 * 消息处理器
	 */
	protected BasicNetworkHandler mHandler;

	/**
	 * 消息
	 */
	protected Message msg;

	/**
	 * 全局应用
	 */
	protected JiaHeLogistic app;

	/**
	 * 实例化
	 *
	 * @param handler 消息处理
	 */
	public BasicResponseError(BasicNetworkHandler handler) {
		mHandler = handler;
		msg = Message.obtain();
		app = JiaHeLogistic.getInstance();
	}

	/**
	 * Callback method that an error has been occurred with the
	 * provided error code and optional user-readable message.
	 *
	 * @param error 错误信息
	 */
	@Override
	public void onErrorResponse(VolleyError error) {
		if (error.networkResponse != null) {
			Log.e(TAG, String.valueOf(error.networkResponse.statusCode));
		}

		// 失败处理
		callFailure();
	}

	/**
	 * Called when the request could not be executed due to cancellation, a connectivity problem or
	 * timeout. Because networks can fail during an exchange, it is possible that the remote server
	 * accepted the request before the failure.
	 *
	 * @param call 回调信息
	 * @param e 异常信息
	 */
	@Override
	public void onFailure(Call call, IOException e) {
		//Log.e(TAG, e.getMessage());

		// 失败处理
		callFailure();
	}

	/**
	 * 网络访问失败处理
	 */
	private void callFailure() {
		if (!app.isHasPromptNetwork()) {
			// 如果已经提示过用户，则忽略
			if (app.isHasNetwork()) {
				// 有网络，连接超时
				msg.what = NetConfig.STATUS_HTTP_TIMEOUT;
			} else {
				// 没有网络连接
				msg.what = NetConfig.STATUS_NO_INTERNET_CONNECTION;
			}
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 成功回调，由子类实现，不管
	 *
	 * @param call 回调信息
	 * @param response 响应
	 */
	@Override
	public void onResponse(Call call, okhttp3.Response response) throws IOException { }
}
