/**
 *
 */
package com.emagine.ussd.utils;

import static com.emagine.ussd.utils.USSDConstants.J4U_SOCIAL_PRODINFO_PROC_NAME;
import static com.emagine.ussd.utils.USSDConstants.USSD_ML_MORNING_OFFER_PRODINFO_PROC;
import static com.emagine.ussd.utils.USSDConstants.USSD_PRODINFO_PROC_NAME;
import static com.emagine.ussd.utils.USSDConstants.USSD_ML_J4U_TOWN_OFFER_PRODINFO_PROC;
import static com.emagine.ussd.utils.USSDConstants.USSD_ML_J4U_CELLID_TOWNNAME_MAP_PROC;
import static com.emagine.ussd.utils.USSDConstants.USSD_ML_GET_TOWN_PRODUCT_IDS_PROC;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.apache.log4j.Logger;
import com.emagine.ussd.config.PropertiesLoader;
import com.emagine.ussd.dao.LookUpDAO;
import com.emagine.ussd.model.ProductInfo;
import com.emagine.ussd.model.TownProdInfo;

/**
 *
 */
public class ProductInfoCache {
	private static final Logger LOGGER = Logger.getLogger(ProductInfoCache.class);
	private final Map<String, ProductInfo> prodInfoCache;
	private final Map<String, ProductInfo> locationCache;
	private final Map<String, ProductInfo> socialCache;
	private final Map<String, ProductInfo> morningCache;
	private final Map<String, List<String>> prodTypeCache;
	private final Map<String, List<String>> locationProdTypeCache;
	private final Map<String, List<String>> nonPromotionalProductInfo;
	private final Map<String, ProductInfo> townCache;
	private final List<String> totTownProdIdList;
	private final Map<String, TownProdInfo> cellIdTownMapCache;
	private final Map<String, List<String>> townProdIdMapCache;
	private final List<String> socialProdList;
	private final List<String> morningProdList;
	private static LocalDateTime cacheReloadTime;

	private final BiConsumer<Map<String, ProductInfo>, Map<String, List<String>>> initProductTypeCache = (cachemap,
			cachetype) -> cachemap.forEach((key, value) -> {
				ProductInfo productInfo = value;
				if (productInfo.getLangCode() == 1) {
					cachetype.computeIfAbsent(productInfo.getProductType(), k -> new ArrayList<>())
							.add(productInfo.getProductID());
				}
			});

	private ProductInfoCache() {
		prodInfoCache = new HashMap<>();
		locationCache = new HashMap<>();
		socialCache = new HashMap<>();
		morningCache = new HashMap<>();
		prodTypeCache = new HashMap<>();
		locationProdTypeCache = new HashMap<>();
		socialProdList = new ArrayList<>();
		morningProdList = new ArrayList<>();
		nonPromotionalProductInfo = new HashMap<>();
		townCache = new HashMap<>();
		totTownProdIdList = new ArrayList<>();
		cellIdTownMapCache = new HashMap<>();
		townProdIdMapCache = new HashMap<>();
		try {
			init();
		} catch (Exception ex) {
			LOGGER.error("Error occured at ==> ", ex);
		}
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Cache reloaded at - ");
		stringBuilder.append(cacheReloadTime.toString());
		stringBuilder.append(" | ML ProdInfo Cache total records - ");
		stringBuilder.append(prodInfoCache.size());
		stringBuilder.append(" | Location ProdInfo Cache total records - ");
		stringBuilder.append(locationCache.size());
		stringBuilder.append(" | Social ProdInfo Cache total records - ");
		stringBuilder.append(socialCache.size());
		stringBuilder.append(" | Morning Offer ProdInfo Cache total records - ");
		stringBuilder.append(morningCache.size());
		stringBuilder.append(" | J4U Town Offer ProdInfo Cache total records - ");
		stringBuilder.append(townCache.size());

		return stringBuilder.toString();
	}

	public static ProductInfoCache instance() {
		return InstanceHolder.instance;
	}

