package com.emagine.ussd.model;

import java.util.Date;

/**
 * J4UNewCustomerProfile class
 */
public class J4UNewCustomerProfile {

	private String msisdn;
	private String langCategory;
	private int langCode;
	private String subscriberState;
	private String paymentMethod;
	private String offerPaymentMethod;
	private String subscriberStartDate;
	private Date date;

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getLangCategory() {
		return langCategory;
	}

	public void setLangCategory(String langCategory) {
		this.langCategory = langCategory;
	}

	public int getLangCode() {
		return langCode;
	}

	public void setLangCode(int langCode) {
		this.langCode = langCode;
	}

	public String getSubscriberState() {
		return subscriberState;
	}

	public void setSubscriberState(String subscriberState) {
		this.subscriberState = subscriberState;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getOfferPaymentMethod() {
		return offerPaymentMethod;
	}

	public void setOfferPaymentMethod(String offerPaymentMethod) {
		this.offerPaymentMethod = offerPaymentMethod;
	}

	public String getSubscriberStartDate() {
		return subscriberStartDate;
	}

	public void setSubscriberStartDate(String subscriberStartDate) {
		this.subscriberStartDate = subscriberStartDate;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "J4UNewCustomerProfile [msisdn=" + msisdn + ", langCategory=" + langCategory + ", langCode=" + langCode
				+ ", subscriberState=" + subscriberState + ", paymentMethod=" + paymentMethod + ", offerPaymentMethod="
				+ offerPaymentMethod + ", subscriberStartDate=" + subscriberStartDate + ", date=" + date + "]";
	}

}
