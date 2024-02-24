package com.emagine.ussd.config;

import static com.emagine.ussd.utils.USSDConstants.ONE_1;
import static com.emagine.ussd.utils.USSDConstants.USSD_BLACKLIST_ENABLED;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;

import com.comviva.voltdb.factory.DAOFactory;
import com.emagine.ussd.service.IUssdConnectionService;
import com.emagine.ussd.service.UssdConnectionService;
import com.emagine.ussd.service.consumer.ConsumerService;
import com.emagine.ussd.service.consumer.IConsumerService;
import com.emagine.ussd.service.publisher.UssdEventPublisher;
import com.emagine.ussd.utils.DefaultMessageCache;
import com.emagine.ussd.utils.ProductInfoCache;
import com.emagine.ussd.utils.TemplateCache;

@WebListener
public class Config implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(Config.class);

    private IUssdConnectionService ussdConnectionService;

    private IConsumerService consumerService;

    public Config() {
        ussdConnectionService = new UssdConnectionService();
        consumerService = new ConsumerService();
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {

        try {
            LOG.debug("Ussd process is starting");

            /*
             * Opening USSD connection - Binding to USSD GW
             */
            ussdConnectionService.open();

            /*
             * Caching the data
             */
            LOG.debug("Product caching  is starting => ");
            ProductInfoCache.instance();
            TemplateCache.instance();
            DefaultMessageCache.instance();

            /*
             * Starting the USSD consumer
             */

            consumerService.startUssdConsumer();

            /*
             * Starting the USSD publisher
             */
            UssdEventPublisher.startPublisher();
            LOG.info("isBlacklistEnabled : "+PropertiesLoader.getValue(USSD_BLACKLIST_ENABLED).equalsIgnoreCase(ONE_1));
            LOG.debug("Ussd process STARTED !!");
        } catch (Exception e) {
            LOG.error("Error is starting the Just For You Ussd Process", e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        try {
            LOG.debug("Ussd Process is stopping");
            LOG.info("Removing active session request details from ECMP_T_USSD_TRX_PRODID_MAP table.");
            /*
             * Closing the USSD connection - Unbinding from USSD GW
             */
            ussdConnectionService.close();

            /*
             * Stopping the USSD consumer
             */

            consumerService.stopUssdConsumer();

            /*
             * Closing the VOLT instances
             */
            DAOFactory.close();
            /*
             * Closing the USSD publisher
             */
            UssdEventPublisher.stopExecutorThread();
            LOG.debug("Ussd process STOPPED !!");
        } catch (Exception e) {
            LOG.error("Error in stopping the Ussd Process", e);
        }
    }

}