	public ProductInfo get(String key) {
		ProductInfo productInfo;
		productInfo = prodInfoCache.get(key);
		if (null == productInfo) {
			productInfo = locationCache.get(key);
		}
		if (null == productInfo) {
			productInfo = socialCache.get(key);
		}
		if (null == productInfo) {
			productInfo = morningCache.get(key);
		}
		if (null == productInfo) {
			productInfo = townCache.get(key);
		}
		return productInfo;
	}

	public ProductInfo getML(String key) {
		return prodInfoCache.get(key);
	}

	public ProductInfo getLocation(String key) {
		return locationCache.get(key);
	}

	public ProductInfo getSocial(String key) {
		return socialCache.get(key);
	}

	public ProductInfo getMorning(String key) {
		return morningCache.get(key);
	}

	public ProductInfo getTownProdInfo(String key) {
		return townCache.get(key);
	}

	public TownProdInfo getCellIdTownName(String key) {
		return cellIdTownMapCache.get(key);
	}

	public List<String> getProductIdsList(String key) {
		return prodTypeCache.get(key);
	}

	public List<String> getLocationProdIds(String category, String poolId, int langCode) {
		List<String> prodIdsList = new ArrayList<>();
		locationCache.forEach((key, value) -> {
			ProductInfo productInfo = value;
			if (langCode == productInfo.getLangCode() && poolId.equalsIgnoreCase(productInfo.getPoolID())
					&& category.equalsIgnoreCase(productInfo.getProductType())) {
				prodIdsList.add(productInfo.getProductID());
			}
		});

		return prodIdsList;
	}

	public List<String> getSocialProductIdsList() {
		return socialProdList;
	}

	public List<String> getTownProductIdsMapList(String townName) {
		return townProdIdMapCache.get(townName);
	}

	public List<String> getNonPromotionalOffer(String key) {
		return nonPromotionalProductInfo.get(key);
	}

	public synchronized void reloadCache() {
		try {
			init();
		} catch (Exception ex) {
			LOGGER.error("Error occured at ==> ", ex);
		}
	}

	private synchronized void init() throws Exception {
		LOGGER.info("ProductInfoCache - Caching START");
		cacheReloadTime = LocalDateTime.now();
		LookUpDAO dao = new LookUpDAO();
		Map<String, ProductInfo> prodInfoMap = dao.getProductInfoMap(PropertiesLoader.getValue(USSD_PRODINFO_PROC_NAME));
		Map<String, ProductInfo> locationProdInfoMap = dao.getLocationProductInfoMap();
		Map<String, ProductInfo> socialProdInfoMap = dao.getProductInfoMap(PropertiesLoader.getValue(J4U_SOCIAL_PRODINFO_PROC_NAME));
		Map<String, ProductInfo> morningProdInfoMap = dao.getMorningOfferProductInfoMap(PropertiesLoader.getValue(USSD_ML_MORNING_OFFER_PRODINFO_PROC));
		Map<String, List<String>> nonPromotionInfoList = dao.getProductNonPromotionalInfo();
		Map<String, ProductInfo> townProdInfoMap = dao.getTownOfferProductInfoMap(PropertiesLoader.getValue(USSD_ML_J4U_TOWN_OFFER_PRODINFO_PROC));
		Map<String, TownProdInfo> cellIdTownMap = dao.getCellIdTownNameDetails(PropertiesLoader.getValue(USSD_ML_J4U_CELLID_TOWNNAME_MAP_PROC));
		Map<String, List<String>> townProductIdMap = dao.getTownProdIdDetails(PropertiesLoader.getValue(USSD_ML_GET_TOWN_PRODUCT_IDS_PROC));

		checkDataExistence(prodInfoMap, "ECMP_T_PROD_INFO & ECMP_T_LOCATION_PROD_INFO & ECMP_T_SOCIAL_PROD_INFO");
		checkDataExistence(locationProdInfoMap, "ECMP_T_LOCATION_PROD_INFO");
		checkDataExistence(socialProdInfoMap, "ECMP_T_SOCIAL_PROD_INFO");
		checkDataExistence(morningProdInfoMap, "ECMP_T_MORNING_OFFER_INFO");
		checkDataExistence(nonPromotionInfoList, "ECMP_T_PROD_INFO");
		checkDataExistence(townProdInfoMap, "ECMP_T_J4U_TOWN_OFFER_INFO");
		checkDataExistence(cellIdTownMap, "ENBA_T_J4U_CELLID_TOWNNAME_MAP");
		checkDataExistence(townProductIdMap, "ENBA_T_J4U_TOWNNAME_TOWNOFFERS_MAP");

		updateCache(prodInfoMap, prodInfoCache);
		updateCache(locationProdInfoMap, locationCache);
		updateSocialProdIdCache(socialProdInfoMap);
		updateMorningProdIdCache(morningProdInfoMap);
		updateTownProdIdCache(townProdInfoMap);
		updateCache(cellIdTownMap, cellIdTownMapCache);
		updateCache(townProductIdMap, townProdIdMapCache);

		LOGGER.info("J4U cellIdTownMap Cache END -Total cellId Record :: " + (cellIdTownMapCache != null ? cellIdTownMapCache.size() : 0));
		LOGGER.info("J4U townProductIdMap Cache END -Total Town Records :: " + (townProdIdMapCache != null ? townProdIdMapCache.size() : 0));

		prodTypeCache.clear();
		locationProdTypeCache.clear();
		initProductTypeCache.accept(prodInfoCache, prodTypeCache);
		LOGGER.debug("Product Type Cache - " + prodTypeCache.toString());
		initProductTypeCache.accept(locationCache, locationProdTypeCache);
		LOGGER.debug("Location Product Type Cache - " + locationProdTypeCache.toString());

		updateCache(nonPromotionInfoList, nonPromotionalProductInfo);
		
		LOGGER.info("ProductInfoCache - Caching END");
	}

