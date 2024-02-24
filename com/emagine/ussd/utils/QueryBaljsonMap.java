package com.emagine.ussd.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

public class QueryBaljsonMap {

	private Map<Integer, JSONObject> queryBaljsonMap = new ConcurrentHashMap<>();

    /**
     * @return
     */
    public static QueryBaljsonMap instance() {
        return InstanceHolder.instance;
    }

    /**
     * @param key
     * @return
     */
    public JSONObject get(Integer key) {
        return this.queryBaljsonMap.get(key);
    }

    /**
     * @param key
     * @param value
     */
    public void put(Integer key, JSONObject value) {
        this.queryBaljsonMap.put(key, value);
    }

    /**
     * @param key
     */
    public void remove(Integer key) {
        this.queryBaljsonMap.remove(key);
    }

    private static class InstanceHolder {
        private static QueryBaljsonMap instance = new QueryBaljsonMap();
    }
}
