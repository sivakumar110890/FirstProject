package com.emagine.ussd.connection;

import java.util.HashMap;
import java.util.Map;

public class UssdSession {

    public static final int MO = 1;
    public static final int NO = 2;

    private static final int USSR_REQ = 2;
    private static final int USSN_REQ = 3;
    private static final int PSSR_RESP = 17;

    private final int sessionId;
    private final Map<Integer, Object> data = new HashMap<>();
    private final long createdTime;
    private final int notifyServiceOp;
    private final int requestServiceOp;

    private int sequenceNumber;
    private String transactionId;
    private String msisdn;
    private String cellId;
    private String poolId;

    public UssdSession(int sessionId, int sessionType) {
        sequenceNumber = 1;
        this.sessionId = sessionId;
        createdTime = System.currentTimeMillis();
        notifyServiceOp = (sessionType == MO) ? PSSR_RESP : USSN_REQ;
        requestServiceOp = USSR_REQ;
    }

    public UssdMessage createNotifyMessage() {
        UssdMessage message = new UssdMessage(UssdMessage.NOTIFY);
        message.setSessionId(sessionId);
        message.setServiceOp(notifyServiceOp);
        message.setTransactionId(transactionId);
        return message;
    }

    public UssdMessage createRequestMessage() {
        UssdMessage message = new UssdMessage(UssdMessage.REQUEST);
        message.setSessionId(sessionId);
        message.setServiceOp(requestServiceOp);
        message.setTransactionId(transactionId);
        return message;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void incrementSequenceNumber() {
        sequenceNumber++;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setData(int key, Object value) {
        data.put(key, value);
    }

    public Object getData(int key) {
        return data.get(key);
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * @return the msisdn
     */
    public String getMsisdn() {
        return msisdn;
    }

    /**
     * @param msisdn
     *            the msisdn to set
     */
    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public String getPoolId() {
        return poolId;
    }

    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }
}