	private <K, V> void updateCache(Map<K, V> data, Map<K, V> cache) {
		if (data != null && !data.isEmpty()) {
			cache.clear();
			cache.putAll(data);
		}
	}

	private void checkDataExistence(Map<?, ?> data, String tableName) {
		if (data == null || data.isEmpty()) {
			LOGGER.error("Data not configured/table is empty :: [" + tableName + "]");
		}
	}

	private void updateSocialProdIdCache(Map<String, ProductInfo> socialProdInfoMap) {
		if (socialProdInfoMap != null && !socialProdInfoMap.isEmpty()) {
			socialCache.clear();
			socialCache.putAll(socialProdInfoMap);
			socialProdList.clear();
			socialProdInfoMap.forEach((k, v) -> {
				if (v.getLangCode() == 1)
					socialProdList.add(v.getProductID());
			});
			LOGGER.debug("Social Product Type Cache - " + socialProdList);
		}
	}

	private void updateMorningProdIdCache(Map<String, ProductInfo> morningProdInfoMap) {
		if (morningProdInfoMap != null && !morningProdInfoMap.isEmpty()) {
			morningCache.clear();
			morningCache.putAll(morningProdInfoMap);
			morningProdList.clear();
			morningProdInfoMap.forEach((k, v) -> {
				if (v.getLangCode() == 1)
					morningProdList.add(v.getProductID());
			});
			LOGGER.debug("Morning Offer Product Type Cache - " + morningProdList);
		}
	}

	private void updateTownProdIdCache(Map<String, ProductInfo> townProdInfoMap) {
		if (townProdInfoMap != null && !townProdInfoMap.isEmpty()) {
			townCache.clear();
			townCache.putAll(townProdInfoMap);
			totTownProdIdList.clear();
			townProdInfoMap.forEach((k, v) -> {
				if (v.getLangCode() == 1)
					totTownProdIdList.add(v.getProductID());
			});
			LOGGER.debug("J4U Town ProductIds Cache - " + totTownProdIdList);
		}
	}

	private static class InstanceHolder {
		private static ProductInfoCache instance = new ProductInfoCache();
	}
}
