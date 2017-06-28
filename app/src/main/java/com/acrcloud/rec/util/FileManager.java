package com.acrcloud.rec.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jiahelogistic.R;
import com.jiahelogistic.bean.KeyValue;
import com.jiahelogistic.config.NetConfig;
import com.jiahelogistic.config.SystemConfig;
import com.jiahelogistic.handler.BasicNetworkHandler;
import com.jiahelogistic.utils.Utils;
import com.jiahelogistic.widget.CustomDialog;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * @author Li Huanling on 2016/7/12 13:16.
 *
 * 文件上传下载工具类，使用Okhttp3.2
 */
public class FileManager {
	/**
	 * 媒体类型
	 */
	private static final MediaType MEDIA_TYPE_MARKDOWN
					= MediaType.parse("text/x-markdown; charset=utf-8");

	/**
	 * OkHttp实例
	 */
	private static final OkHttpClient client = new OkHttpClient();


	/**
	 * 异步提交数据
	 * @param file 需要上传的文件
	 * @param param 额外参数，键值对数组
	 * @param handler 处理返回结果
	 */
	public static final void asynPost(File file, KeyValue[] param, final BasicNetworkHandler handler) {
		MultipartBody.Builder builder =  new MultipartBody.Builder();
		builder.setType(MultipartBody.FORM);
		builder.addFormDataPart("fileData", file.getName(), RequestBody.create(MEDIA_TYPE_MARKDOWN, file));

		// 如果额外参数不为空
		if (param != null) {
			for (KeyValue kv : param) {
				// 循环添加参数
				builder.addFormDataPart(kv.getKey(), kv.getValue());
			}
		}
		MultipartBody body = builder.build();

		Request request = new Request.Builder()
				.url(NetConfig.URL_CRASH_FILE_UPLOAD)
				.post(body)
				.build();

		client.newCall(request).enqueue(new BasicResponseError(handler) {
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					// 响应失败
					Log.e("asynPost", "NOT FOUND");
					msg.what = NetConfig.STATUS_HTTP_NOT_FOUND;
					handler.sendMessage(msg);
					return;
				}
				Headers responseHeaders = response.headers();
				for (int i = 0; i < responseHeaders.size(); i++) {
					//Log.e("UploadFileHeader", responseHeaders.runned(i) + ": " + responseHeaders.value(i));
				}

				Log.e("UploadFile", response.body().string());
				msg.what = NetConfig.STATUS_HTTP_OK;
				msg.obj = response.body();
				handler.sendMessage(msg);
			}
		});
	}

	/**
	 * 带进度条的下载
	 *
	 * @param context 上下文
	 * @param url 文件地址
	 * @param filePath 下载的文件路径
	 * @param handler 消息处理
	 */
	public static void downloadFileWithProgressBar(final Context context, String url, final String filePath,
	                                               final BasicNetworkHandler handler) {
		assert url == null;
		// 创建新的对话框
		final Dialog dialog = CustomDialog.createCommonScheduleProgressDialog(context, "已下载");

		// 进度条
		final ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.jh_dialog_schedule_progressBar);

		// 文字信息
		final TextView textView = (TextView) dialog.findViewById(R.id.jh_dialog_schedule_content);

		// 开始下载，清空下载记录
		final long breakPoints = 0L;
		ProgressDownloader downloader = new ProgressDownloader(url, filePath, new ProgressResponseBody.ProgressListener() {

			/**
			 * 真实文件
			 */
			private File trueFile;

			/**
			 * 文件长度
			 */
			private long contentLength = 0L;

			/**
			 * 在开始下载前调用，设置文件的长度，只调用一次
			 *
			 * @param contentLength 文件长度
			 */
			@Override
			public void onPreExecute(long contentLength) {
				this.contentLength = contentLength;
				progressBar.setMax((int) (contentLength / 1024));
			}

			/**
			 * 设置真实文件
			 *
			 * @param trueFile 真实文件
			 */
			@Override
			public void onGetTrueFile(File trueFile) {
				this.trueFile = trueFile;
			}

			/**
			 * 更新文件下载进度
			 *
			 * @param totalBytes 已下载的长度
			 * @param done       是否完成
			 */
			@Override
			public void update(long totalBytes, boolean done) {
				totalBytes = totalBytes + breakPoints;
				progressBar.setProgress((int) (totalBytes / 1024));

				// 通知主线程更新文本进度
				Message msg = Message.obtain();
				msg.what = SystemConfig.UPDATE_DOWNLOAD_PROGRESS;
				msg.obj = textView;
				msg.arg1 = (int) Math.floor(totalBytes * 100 / contentLength);
				handler.sendMessage(msg);

				// 下载完成
				if (done) {
					// 切换到主线程
					rx.Observable.empty().observeOn(AndroidSchedulers.mainThread())
							.doOnCompleted(new Action0() {
								@Override
								public void call() {
									dialog.dismiss();
									if (trueFile.getName().endsWith("apk")) {
										Intent install = new Intent();
										install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										install.setAction(Intent.ACTION_VIEW);
										install.setDataAndType(Uri.fromFile(trueFile),"application/vnd.android.package-archive");
										context.startActivity(install);
									} else {
										Utils.showToast(context, "下载完成", Toast.LENGTH_SHORT);
									}
								}
							}).subscribe();
				}

			}
		});
		downloader.download(0L);
		dialog.show();
	}
}
