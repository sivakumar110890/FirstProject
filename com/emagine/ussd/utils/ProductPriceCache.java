/**
 *
 */
package com.emagine.ussd.utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.emagine.ussd.dao.LookUpDAO;
import com.emagine.ussd.exception.USSDPluginException;

/**
 *
 */
public class ProductPriceCache {
    private static final Logger LOGGER = Logger.getLogger(ProductPriceCache.class);
    private final Map<String, String> cache;
    private static LocalDateTime cacheReloadTime ;

    private ProductPriceCache() {
        cache = new HashMap<>();
        try {
            init();
        } catch (Exception ex) {
            LOGGER.error("Error occured at ==> ", ex);
        }
    }

    public void reloadCache () {
        try {
            init();
        } catch (Exception ex) {
            LOGGER.error("Error occured at ==> ", ex);
        }
    }

    public String toString () {
        StringBuilder stringBuilder = new StringBuilder() ;
        stringBuilder.append("Cache reloaded at - ") ;
        stringBuilder.append(cacheReloadTime.toString()) ;
        stringBuilder.append(" | Total records - ") ;
        stringBuilder.append(cache.size());

        return  stringBuilder.toString() ;
    }

    public static ProductPriceCache instance() {
        return InstanceHolder.instance;
    }

    public String get(String key) {
        return cache.get(key);
    }

    private synchronized void init() throws Exception {
        LOGGER.info("ProductPriceCache - START");
        cacheReloadTime = LocalDateTime.now() ;
        LookUpDAO dao = new LookUpDAO();
        Map<String, String> productPriceMap = dao.getProductPriceMap();
        if (null != productPriceMap) {
            if (null != cache) {
                cache.clear();
                cache.putAll(productPriceMap);
            }

        } else {
            throw new USSDPluginException("Data not configured/table is empty :: [ECMP_T_PRODUCT_PRICE]");
        }
        LOGGER.info("ProductPriceCache - END - Total Records - " + (cache != null  ? cache.size() : 0));

    }

    private static class InstanceHolder {
        private static ProductPriceCache instance = new ProductPriceCache();
    }
}