package com.emagine.ussd.connection;

import static com.emagine.ussd.utils.USSDConstants.EIGHT;
import static com.emagine.ussd.utils.USSDConstants.EIGHTEEN;
import static com.emagine.ussd.utils.USSDConstants.ELEVEN;
import static com.emagine.ussd.utils.USSDConstants.FIFTEEN;
import static com.emagine.ussd.utils.USSDConstants.FIVE;
import static com.emagine.ussd.utils.USSDConstants.FOUR;
import static com.emagine.ussd.utils.USSDConstants.FOURTEEN;
import static com.emagine.ussd.utils.USSDConstants.NINE;
import static com.emagine.ussd.utils.USSDConstants.NINETEEN;
import static com.emagine.ussd.utils.USSDConstants.ONE;
import static com.emagine.ussd.utils.USSDConstants.SEVEN;
import static com.emagine.ussd.utils.USSDConstants.SEVENTEEN;
import static com.emagine.ussd.utils.USSDConstants.SIX;
import static com.emagine.ussd.utils.USSDConstants.SIXTEEN;
import static com.emagine.ussd.utils.USSDConstants.TEN;
import static com.emagine.ussd.utils.USSDConstants.THIRTEEN;
import static com.emagine.ussd.utils.USSDConstants.THREE;
import static com.emagine.ussd.utils.USSDConstants.TWELVE;
import static com.emagine.ussd.utils.USSDConstants.TWO;
import static com.emagine.ussd.utils.USSDConstants.ZERO;
import java.util.ArrayList;
import java.util.List;

public class TransmitMessage extends AbstractTextMessage {

    private Long messageKey;
    private int sourceTON;
    private int sourceNPI;
    private String sourceAddress;
    private int destinationTON;
    private int destinationNPI;
    private String destinationAddress;
    private int messageEncoding;
    private String messageText;
    private boolean registered;
    private int retryCount;
    private int partsReference;
    private int partNumber = 1;
    private int partsTotal = 1;
    private String validityPeriod;
    private Long expiryTime;
    private int dataCoding;
    private int serviceOp;
    private int referenceNumber;
    private String transactionId;
    private int ussdLogStatus = 1;
    private String logStatus;
    private boolean mlFlag;
    private boolean randomFlag;
    private String prodIds;
    private String selProdId;
    private String cellId;
    private String poolId;
    private List<String> productIdList;
    private long  lstarttime ;
    private long  lTimeTaken ;
    private String townName;

    public long getlTimeTaken() {
		return lTimeTaken;
	}

	public void setlTimeTaken(long lTimeTaken) {
		this.lTimeTaken = lTimeTaken;
	}

	public TransmitMessage() {
		lstarttime = System.currentTimeMillis() ;
		lTimeTaken = 0 ;
    }

    public TransmitMessage(final String serialized) throws UssdMessageException {
        deserializeFromString(serialized);
    }

    public TransmitMessage(TransmitMessage message) {
        this.messageKey = message.messageKey;
        this.sourceTON = message.sourceTON;
        this.sourceNPI = message.sourceNPI;
        this.sourceAddress = message.sourceAddress;
        this.destinationTON = message.destinationTON;
        this.destinationNPI = message.destinationNPI;
        this.destinationAddress = message.destinationAddress;
        this.messageEncoding = message.messageEncoding;
        this.messageText = message.messageText;
        this.registered = message.registered;
        this.retryCount = message.retryCount;
        this.partsReference = message.partsReference;
        this.partNumber = message.partNumber;
        this.partsTotal = message.partsTotal;
        this.validityPeriod = message.validityPeriod;
        this.expiryTime = message.expiryTime;
        this.dataCoding = message.dataCoding;
        this.serviceOp = message.serviceOp;
        this.referenceNumber = message.referenceNumber;
        this.transactionId = message.transactionId;
        this.ussdLogStatus = message.ussdLogStatus;
        this.mlFlag = message.mlFlag;
        this.randomFlag = message.randomFlag;
        this.prodIds = message.prodIds;
        this.selProdId = message.selProdId;
        this.logStatus = message.logStatus;
        this.cellId = message.cellId;
        this.poolId = message.poolId;
        this.lstarttime = message.lstarttime;
        this.lTimeTaken = message.lTimeTaken;
        this.townName = message.townName;
    }

    public long getLstarttime() {
		return lstarttime;
	}

	public void setLstarttime(long lstarttime) {
		this.lstarttime = lstarttime;
	}

	public List<String> getProductIdList() {
        return productIdList ;
    }

    public void setProductIdList(List<String> productIdList) {
        this.productIdList = new ArrayList<>(productIdList);
    }

