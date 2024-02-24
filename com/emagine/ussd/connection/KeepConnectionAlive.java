/**
 *
 */
package com.emagine.ussd.connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ie.omk.smpp.Connection;
import ie.omk.smpp.message.EnquireLink;

/**
 * @author udaykapavarapu
 *
 */
public class KeepConnectionAlive implements Runnable {

    private static final Log LOG = LogFactory.getLog(KeepConnectionAlive.class);
    private Connection ussdConnection;

    public KeepConnectionAlive(Connection ussdConnection) {
        this.ussdConnection = ussdConnection;
    }

    public void run() {
        try {
            LOG.debug("Sending Enquiry Link Request");
            ussdConnection.sendRequest(new EnquireLink());
            LOG.debug("Connection Status is::" + ussdConnection.getState());
            EnquiryLinkTime.setEnquiryRequestSent(true);
            EnquiryLinkTime.getEnquireLinkRequestTime().set(System.currentTimeMillis());
        } catch (Throwable t) {
            LOG.error("Error in sending the Enquiry Link with thread-" + Thread.currentThread().getName(), t);
        }
    }
}