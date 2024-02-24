package com.emagine.ussd.dao;

import org.apache.log4j.Logger;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcedureCallback;

public class USSDDAOCallback implements ProcedureCallback {
    private static final Logger logger = Logger.getLogger(USSDDAOCallback.class);

    @Override
    public void clientCallback(ClientResponse clientResponse) throws Exception {
        if (clientResponse.getStatus() != ClientResponse.SUCCESS) {
            logger.error("clientCallback :: " + clientResponse.getStatusString());
        }
    }
}
