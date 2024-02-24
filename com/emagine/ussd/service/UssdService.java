package com.emagine.ussd.service;

import static com.emagine.ussd.utils.USSDConstants.ACCOUNT_BALANCE;
import static com.emagine.ussd.utils.USSDConstants.AIRTIME_ADVANCE_BALANCE;
import static com.emagine.ussd.utils.USSDConstants.CELL_ID_KEY_NAME;
import static com.emagine.ussd.utils.USSDConstants.DEST_ADDRESS;
import static com.emagine.ussd.utils.USSDConstants.FLAG_N;
import static com.emagine.ussd.utils.USSDConstants.FLAG_Y;
import static com.emagine.ussd.utils.USSDConstants.J4U_MORNING_OFFER;
import static com.emagine.ussd.utils.USSDConstants.MAIN_MENU;
import static com.emagine.ussd.utils.USSDConstants.MPESA_BALANCE_RECEIVED;
import static com.emagine.ussd.utils.USSDConstants.MSISDN;
import static com.emagine.ussd.utils.USSDConstants.OCS_BALANCE_RECEIVED;
import static com.emagine.ussd.utils.USSDConstants.POOL_ID;
import static com.emagine.ussd.utils.USSDConstants.PREF_PAY_METHOD;
import static com.emagine.ussd.utils.USSDConstants.PREF_PAY_MET_MPESA;
import static com.emagine.ussd.utils.USSDConstants.PRODUCT_TYPE;
import static com.emagine.ussd.utils.USSDConstants.REFRESH_RF_VALUE;
import static com.emagine.ussd.utils.USSDConstants.TRANSACTION_ID;
import static com.emagine.ussd.utils.USSDConstants.USER_MSG_REF;
import static com.emagine.ussd.utils.USSDConstants.USER_SELECTION;
import static com.emagine.ussd.utils.USSDConstants.VALUE_N;
import static com.emagine.ussd.utils.USSDConstants.J4U_TOWN;
import static com.emagine.ussd.utils.Utils.getCurrentTimeStamp;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import com.emagine.ussd.config.PropertiesLoader;
import com.emagine.ussd.connection.AAMap;
import com.emagine.ussd.connection.TransmitMessage;
import com.emagine.ussd.model.UserInfo;
import com.emagine.ussd.utils.QueryBaljsonMap;
import com.emagine.ussd.utils.UserInfoMapCache;

/**
 * UssdService processes USSD queries, interacts with the USSD gateway, and logs events into the database.
 * It handles main menu, morning offer submenu, town offer submenu, random or cache submenu,
 * and target customer submenu requests.
 *
 * @author Anket Pratap Singh
 */
public class UssdService implements IUssdServcie {

	private static final Logger LOGGER = Logger.getLogger(UssdService.class);

	// InboundMessageService for processing inbound messages
	private InboundMessageService inboundMessageService;

	/**
     * Constructs a new UssdService instance and initializes the InboundMessageService.
     */
	public UssdService() {
		inboundMessageService = new InboundMessageService();
	}

	/**
     * Processes the incoming USSD query request and delegates it to specific methods based on the
     * product type and other conditions.
     *
     * @param incomingReq The incoming USSD query request in String format.
     * @throws Exception If an error occurs during processing.
     */
	@Override
	public void processQueryBalanceRequest(String incomingReq) throws Exception {
		LOGGER.debug("processQueryBalanceRequest received");
		JSONObject queryBaljson = new JSONObject(incomingReq);

		if (queryBaljson.getString(PRODUCT_TYPE).equalsIgnoreCase(MAIN_MENU)) {
			processMainMenuRequest(queryBaljson);
		} else if (queryBaljson.getString(PRODUCT_TYPE).equalsIgnoreCase(PropertiesLoader.getValue(J4U_MORNING_OFFER))) {
			processMorningOfferSubMenuRequest(queryBaljson);
		} else if (queryBaljson.getString(PRODUCT_TYPE).equalsIgnoreCase(PropertiesLoader.getValue(J4U_TOWN))) {
			processTownOfferSubMenuRequest(queryBaljson);
		} else if (queryBaljson.getInt(REFRESH_RF_VALUE) == 0) {
			processRandomOrCacheSubMenuRequest(queryBaljson);
		} else {
			processTargetCustomerSubMenuRequest(queryBaljson);
		}
	}

