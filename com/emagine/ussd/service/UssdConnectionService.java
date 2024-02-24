package com.emagine.ussd.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.emagine.ussd.connection.TransmitMessage;
import com.emagine.ussd.connection.UssdConnectionFactory;
import com.emagine.ussd.model.UserInfo;

@Component
public class UssdConnectionService implements IUssdConnectionService {

    private static final Logger LOG = Logger.getLogger(UssdConnectionService.class);

    private UssdConnectionFactory ussdConnectionFactory;

    public UssdConnectionService() {
        try {
            ussdConnectionFactory = new UssdConnectionFactory();
        } catch (Exception e) {
            LOG.error("Error occured at" + "::", e);
        }
    }

    public static UssdConnectionService getInstance() {
        return USSDConnectionServiceHolder.INSTANCE;
    }

    @Override
    public String open() throws Exception {
        LOG.info(" UssdConnectionFactory => Defining new Tags");
        ussdConnectionFactory.bind();
        LOG.debug("Connection Status after binding [2=SUCCESSFULY BINDED] = " + ussdConnectionFactory.getConnectionStatus());
        return "" + ussdConnectionFactory.getConnectionStatus();
    }

    @Override
    public String close() throws Exception {
        LOG.debug("USSD Connection Unbinding");
        ussdConnectionFactory.unbind();
        ussdConnectionFactory.shutdown();
        LOG.debug("Connection Status after Unbinding:::" + ussdConnectionFactory.getConnectionStatus());
        return "SUCCESS";
    }

    @Override
    public void sendMesage(UserInfo userInfo) throws Exception {
        ussdConnectionFactory.sendMessage(userInfo);
    }
    
    @Override
    public void sendMesage(TransmitMessage message) throws Exception {
        ussdConnectionFactory.sendMessage(message);
    }

    private static class USSDConnectionServiceHolder {
        private static final UssdConnectionService INSTANCE = new UssdConnectionService();
    }

}
