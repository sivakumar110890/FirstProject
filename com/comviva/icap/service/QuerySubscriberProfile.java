package com.comviva.icap.service;

import static com.comviva.icap.utils.ICAPConstants.ERROR_CODE;
import static com.comviva.icap.utils.ICAPConstants.ERROR_MESSAGE;
import static com.comviva.icap.utils.ICAPConstants.FAILURE;
import static com.comviva.icap.utils.ICAPConstants.ICAP_INVALID_SESSION_ERROR_CODE;
import static com.comviva.icap.utils.ICAPConstants.ICAP_MSISDN;
import static com.comviva.icap.utils.ICAPConstants.ICAP_STATUS;
import static com.comviva.icap.utils.ICAPConstants.LOGIN_URL;
import static com.comviva.icap.utils.ICAPConstants.QP_ERROR_CODE;
import static com.comviva.icap.utils.ICAPConstants.QP_ERROR_MESSAGE;
import static com.comviva.icap.utils.ICAPConstants.QUERY_SUBSCRIBER_PROFILE_URL;
import static com.comviva.icap.utils.ICAPConstants.R_MSISDN;
import static com.comviva.icap.utils.ICAPConstants.R_SECURITY_KEY;
import static com.comviva.icap.utils.ICAPConstants.R_USER_ID;
import static com.comviva.icap.utils.ICAPConstants.SECURITY_KEY;
import static com.comviva.icap.utils.ICAPConstants.SUCCESS;
import static com.comviva.icap.utils.ICAPConstants.USER_ID;
import static com.comviva.icap.utils.ICAPConstants.ZERO;
import static com.emagine.ussd.utils.USSDConstants.SUBID_LENGTH;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import com.emagine.ussd.config.PropertiesLoader;
import com.emagine.ussd.utils.Utils;

/**
 * The QuerySubscriberProfile class provides functionality for checking the profile of a subscriber
 * using the ICAP service.
 * <p>
 * This class encapsulates the logic for performing login, querying subscriber profiles,
 * and handling invalid sessions. It relies on the HTTPSclient for making HTTP requests.
 * </p>
 *
 * @author Anket Pratap Singh
 */
public class QuerySubscriberProfile {
	private static final Logger LOGGER = Logger.getLogger(QuerySubscriberProfile.class);

	private static String userId = "";
	private static String securityKey = "";
	private static String icapStatus = "";

	private static String loginEndPoint;
	private HTTPSclient httpSclient;

	// Use a lock for synchronization
	private final Object lock = new Object();

	static {
		try {
			loginEndPoint = PropertiesLoader.getValue(LOGIN_URL);
		} catch (Exception e) {
			LOGGER.error("Error in loading ICAP login EndPoint :: ", e);
		}
	}

	/**
     * Constructs a new instance of the QuerySubscriberProfile class.
     * It initializes the HTTP client for making requests.
     */
	public QuerySubscriberProfile() {
		httpSclient = new HTTPSclient();
	}

	/**
     * Checks the profile of a subscriber identified by the provided MSISDN.
     *
     * @param msisdn The MSISDN of the subscriber.
     * @return A JSONObject containing the response from the iCap service.
     * @throws Exception If an error occurs during the profile check.
     */
	public JSONObject checkCustomerProfile(String msisdn) throws Exception {
		JSONObject response = null;
		LOGGER.info("Checking Profile for msisdn -> " + msisdn);
		// Perform login if necessary
		if (securityKey.isEmpty() || userId.isEmpty() || icapStatus.equalsIgnoreCase(FAILURE)) {
			performLogin();
		}
		// Query subscriber profile
		response = querySubscriberProfile(msisdn);

		// Handle invalid session and retry login
		if (!response.getString(ICAP_STATUS).equalsIgnoreCase(SUCCESS)
				&& response.getString(ERROR_CODE).equals(PropertiesLoader.getValue(ICAP_INVALID_SESSION_ERROR_CODE))) {
			response.put(ICAP_MSISDN, msisdn);
			LOGGER.debug("Query Subscriber Profile Response :: " + response);
			LOGGER.info("Invalid session, trying to log on again for msisdn -> " + msisdn);
			performLogin();
			response = querySubscriberProfile(msisdn);
		}
		response.put(ICAP_MSISDN, msisdn);
		LOGGER.debug("Query Subscriber Profile Response :: " + response);
		return response;
	}
	
	/**
     * Queries the subscriber profile using the ICAP service.
     *
     * @param msisdn The MSISDN of the subscriber.
     * @return A JSONObject containing the response from the ICAP service.
     * @throws Exception If an error occurs during the query.
     */
	private JSONObject querySubscriberProfile(String msisdn) throws Exception {
	    JSONObject response = new JSONObject();

	    if (icapStatus.equalsIgnoreCase(SUCCESS)) {
	        // Query Subscriber Profile API
	        response = httpSclient.sendRequest(getQuerySubscriberProfileUrl(msisdn));
	    } else {
	        response.put(ICAP_STATUS, FAILURE);
	        response.put(ERROR_CODE, QP_ERROR_CODE);
	        response.put(ERROR_MESSAGE, QP_ERROR_MESSAGE);
	    }

	    return response;
	}

	/**
	 * Performs the login to the ICAP service and retrieves the necessary credentials.
	 *
	 * @throws Exception If an error occurs during the login process.
	 */
	private void performLogin() throws Exception {
	    synchronized (lock) {
	        JSONObject loginResponse = httpSclient.sendRequest(loginEndPoint);
	        icapStatus = loginResponse.getString(ICAP_STATUS);
	        if (icapStatus.equalsIgnoreCase(SUCCESS)) {
	            userId = loginResponse.getString(USER_ID);
	            securityKey = loginResponse.getString(SECURITY_KEY);
	        } else {
	            userId = "";
	            securityKey = "";
	            LOGGER.error("ERROR :: " + loginResponse);
	        }
	    }
	}


	/**
     * Constructs the URL for querying the subscriber profile.
     *
     * @param msisdn The MSISDN of the subscriber.
     * @return The constructed URL for querying the subscriber profile.
     * @throws Exception If an error occurs during the construction of the URL.
     */
	private static String getQuerySubscriberProfileUrl(String msisdn) throws Exception {
		String endpoint = PropertiesLoader.getValue(QUERY_SUBSCRIBER_PROFILE_URL);
		if (msisdn.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
            msisdn = Utils.getMsisdnWithoutCcode(msisdn);
        }
		// Replace placeholders with actual values
		endpoint = endpoint.replace(R_USER_ID, userId);
		endpoint = endpoint.replace(R_SECURITY_KEY, securityKey);
		endpoint = endpoint.replace(R_MSISDN, ZERO + msisdn);
		return endpoint;
	}
}
