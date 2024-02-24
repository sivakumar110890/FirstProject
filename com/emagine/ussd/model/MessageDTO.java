/**
 *
 */
package com.emagine.ussd.model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * MessageDTO class
 */
public class MessageDTO {

    private int active;
    private Timestamp dateTime;
    private int langCode;
    private String messageText;
    private String msisdn;
    private Timestamp nbaDateTime;
    private String[] offerSequence;
    private String productIds;
    private Map<String, String> products;
    private String selectedProdcutId;
    private String status;
    private String template;
    private String templateId;
    private String txnId;
    private int ussdMsgId;
    private String ussdShortCode;
    private String cellId;
    private String poolId;
    private String townName;

    /**
     * @param key
     *            the offer key to set
     * @param value
     *            the product id to set
     */
    public void addProduct(String key, String value) {
        getProducts().put(key, value);
    }

    /**
     * @param products
     *            the products to set
     */
    public void addProducts(final Map<String, String> prods) {
        getProducts().putAll(prods);
    }

    /**
     * @return the active
     */
    public int getActive() {
        return active;
    }

    /**
     * @param active
     *            the active to set
     */
    public void setActive(int active) {
        this.active = active;
    }

    /**
     * @return the dateTime
     */
    public Timestamp getDateTime() {
        return dateTime;
    }

    /**
     * @param dateTime
     *            the dateTime to set
     */
    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * @return the langCode
     */
    public int getLangCode() {
        return langCode;
    }

    /**
     * @param langCode
     *            the langCode to set
     */
    public void setLangCode(int langCode) {
        this.langCode = langCode;
    }

    /**
     * @return the messageText
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * @param messageText
     *            the messageText to set
     */
    public void setMessageText(String messageText) {
        this.messageText = messageText;
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

    /**
     * @return the nbaDateTime
     */
    public Timestamp getNbaDateTime() {
        return nbaDateTime;
    }

    /**
     * @param nbaDateTime
     *            the nbaDateTime to set
     */
    public void setNbaDateTime(Timestamp nbaDateTime) {
        this.nbaDateTime = nbaDateTime;
    }

    /**
     * @return the offerSequence
     */
    public String[] getOfferSequence() {
        return offerSequence.clone();
    }

    /**
     * @param offerSequence
     *            the offerSequence to set
     */
    public void setOfferSequence(String[] offerSequence) {
        this.offerSequence = offerSequence.clone();
    }

    /**
     * @return the productIds
     */
    public String getProductIds() {
        return productIds;
    }

    /**
     * @param productIds
     *            the productIds to set
     */
    public void setProductIds(String productIds) {
        this.productIds = productIds;
    }

    /**
     * @return the products
     */
    public Map<String, String> getProducts() {
        if (null == products) {
            products = new HashMap<>();
        }
        return products;
    }

    /**
     * @return the selectedProdcutId
     */
    public String getSelectedProdcutId() {
        return selectedProdcutId;
    }

    /**
     * @param selectedProdcutId
     *            the selectedProdcutId to set
     */
    public void setSelectedProdcutId(String selectedProdcutId) {
        this.selectedProdcutId = selectedProdcutId;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * @param template
     *            the template to set
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * @return the templateId
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * @param templateId
     *            the templateId to set
     */
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    /**
     * @return the txnId
     */
    public String getTxnId() {
        return txnId;
    }

    /**
     * @param txnId
     *            the txnId to set
     */
    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    /**
     * @return the ussdMsgId
     */
    public int getUssdMsgId() {
        return ussdMsgId;
    }

    /**
     * @param ussdMsgId
     *            the ussdMsgId to set
     */
    public void setUssdMsgId(int ussdMsgId) {
        this.ussdMsgId = ussdMsgId;
    }

    /**
     * @return the ussdShortCode
     */
    public String getUssdShortCode() {
        return ussdShortCode;
    }

    /**
     * @param ussdShortCode
     *            the ussdShortCode to set
     */
    public void setUssdShortCode(String ussdShortCode) {
        this.ussdShortCode = ussdShortCode;
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
