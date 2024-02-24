/**
 *
 */
package com.emagine.ussd.connection;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author udaykapavarapu
 *
 */
public class EnquiryLinkTime {
    private static AtomicLong enquireLinkResponseRecievedTime = new AtomicLong(System.currentTimeMillis());
    private static AtomicLong enquireLinkRequestTime = new AtomicLong(System.currentTimeMillis());
    private static AtomicInteger retryCount = new AtomicInteger(0);
    private static boolean enquiryRequestSent = false;
    private static boolean enquiryResponseRecieved = false;
    private EnquiryLinkTime() {
    }

    public static AtomicLong getEnquireLinkResponseRecievedTime() {
        return enquireLinkResponseRecievedTime;
    }

    public static AtomicLong getEnquireLinkRequestTime() {
        return enquireLinkRequestTime;
    }

    public static AtomicInteger getRetryCount() {
        return retryCount;
    }

    public static boolean isEnquiryRequestSent() {
        return enquiryRequestSent;
    }

    public static void setEnquiryRequestSent(boolean enquiryRequestSent) {
        EnquiryLinkTime.enquiryRequestSent = enquiryRequestSent;
    }

    public static void setEnquiryResponseRecieved(boolean enquiryResponseRecieved) {
        EnquiryLinkTime.enquiryResponseRecieved = enquiryResponseRecieved;
    }
}
