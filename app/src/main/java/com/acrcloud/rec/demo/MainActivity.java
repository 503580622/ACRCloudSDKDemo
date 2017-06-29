package com.acrcloud.rec.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.acrcloud.rec.JiaHeLogistic;
import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;
import com.acrcloud.rec.util.BasicNetworkHandler;
import com.acrcloud.rec.util.FileManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends Activity implements IACRCloudListener {
	private static final int NECESSARY_PERMISSION = 100;
	//NOTE: You can also implement IACRCloudResultWithAudioListener, replace "onResult(String result)" with "onResult(ACRCloudResult result)"

	private ACRCloudClient mClient;
	private ACRCloudConfig mConfig;

	private TextView mVolume, mResult, tv_time;

	private boolean mProcessing = false;
	private boolean initState = false;

	private String path = "";

	private long startTime = 0;
	private long stopTime = 0;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			Uri uri = data.getData();
			if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
				path = uri.getPath();
				//Toast.makeText(this, path + "11111", Toast.LENGTH_SHORT).show();
				FileManager.asynPost(this, new File(path), null, new BasicNetworkHandler(JiaHeLogistic.getInstance()));
				return;
			}
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
				path = getPath(this, uri);
				Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
			} else {//4.4以下下系统调用方法
				path = getRealPathFromURI(uri);
				//Toast.makeText(MainActivity.this, path + "222222", Toast.LENGTH_SHORT).show();
			}
			JiaHeLogistic.getInstance().getStack().push(this);
			FileManager.asynPost(this, new File(path), null, new BasicNetworkHandler(JiaHeLogistic.getInstance()));
			Log.e("path:", path);
		}
	}

	public String getRealPathFromURI(Uri contentUri) {
		String res = null;
		String[] proj = {MediaStore.Images.Media.DATA};
		Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
		if (null != cursor && cursor.moveToFirst()) {
			;
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			res = cursor.getString(column_index);
			cursor.close();
		}
		return res;
	}

	@SuppressLint("NewApi")
	public String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{split[1]};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context       The context.
	 * @param uri           The Uri to query.
	 * @param selection     (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public String getDataColumn(Context context, Uri uri, String selection,
	                            String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {column};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		requestPermission(new String[]{"android.permission.RECORD_AUDIO", "android.permission.READ_EXTERNAL_STORAGE"});

		path = Environment.getExternalStorageDirectory().toString()
				+ "/acrcloud/model";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}

		mVolume = (TextView) findViewById(R.id.volume);
		mResult = (TextView) findViewById(R.id.result);
		tv_time = (TextView) findViewById(R.id.time);

		Button startBtn = (Button) findViewById(R.id.start);
		startBtn.setText(getResources().getString(R.string.start));

		Button stopBtn = (Button) findViewById(R.id.stop);
		stopBtn.setText(getResources().getString(R.string.stop));

		Button chooseBtn = (Button) findViewById(R.id.choose_file);
		chooseBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*"); //选择音频
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				startActivityForResult(intent, 1);
			}
		});

		findViewById(R.id.stop).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						stop();
					}
				});

		Button cancelBtn = (Button) findViewById(R.id.cancel);
		cancelBtn.setText(getResources().getString(R.string.cancel));

		findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				start();
			}
		});

		findViewById(R.id.cancel).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						cancel();
					}
				});


		this.mConfig = new ACRCloudConfig();
		this.mConfig.acrcloudListener = this;

		// If you implement IACRCloudResultWithAudioListener and override "onResult(ACRCloudResult result)", you can get the Audio data.
		//this.mConfig.acrcloudResultWithAudioListener = this;

		this.mConfig.context = this;
		this.mConfig.host = "cn-north-1.api.acrcloud.com";
		this.mConfig.dbPath = path; // offline db path, you can change it with other path which this app can access.
		this.mConfig.accessKey = "28d1eb21bbb565e746281ff99bc3bb04";
		this.mConfig.accessSecret = "9b67PoDPE8OZ0ALPJsHjUgd3a9yLLRrcH8lO91rU";
		this.mConfig.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTP; // PROTOCOL_HTTPS
		this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;
		//this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_LOCAL;
		//this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_BOTH;

		this.mClient = new ACRCloudClient();
		// If reqMode is REC_MODE_LOCAL or REC_MODE_BOTH,
		// the function initWithConfig is used to load offline db, and it may cost long time.
		this.initState = this.mClient.initWithConfig(this.mConfig);
		if (this.initState) {
			this.mClient.startPreRecord(3000); //start prerecord, you can call "this.mClient.stopPreRecord()" to stop prerecord.
		}
	}


	/**
	 * 请求权限
	 *
	 * @param permissions 权限数组
	 */
	protected void requestPermission(String[] permissions) {
		// 检测没有的权限
		ArrayList<String> permissionDenied = new ArrayList<String>();

		for (String permission : permissions) {
			if (ContextCompat.checkSelfPermission(this, permission)
					!= PackageManager.PERMISSION_GRANTED) {
				// 没有权限，添加到未申请数组
				permissionDenied.add(permission);
			}
		}

		// 统一申请必须的权限
		String[] requestPermissions = new String[permissionDenied.size()];
		permissionDenied.toArray(requestPermissions);
		if (requestPermissions.length > 0) {
			ActivityCompat.requestPermissions(this, requestPermissions, NECESSARY_PERMISSION);
		}
	}


	public void start() {
		if (!this.initState) {
			Toast.makeText(this, "init error", Toast.LENGTH_SHORT).show();
			return;
		}

		if (!mProcessing) {
			mProcessing = true;
			mVolume.setText("");
			mResult.setText("");
			if (this.mClient == null || !this.mClient.startRecognize()) {
				mProcessing = false;
				mResult.setText("start error!");
			}
			startTime = System.currentTimeMillis();
		}
	}

	protected void stop() {
		if (mProcessing && this.mClient != null) {
			this.mClient.stopRecordToRecognize();
		}
		mProcessing = false;

		stopTime = System.currentTimeMillis();
	}

	protected void cancel() {
		if (mProcessing && this.mClient != null) {
			mProcessing = false;
			this.mClient.cancel();
			tv_time.setText("");
			mResult.setText("");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Old api
	@Override
	public void onResult(String result) {
		if (this.mClient != null) {
			this.mClient.cancel();
			mProcessing = false;
		}

		String tres = "\n";

		try {
			JSONObject j = new JSONObject(result);
			JSONObject j1 = j.getJSONObject("status");
			int j2 = j1.getInt("code");
			if (j2 == 0) {
				JSONObject metadata = j.getJSONObject("metadata");
				//
				if (metadata.has("humming")) {
					JSONArray hummings = metadata.getJSONArray("humming");
					for (int i = 0; i < hummings.length(); i++) {
						JSONObject tt = (JSONObject) hummings.get(i);
						String title = tt.getString("title");
						JSONArray artistt = tt.getJSONArray("artists");
						JSONObject art = (JSONObject) artistt.get(0);
						String artist = art.getString("name");
						tres = tres + (i + 1) + ".  " + title + "\n";
					}
				}
				if (metadata.has("music")) {
					JSONArray musics = metadata.getJSONArray("music");
					for (int i = 0; i < musics.length(); i++) {
						JSONObject tt = (JSONObject) musics.get(i);
						String title = tt.getString("title");
						JSONArray artistt = tt.getJSONArray("artists");
						JSONObject art = (JSONObject) artistt.get(0);
						String artist = art.getString("name");
						tres = tres + (i + 1) + ".  Title: " + title + "    Artist: " + artist + "\n";
					}
				}
				if (metadata.has("streams")) {
					JSONArray musics = metadata.getJSONArray("streams");
					for (int i = 0; i < musics.length(); i++) {
						JSONObject tt = (JSONObject) musics.get(i);
						String title = tt.getString("title");
						String channelId = tt.getString("channel_id");
						tres = tres + (i + 1) + ".  Title: " + title + "    Channel Id: " + channelId + "\n";
					}
				}
				if (metadata.has("custom_files")) {
					JSONArray musics = metadata.getJSONArray("custom_files");
					for (int i = 0; i < musics.length(); i++) {
						JSONObject tt = (JSONObject) musics.get(i);
						String title = tt.getString("title");
						tres = tres + (i + 1) + ".  Title: " + title + "\n";
					}
				}
				tres = tres + "\n\n" + result;
			} else {
				tres = result;
			}
		} catch (JSONException e) {
			tres = result;
			e.printStackTrace();
		}

		mResult.setText(tres);
	}

	@Override
	public void onVolumeChanged(double volume) {
		long time = (System.currentTimeMillis() - startTime) / 1000;
		mVolume.setText(getResources().getString(R.string.volume) + volume + "\n\n录音时间：" + time + " s");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e("MainActivity", "release");
		if (this.mClient != null) {
			this.mClient.release();
			this.initState = false;
			this.mClient = null;
		}
	}

	public boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	public boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	public boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}
}
