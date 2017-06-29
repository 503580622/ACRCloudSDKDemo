package com.acrcloud.rec.util;

/**
 * Created by Li Huanling on 2016/7/16 22:12.
 *
 * 键值对
 */
public class KeyValue {
	/**
	 * 键名
	 */
	public String key;

	/**
	 * 值
	 */
	public String value;

	/**
	 * 无参构造
	 */
	public KeyValue() {

	}

	/**
	 * 有参构造
	 * @param key 键名
	 * @param value 值
	 */
	public KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * 获取键名
	 * @return String
	 */
	public String getKey() {
		return key;
	}

	/**
	 * 设置键名
	 * @param key String
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * 获取值
	 * @return String
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 设置值
	 * @param value String
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
