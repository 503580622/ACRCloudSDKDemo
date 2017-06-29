package com.acrcloud.rec.util;

import android.util.Log;
import android.widget.Toast;

import com.acrcloud.rec.demo.MainActivity;

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
	 * @param activity 用来Toast
	 * @param file 需要上传的文件
	 * @param param 额外参数，键值对数组
	 * @param handler 处理返回结果
	 */
	public static final void asynPost(final MainActivity activity, File file, KeyValue[] param, final BasicNetworkHandler handler) {
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

				msg.what = NetConfig.FINISH_UPLOAD;
				handler.sendMessage(msg);
			}
		});
	}
}
