package com.comviva.ped.utils;

import static com.comviva.ped.model.PEDCONSTANT.NO_PRIZE;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.comviva.ped.dao.PEDLookUPDAO;
import com.emagine.ussd.exception.USSDPluginException;
import com.emagine.ussd.model.PEDRandomPrizeInfo;

public class PEDRandomPrizesCache {
    private static final Logger LOGGER = Logger.getLogger(PEDRandomPrizesCache.class);
    private final List<PEDRandomPrizeInfo> randomPrizesCache ;
    private static LocalDateTime cacheReloadTime ;

    private PEDRandomPrizesCache () {
        randomPrizesCache = new ArrayList<>() ;
        try {
            init();
        } catch (Exception ex) {
            LOGGER.error("Error occured at ==> ", ex);
        }
    }

    public String getRandomPrize (int randomNum) {
        String prizeId = NO_PRIZE;

        for (PEDRandomPrizeInfo priceInfo: randomPrizesCache) {
            if (randomNum >= priceInfo.getMinRange() && randomNum <= priceInfo.getMaxRange()){
                prizeId = priceInfo.getPrizeID() ;
                break;
            }
        }
        return prizeId ;

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
        stringBuilder.append(randomPrizesCache.size());

        return  stringBuilder.toString() ;
    }

    public static PEDRandomPrizesCache instance() {
        return InstanceHolder.instance;
    }


    private synchronized void init() throws Exception {
        LOGGER.info("PEDRandomPrizesCache - Caching START");
        cacheReloadTime = LocalDateTime.now() ;
        PEDLookUPDAO dao = new PEDLookUPDAO ();
        List<PEDRandomPrizeInfo> randomPrizeInfoList = dao.getRandomPrize();
        if (null != randomPrizeInfoList) {
            if (null != randomPrizesCache) {
                randomPrizesCache.clear();
                randomPrizesCache.addAll(randomPrizeInfoList);
            }
        } else {
            throw new USSDPluginException("Could not find any record in Random Prize table");
        }
        LOGGER.info("PEDRandomPrizesCache - Caching END - Total records in Cache -" + (randomPrizesCache != null  ? randomPrizesCache.size() : 0));
    }

    private static class InstanceHolder {
        private static PEDRandomPrizesCache instance = new PEDRandomPrizesCache();
    }
}
