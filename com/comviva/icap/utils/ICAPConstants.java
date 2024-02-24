package com.comviva.icap.utils;

/**
 * The ICAPConstants class provides constant values used in the ICAP service integration.
 *
 * @author Anket Pratap Singh
 */
public final class ICAPConstants {
	
	// Private constructor to prevent instantiation
	private ICAPConstants() {
	}
	
	public static final String ICAP_MSISDN = "MSISDN";
	
	public static final String USER_ID = "userId";
	public static final String SECURITY_KEY = "securityKey";
	
	public static final String ICAP_LANGUAGE_CATEGORY = "Language Category";
	public static final String ICAP_SUBSCRIBER_STATE = "Subscriber State";
	public static final String ICAP_PAYMENT_METHOD = "Payment Method";
	public static final String ICAP_OFFER_PAYMENT_METHOD = "Offer Payment Method";
	public static final String ICAP_SUBSCRIBER_START_DATE = "Subscriber Start Date";
	
	public static final String ICAP_STATUS = "status";
	public static final String ERROR_CODE = "error-code";
	public static final String ICAP_INVALID_SESSION_ERROR_CODE = "icap.invalid.session.error.code";
	public static final String ERROR_MESSAGE = "error-message";
	public static final String QP_ERROR_MESSAGE = "Login API failed.";
	public static final String QP_ERROR_CODE = "-1";
	public static final String ATTRIBUTE = "attribute";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	public static final String SUCCESS = "SUCCESS";
	public static final String HTTPS = "HTTPS";
	public static final String GET = "GET";
	public static final String FAILURE = "FAILURE";
	public static final String R_USER_ID = "@userid@";
	public static final String R_SECURITY_KEY = "@securitykey@";
	public static final String R_MSISDN = "@MSISDN@";
	public static final String ZERO = "0";
	public static final String ICAP_CONN_FAILED_CD = "icap.connection.failed.code";
    public static final String ICAP_CONN_FAILED_MSG = "icap.connection.failed.msg";
	public static final String SSL_CERTIFICATE_CHECK_ENABLED = "SSL_CERTIFICATE_CHECK_ENABLED";
	public static final String LOGIN_URL = "icap.login.url";
	public static final String QUERY_SUBSCRIBER_PROFILE_URL = "icap.query.subscriber.profile.url";
	public static final String ICAP_LOG_DISABLE_FLAG = "icap.log.disable.flag";

	
	// Timeout related	
	public static final String READ_TIMEOUT_MILLIS = "icap.read.timeoutMillis";
	public static final String CONN_TIMEOUT_MILLIS = "icap.connection.timeoutMillis";
	
	
}