	/**
     * Processes the main menu request.
     *
     * @param queryBaljson The USSD query JSON object.
     * @throws Exception If an error occurs during processing.
     */
	private void processMainMenuRequest(JSONObject queryBaljson) throws Exception {
		TransmitMessage message = null;
		UserInfoMapCache userInfoMapCache = UserInfoMapCache.instance();
		QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
		UserInfo userInfo = userInfoMapCache.get(queryBaljson.getInt(USER_MSG_REF));
		String mlFlag = userInfo.isMlFlag() ? FLAG_Y : FLAG_N;
		String randomFlag = userInfo.isRandomFlag() ? FLAG_Y : FLAG_N;
		logEventToDB(queryBaljson, OCS_BALANCE_RECEIVED, mlFlag, randomFlag, userInfo.getTownName());

		if (userInfo.isJ4uNewUser()) {
			message = inboundMessageService.processMenuForJ4UnewCustomer(queryBaljson, userInfo);
		} else if (userInfo.isMlFlag()) {
			message = inboundMessageService.getMainMenuForMLUser(queryBaljson, userInfo);
		}

		if (message != null) {
			IUssdConnectionService ussdConnectionService = UssdConnectionService.getInstance();
			LOGGER.debug("J4U Main menu calling USSD gateway");
			ussdConnectionService.sendMesage(message);
		}

		userInfoMapCache.remove(queryBaljson.getInt(USER_MSG_REF));
		queryBaljsonMap.put(queryBaljson.getInt(USER_MSG_REF), queryBaljson);
	}

	/**
     * Processes the morning offer submenu request.
     *
     * @param queryBaljson The USSD query JSON object.
     * @throws Exception If an error occurs during processing.
     */
	private void processMorningOfferSubMenuRequest(JSONObject queryBaljson) throws Exception {
		UserInfoMapCache userInfoMapCache = UserInfoMapCache.instance();
		UserInfo userInfo = userInfoMapCache.get(queryBaljson.getInt(USER_MSG_REF));
		String mlFlag = userInfo.isMlFlag() ? FLAG_Y : FLAG_N;
		String randomFlag = userInfo.isRandomFlag() ? FLAG_Y : FLAG_N;
		logEventToDB(queryBaljson, OCS_BALANCE_RECEIVED, mlFlag, randomFlag, userInfo.getTownName());
		TransmitMessage message = inboundMessageService.subMenuMLMorningRequest(queryBaljson,
				queryBaljson.getInt(USER_MSG_REF), queryBaljson.getString(USER_SELECTION), userInfo);

		if (message != null) {
			IUssdConnectionService ussdConnectionService = UssdConnectionService.getInstance();
			LOGGER.debug("J4U Morning Offer Sub Menu calling USSD gateway");
			ussdConnectionService.sendMesage(message);
		}
		userInfoMapCache.remove(queryBaljson.getInt(USER_MSG_REF));
	}

	/**
     * Processes the town offer submenu request.
     *
     * @param queryBaljson The USSD query JSON object.
     * @throws Exception If an error occurs during processing.
     */
	private void processTownOfferSubMenuRequest(JSONObject queryBaljson) throws Exception {
		AAMap aaMap = AAMap.instance();
		UserInfo aaMapInfo = aaMap.get(queryBaljson.getInt(USER_MSG_REF));
		String mlFlag = aaMapInfo.isMlFlag() ? FLAG_Y : FLAG_N;
		String randomFlag = aaMapInfo.isRandomFlag() ? FLAG_Y : FLAG_N;
		logEventToDB(queryBaljson, OCS_BALANCE_RECEIVED, mlFlag, randomFlag, aaMapInfo.getTownName());
		TransmitMessage message = inboundMessageService.subMenuMLTownRequest(queryBaljson,
				queryBaljson.getInt(USER_MSG_REF), queryBaljson.getString(USER_SELECTION), aaMapInfo);

		if (message != null) {
			IUssdConnectionService ussdConnectionService = UssdConnectionService.getInstance();
			LOGGER.debug("J4U Town Offer Sub Menu calling USSD gateway");
			ussdConnectionService.sendMesage(message);
		}
		processAaEligibleUser(aaMapInfo, aaMap, queryBaljson);
	}

