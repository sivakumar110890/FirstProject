package com.emagine.ussd.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.emagine.ussd.model.UserInfo;

/**
* UserInfoMapCache class
*/
public class UserInfoMapCache {
	private Map<Integer, UserInfo> userInfoMap = new ConcurrentHashMap<>();

	/**
	 * @return
	 */
	public static UserInfoMapCache instance() {
		return InstanceHolder.instance;
	}

	/**
	 * @param key
	 * @return
	 */
	public UserInfo get(Integer key) {
		return this.userInfoMap.get(key);
	}

	/**
	 * @param key
	 * @param value
	 */
	public void put(Integer key, UserInfo value) {
		this.userInfoMap.put(key, value);
	}

	/**
	 * @param key
	 */
	public void remove(Integer key) {
		this.userInfoMap.remove(key);
	}

	private static class InstanceHolder {
		private static UserInfoMapCache instance = new UserInfoMapCache();
	}
}
