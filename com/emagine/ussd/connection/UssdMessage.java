package com.emagine.ussd.connection;

import ie.omk.smpp.message.DeliverSM;

public class UssdMessage {
    public static final int NOTIFY = 1;
    public static final int REQUEST = 2;
    public static final int NONE = 3;

    private final int messageType;
    private String sourceAddress;
    private String destinationAddress;
    private String messageText;
    private int serviceOp;
    private int sessionId;
    private String transactionId;
    private String prodIds;
    private String selProdId;
    private String cellId;
    private String poolId;
    private DeliverSM deliverSM;
    private String townName;

    public UssdMessage() {
        this(NONE);
    }

    public UssdMessage(int messageType) {
        this.messageType = messageType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public int getMessageType() {
        return messageType;
    }

    public int getServiceOp() {
        return serviceOp;
    }

    public void setServiceOp(int serviceOp) {
        this.serviceOp = serviceOp;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getProdIds() {
        return prodIds;
    }

    public void setProdIds(String prodIds) {
        this.prodIds = prodIds;
    }

    public String getSelProdId() {
        return selProdId;
    }

    public void setSelProdId(String selProdId) {
        this.selProdId = selProdId;
    }

    public void setDeliverSM(DeliverSM deliverSM) {
        this.deliverSM = deliverSM;
    }

    public Integer getDestinationTON() {
        return this.deliverSM == null ? null : this.deliverSM.getDestination().getTON();
    }

    public Integer getSourceTON() {
        return this.deliverSM == null ? null : this.deliverSM.getSource().getTON();
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

	public String getTownName() {
		return townName;
	}

	public void setTownName(String townName) {
		this.townName = townName;
	}
    
}