    @Override
    public String serializeToString() {
        final StringBuffer s = new StringBuffer();
        s.append(messageKey).append('\t');
        s.append(sourceTON).append('\t');
        s.append(sourceNPI).append('\t');
        s.append(encodeString(sourceAddress)).append('\t');
        s.append(destinationTON).append('\t');
        s.append(destinationNPI).append('\t');
        s.append(encodeString(destinationAddress)).append('\t');
        s.append(registered).append('\t');
        s.append(retryCount).append('\t');
        s.append(partsReference).append('\t');
        s.append(partsTotal).append('\t');
        s.append(partNumber).append('\t');
        s.append(encodeString(validityPeriod)).append('\t');
        s.append(expiryTime).append('\t');
        s.append(dataCoding).append('\t');
        s.append(serviceOp).append('\t');
        s.append(referenceNumber).append('\t');
        s.append(messageEncoding).append('\t');
        s.append(encodeString(messageText)).append('\t');
        s.append(ussdLogStatus).append('\t');
        s.append(null != transactionId ? transactionId : "");
        return s.toString();
    }

    public void deserializeFromString(String serialized) throws UssdMessageException {
        final String[] fields = ((serialized == null) ? (new String[1]) : split(serialized, "\t"));

        if (fields.length < NINETEEN) {
            throw new UssdMessageException("Could not deserialize the " + TransmitMessage.class.getSimpleName() + " message - incorrect number of fields. The serialized message is: " + serialized);
        }

        try {
            messageKey = ("null".equals(fields[ZERO]) ? null : Long.parseLong(fields[ZERO]));
            sourceTON = Integer.parseInt(fields[ONE]);
            sourceNPI = Integer.parseInt(fields[TWO]);
            sourceAddress = ("null".equals(fields[THREE]) ? null : decodeString(fields[THREE]));
            destinationTON = Integer.parseInt(fields[FOUR]);
            destinationNPI = Integer.parseInt(fields[FIVE]);
            destinationAddress = ("null".equals(fields[SIX]) ? null : decodeString(fields[SIX]));
            registered = Boolean.parseBoolean(fields[SEVEN]);
            retryCount = Integer.parseInt(fields[EIGHT]);
            partsReference = Integer.parseInt(fields[NINE]);
            partsTotal = Integer.parseInt(fields[TEN]);
            partNumber = Integer.parseInt(fields[ELEVEN]);
            validityPeriod = ("null".equals(fields[TWELVE]) ? null : decodeString(fields[TWELVE]));
            expiryTime = ("null".equals(fields[THIRTEEN]) ? null : Long.parseLong(fields[THIRTEEN]));
            dataCoding = Integer.parseInt(fields[FOURTEEN]);
            serviceOp = Integer.parseInt(fields[FIFTEEN]);
            referenceNumber = Integer.parseInt(fields[SIXTEEN]);
            messageEncoding = Integer.parseInt(fields[SEVENTEEN]);
            messageText = ("null".equals(fields[EIGHTEEN]) ? null : decodeString(fields[EIGHTEEN]));
        } catch (Throwable e) {
            throw new UssdMessageException("Could not deserialize the " + TransmitMessage.class.getSimpleName(), e);
        }
    }

    public Long getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(Long messageKey) {
        this.messageKey = messageKey;
    }

    public int getSourceTON() {
        return sourceTON;
    }

    public void setSourceTON(int sourceTON) {
        this.sourceTON = sourceTON;
    }

    public int getSourceNPI() {
        return sourceNPI;
    }

    public void setSourceNPI(int sourceNPI) {
        this.sourceNPI = sourceNPI;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public int getDestinationTON() {
        return destinationTON;
    }

    public void setDestinationTON(int destinationTON) {
        this.destinationTON = destinationTON;
    }

    public int getDestinationNPI() {
        return destinationNPI;
    }

    public void setDestinationNPI(int destinationNPI) {
        this.destinationNPI = destinationNPI;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public int getMessageEncoding() {
        return messageEncoding;
    }

    public void setMessageEncoding(int messageEncoding) {
        this.messageEncoding = messageEncoding;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void incrementRetryCount() {
        retryCount++;
    }

    public int getPartsReference() {
        return partsReference;
    }

    public void setPartsReference(int partsReference) {
        this.partsReference = partsReference;
    }

    public int getPartsTotal() {
        return partsTotal;
    }

    public void setPartsTotal(int partsTotal) {
        this.partsTotal = partsTotal;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public String getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(String validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public Long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public int getDataCoding() {
        return dataCoding;
    }

    public void setDataCoding(int dataCoding) {
        this.dataCoding = dataCoding;
    }

    public int getServiceOp() {
        return serviceOp;
    }

    public void setServiceOp(int serviceOp) {
        this.serviceOp = serviceOp;
    }

    public int getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(int referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getUssdLogStatus() {
        return ussdLogStatus;
    }

    public void setUssdLogStatus(int ussdLogStatus) {
        this.ussdLogStatus = ussdLogStatus;
    }

    public String getLogStatus() {
        return logStatus;
    }

    public void setLogStatus(String logStatus) {
        this.logStatus = logStatus;
    }

    public boolean getMlFlag() {
        return mlFlag;
    }

    public void setMlFlag(boolean mlFlag) {
        this.mlFlag = mlFlag;
    }

    public boolean getRandomFlag() {
        return randomFlag;
    }

    public void setRandomFlag(boolean randomFlag) {
        this.randomFlag = randomFlag;
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