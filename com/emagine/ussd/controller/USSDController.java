package com.emagine.ussd.controller;

import static com.emagine.ussd.utils.USSDConstants.SUCCESS;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.comviva.ped.utils.PEDRandomPrizesCache;
import com.emagine.ussd.config.PropertiesLoader;
import com.emagine.ussd.utils.CellIDPoolIDCache;
import com.emagine.ussd.utils.ProductInfoCache;
import com.emagine.ussd.utils.ProductPriceCache;
import com.emagine.ussd.utils.RAGDefaultsCache;
import com.emagine.ussd.utils.TemplateCache;

/**
 *USSDController class
 */
@RestController
@RequestMapping("/ussd")
public class USSDController {

    private static final Logger LOGGER = Logger.getLogger(USSDController.class);

    @RequestMapping(value = "/reloadproperties", method = RequestMethod.GET)
    public String reloadUSSDProperties() throws Exception {
        String statusFlag = null;
        try {
            LOGGER.info("reloadUSSDProperties() method :: [Start] reloading the property file");
            statusFlag = PropertiesLoader.reloadUSSDProperties();
        } catch (Exception e) {
            LOGGER.error("reloadUSSDProperties method :: Error occured !!! " + e.getMessage() + "::", e);
        }
        LOGGER.info("reloadUSSDProperties() method :: [END] reloaded the property file and Status ==>  " + statusFlag);
        return statusFlag;
    }

    @RequestMapping(value = "/reloadtemplates", method = RequestMethod.GET)
    public String reloadUSSDTemplates() {
        TemplateCache templateCache = TemplateCache.instance() ;
        templateCache.reloadUSSDTemplatesCache();
        return SUCCESS;
    }

    @RequestMapping(value = "/reloadproductinfocache", method = RequestMethod.GET)
    public String reloadProductInfoCache () {
        ProductInfoCache productInfoCache = ProductInfoCache.instance() ;
        productInfoCache.reloadCache();
        return SUCCESS;
    }

    @RequestMapping(value = "/reloadproductpricecache", method = RequestMethod.GET)
    public String reloadProductPriceCache () {
        ProductPriceCache productPriceCache = ProductPriceCache.instance() ;
        productPriceCache.reloadCache();
        return SUCCESS;
    }

    @RequestMapping(value = "/reloadcellpoolidcache", method = RequestMethod.GET)
    public String reloadCellIDPoolIDCache () {
        CellIDPoolIDCache cellIDPoolIDCache = CellIDPoolIDCache.instance() ;
        cellIDPoolIDCache.reloadCache();
        return SUCCESS;
    }

    @RequestMapping(value = "/reloadrandomprizecache", method = RequestMethod.GET)
    public String reloadRandomPrizeCache () {
        PEDRandomPrizesCache pedRandomPrizesCache = PEDRandomPrizesCache.instance() ;
        pedRandomPrizesCache.reloadCache();
        return SUCCESS;
    }

    @RequestMapping(value = "/reloadragdefaultcache", method = RequestMethod.GET)
    public String reloadRAGDefaultCache () {
        RAGDefaultsCache ragDefaultsCache = RAGDefaultsCache.instance() ;
        ragDefaultsCache.reloadCache();
        return SUCCESS;
    }

    @RequestMapping(value = "/cacheinfo", method = RequestMethod.GET)
    public String cacheDetails () {
        StringBuilder stringBuilder = new StringBuilder() ;
        stringBuilder.append("ProdInfo Cache <br>") ;
        ProductInfoCache productInfoCache = ProductInfoCache.instance() ;
        stringBuilder.append(productInfoCache.toString()) ;
        stringBuilder.append("<br>Product Price Cache <br>") ;
        ProductPriceCache productPriceCache = ProductPriceCache.instance() ;
        stringBuilder.append(productPriceCache.toString()) ;
        stringBuilder.append("<br>CellID PoolID Cache<br>") ;
        CellIDPoolIDCache cellIDPoolIDCache = CellIDPoolIDCache.instance() ;
        stringBuilder.append(cellIDPoolIDCache.toString()) ;
        stringBuilder.append("<br>PED Random Prize Ranges Cache<br>") ;
        PEDRandomPrizesCache pedRandomPrizesCache = PEDRandomPrizesCache.instance() ;
        stringBuilder.append(pedRandomPrizesCache.toString()) ;

        return stringBuilder.toString() ;
    }


}
