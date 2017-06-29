package com.acrcloud.rec.util;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Li Huanling on 2016/7/18 12:56.
 *
 * 应用升级信息
 */
public class UpgradeBean implements Parcelable {

	/**
	 * 版本号
	 */
	private int versionCode;

	/**
	 * 版本名
	 */
	private String versionName;

	/**
	 * 升级下载地址
	 */
	private String url;

	/**
	 * 升级描述，介绍
	 */
	private String description;

	/**
	 * 是否强制升级，如果是表示废弃旧版本
	 */
	private boolean isForce;

	/**
	 * 获取版本号
	 * @return 版本号
	 */
	public int getVersionCode() {
		return versionCode;
	}

	/**
	 * 设置版本号
	 * @param versionCode 版本号
	 */
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	/**
	 * 获取版本名
	 * @return 版本名
	 */
	public String getVersionName() {
		return versionName;
	}

	/**
	 * 设置版本名
	 * @param versionName 版本名
	 */
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	/**
	 * 获取下载地址
	 * @return 下载地址
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * 设置下载地址
	 * @param url 下载地址
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * 获取描述介绍信息
	 * @return 描述介绍信息
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 设置描述介绍信息
	 * @param description 描述介绍信息
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 强制升级
	 * @return 是否强制升级
	 */
	public boolean isIsForce() {
		return isForce;
	}

	@Override
	public String toString() {
		return versionName + String.valueOf(versionCode) + description + String.valueOf(isForce);
	}

	/**
	 * 设置是否强制升级
	 * @param isForce 是否强制升级
	 */
	public void setIsForce(boolean isForce) {
		this.isForce = isForce;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.versionCode);
		dest.writeString(this.versionName);
		dest.writeString(this.url);
		dest.writeString(this.description);
		dest.writeByte(this.isForce ? (byte) 1 : (byte) 0);
	}

	public UpgradeBean() {
	}

	protected UpgradeBean(Parcel in) {
		this.versionCode = in.readInt();
		this.versionName = in.readString();
		this.url = in.readString();
		this.description = in.readString();
		this.isForce = in.readByte() != 0;
	}

	public static final Creator<UpgradeBean> CREATOR = new Creator<UpgradeBean>() {
		@Override
		public UpgradeBean createFromParcel(Parcel source) {
			return new UpgradeBean(source);
		}

		@Override
		public UpgradeBean[] newArray(int size) {
			return new UpgradeBean[size];
		}
	};
}
