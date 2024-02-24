package com.emagine.ussd.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.emagine.ussd.dao.LookUpDAO;
import com.emagine.ussd.exception.USSDPluginException;
import com.emagine.ussd.model.TemplateDTO;

/**
 * Cache for VOLT Tables
 */

public class TemplateCache {
	private static final Logger LOGGER = Logger.getLogger(TemplateCache.class);
	private final Map<String, TemplateDTO> cache;
	private final Map<String, TemplateDTO> mlTemplateCache;

	private TemplateCache() {
		cache = new HashMap<>();
		mlTemplateCache = new HashMap<>();
		try {
			init();
		} catch (Exception ex) {
			LOGGER.error("Error occured at ==> ", ex);
		}
	}

	public static TemplateCache instance() {
		return InstanceHolder.instance;
	}

	public synchronized void reloadUSSDTemplatesCache() {
		try {
			init();
		} catch (Exception ex) {
			LOGGER.error("Error occured at ==> ", ex);
		}
	}

	public TemplateDTO get(String key) {
		return cache.get(key);
	}

	public TemplateDTO getMLMenu(String key) {
		return mlTemplateCache.get(key);
	}

	private synchronized void init() throws Exception {
		LookUpDAO dao = new LookUpDAO();
		Map<String, TemplateDTO> cacheMap = dao.getTemplatesMap();
		Map<String, TemplateDTO> mlCacheMap = dao.getMLTemplatesMap();

		if (null != cacheMap) {
			if (null != cache) {
				cache.clear();
				cache.putAll(cacheMap);
			}
			LOGGER.info("TemplatesCache - END - Total Templates - " + (cache != null ? cache.size() : 0));

		} else {
			throw new USSDPluginException("Could not find any template data from database");
		}
		if (null != mlCacheMap) {
			if (null != mlTemplateCache) {
				mlTemplateCache.clear();
				mlTemplateCache.putAll(mlCacheMap);
			}
			LOGGER.info("MLTemplatesCache - END - Total Templates - "+ (mlTemplateCache != null ? mlTemplateCache.size() : 0));

		} else {
			throw new USSDPluginException("Could not find any template data from database for ML");
		}
	}

	private static class InstanceHolder {
		private static TemplateCache instance = new TemplateCache();
	}
}
