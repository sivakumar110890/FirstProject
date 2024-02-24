package com.emagine.ussd.utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.emagine.ussd.dao.LookUpDAO;
import com.emagine.ussd.exception.USSDPluginException;

public class CellIDPoolIDCache {
    private static final Logger LOGGER = Logger.getLogger(CellIDPoolIDCache.class);
    private final Map<String, String> cellPoolMapCache ;
    private static LocalDateTime cacheReloadTime ;

    private CellIDPoolIDCache () {
        cellPoolMapCache = new HashMap<>() ;
        try {
            init();
        } catch (Exception ex) {
            LOGGER.error("Error occured at ==> ", ex);
        }
    }

    public String getPoolIDForCellID (String cellID) {
        return cellPoolMapCache.get(cellID) ;
    }

    public synchronized void reloadCache () {
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
        stringBuilder.append(cellPoolMapCache.size());

        return  stringBuilder.toString() ;
    }

    public static CellIDPoolIDCache instance() {
        return InstanceHolder.instance;
    }


    public synchronized void init() throws Exception {
        LOGGER.info("CellIDPoolIDCache - Caching START");
        cacheReloadTime = LocalDateTime.now() ;
        LookUpDAO dao = new LookUpDAO ();
        Map<String, String> cellPoolMap = dao.getPoolIdForCellId();
        if (null != cellPoolMap) {
            if (null != cellPoolMapCache) {
                cellPoolMapCache.clear();
                cellPoolMapCache.putAll(cellPoolMap);
            }
        } else {
            throw new USSDPluginException("Could not find any cell ID vs Pool ID mapping");
        }
        LOGGER.info("CellIDPoolIDCache - Caching END - Total records in Cache -" + (cellPoolMapCache != null  ? cellPoolMapCache.size() : 0));
    }

    private static class InstanceHolder {
        private static CellIDPoolIDCache instance = new CellIDPoolIDCache();
    }
}