	/**
     * Processes the random or cache submenu request.
     *
     * @param queryBaljson The USSD query JSON object.
     * @throws Exception If an error occurs during processing.
     */
	private void processRandomOrCacheSubMenuRequest(JSONObject queryBaljson) throws Exception {
		TransmitMessage message = null;
		QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
		AAMap aaMap = AAMap.instance();
		UserInfo aaMapInfo = aaMap.get(queryBaljson.getInt(USER_MSG_REF));
		String mlFlag = aaMapInfo.isMlFlag() ? FLAG_Y : FLAG_N;
		String randomFlag = aaMapInfo.isRandomFlag() ? FLAG_Y : FLAG_N;
		logEventToDB(queryBaljson, OCS_BALANCE_RECEIVED, mlFlag, randomFlag, aaMapInfo.getTownName());

		queryBaljsonMap.put(queryBaljson.getInt(USER_MSG_REF), queryBaljson);
		if (aaMapInfo.isLocationRandomFlag() || aaMapInfo.isRandomFlag()) {
			message = inboundMessageService.subMenuMLRequest(aaMapInfo.getUserMsgRef(), aaMapInfo.getMessageBody(), aaMapInfo, true);
		} else if (aaMapInfo.getOfferRefreshFlag().equalsIgnoreCase(VALUE_N) || aaMapInfo.getOfferRefreshFlag().indexOf(aaMapInfo.getSelProdType()) == -1) {
			LOGGER.debug("OFFER_REFRESH_FLAG = N OR index of OFFER_REFRESH_FLAG = -1 --> Condition is true  receiving  Cache offers =>");
			message = inboundMessageService.subMenuMLRequest(aaMapInfo.getUserMsgRef(), aaMapInfo.getMessageBody(), aaMapInfo, false);
		}
		if (message != null) {
			IUssdConnectionService ussdConnectionService = UssdConnectionService.getInstance();
			LOGGER.debug("J4U Sub menu calling USSD gateway");
			ussdConnectionService.sendMesage(message);
		}
		processAaEligibleUser(aaMapInfo, aaMap, queryBaljson); 
	}

	/**
     * Processes the target customer submenu request.
     *
     * @param queryBaljson The USSD query JSON object.
     * @throws JSONException If an error occurs while handling JSON.
     * @throws Exception     If an error occurs during processing.
     */
	private void processTargetCustomerSubMenuRequest(JSONObject queryBaljson) throws JSONException, Exception {
		QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
		if (queryBaljson.getString(PREF_PAY_METHOD).equalsIgnoreCase(PREF_PAY_MET_MPESA)) {
			logEventToDB(queryBaljson, MPESA_BALANCE_RECEIVED, FLAG_Y, VALUE_N, null);
		} else {
			logEventToDB(queryBaljson, OCS_BALANCE_RECEIVED, FLAG_Y, VALUE_N, null);
			queryBaljsonMap.put(queryBaljson.getInt(USER_MSG_REF), queryBaljson);
		}
		UserInfo userInfo = inboundMessageService.getSubMenuForTgtMLUser(queryBaljson);
		IUssdConnectionService ussdConnectionService = UssdConnectionService.getInstance();
		LOGGER.debug("User Info calling USSD gateway to send => " + userInfo);
		ussdConnectionService.sendMesage(userInfo);
	}

	/**
     * Processes AA (Airtime Advance) eligible users and updates the AAMap with relevant information.
     *
     * @param aaUserInfo The UserInfo object for the AA eligible user.
     * @param aaMap      The AAMap storing information about AA eligible users.
     * @param queryBaljson The USSD query JSON object.
     */
	private void processAaEligibleUser(UserInfo aaUserInfo, AAMap aaMap, JSONObject queryBaljson) {
        if (aaUserInfo.getAaEligible() == 1) {
            aaUserInfo.setAirtimeAdvBal(queryBaljson.getLong(AIRTIME_ADVANCE_BALANCE));
            aaUserInfo.setActBal(queryBaljson.getLong(ACCOUNT_BALANCE));
            LOGGER.debug("queryBaljson.getInt(USER_MSG_REF) cache flow - " + queryBaljson.getInt(USER_MSG_REF));
            LOGGER.debug("aaMapInfo.getAirtimeAdvBal cache flow - " + aaUserInfo.getAirtimeAdvBal());
            LOGGER.debug("aaMapInfo.getActBal cache flow - " + aaUserInfo.getActBal());
            aaMap.put(queryBaljson.getInt(USER_MSG_REF), aaUserInfo);
        }
	}

	/**
     * Logs an event to the database with relevant details.
     *
     * @param queryBaljson The USSD query JSON object.
     * @param status       The status of the event (e.g., OCS_BALANCE_RECEIVED).
     * @param mlFlag       ML flag indicating whether the user is an ML user.
     * @param randomFlag   Random flag indicating random behavior.
     * @param townName     The town name associated with the user.
     * @throws Exception If an error occurs during logging.
     */
	private void logEventToDB(JSONObject queryBaljson, String status, String mlFlag, String randomFlag, String townName)
			throws Exception {
		// log the OCS info in "ECMP_T_USSD_LOG" Oracle table
		inboundMessageService.logEventIntoDB(queryBaljson.getString(TRANSACTION_ID), queryBaljson.getInt(USER_MSG_REF),
				queryBaljson.getString(MSISDN), null, null, status, getCurrentTimeStamp(),
				queryBaljson.getString(DEST_ADDRESS), queryBaljson.getString(USER_SELECTION),
				String.valueOf(queryBaljson.getLong(ACCOUNT_BALANCE)), mlFlag, randomFlag,
				queryBaljson.getString(CELL_ID_KEY_NAME), queryBaljson.getString(POOL_ID), townName);
	}
}