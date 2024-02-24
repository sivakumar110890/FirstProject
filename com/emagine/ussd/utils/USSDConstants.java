package com.emagine.ussd.utils;

public class USSDConstants {

	private USSDConstants() {
	}

	public static final String USER_DIR = System.getProperty("user.dir");

	public static final String USSD_CONNECTION_PROPERTIES_FILE = USER_DIR + "/config/ussdconnection.properties";

	public static final String DB_PROPERTIES_FILENAME = USER_DIR + "/config/ussddbconfig.properties";

	public static final boolean IS_ASYNC = true;
	public static final int CONNECTION_TIMEOUT = 10;

	public static final String USSD_GATEWAY_ENABLED = "ussd.connection.gateway.enabled";
	public static final String BACK_PRESSURE_SLP_TIME = "back.pressure.sleeping.time";
	public static final String USSD_LOG_PROC_NAME = "ussd.log.proc.name";
	public static final String USSD_TRX_PRODID_MAP_NAME = "ussd.trxprodidmap.proc.name";
	public static final String REWARDSPUB_TOPIC_NAME = "ussd.rewardspublisher.topic.name";
	public static final String MPESA_TOPIC_NAME = "ussd.mpesapublisher.topic.name";
	public static final String USSD_TRXPRODIDMAP_DELETE_NAME = "ussd.trxprodidmap.deleteproc.name";
	public static final String USSD_OFFERLOOKUP_PROC_NAME = "ussd.offerLookup.proc.name";
	public static final String USSD_TEMPLATEFORID_PROC_NAME = "ussd.templateForId.proc.name";
	public static final String USSD_PRODINFO_PROC_NAME = "ussd.prodInfo.proc.name";
	public static final String USSD_PRODINFO_PRODTYPE_NAME = "ussd.prodInfo.prodType.name";
	public static final String USSD_TXNPRODMAP_PROC_NAME = "ussd.txnProdMap.proc.name";
	public static final String AUDIT_TIMEZONE = "enba.audit.timzone";
	public static final String NEW_USER_DEFAULT_TEMPLATE = "ussd.newuser.default.templateId";
	public static final String DEFAULT_MSG_PROC_NAME = "ussd.defaultMessage.proc.name";
	public static final String DEFAULT_ERRMSG_PROC_NAME = "ussd.defaultErrMessage.proc.name";
	public static final String TRX_PRODIDMAP_PROC_NAME = "ussd.trxProdIdMap.proc.name";
	public static final String USSD_MPESA_CURRENCY_TYPE = "ussd.mpesa.currency.type";
	public static final String USSD_MPESA_CONF_MENU = "ussd.ml.mpesa.menu.template";
	public static final String USSD_GET_USER_INFO_PROC = "ussd.reduced.ccr.get.info";
	public static final String USSD_CURRECY_MULTIPLIER_1 = "ussd.currency.multiplier.1";
	public static final String USSD_CURRECY_MULTIPLIER_2 = "ussd.currency.multiplier.2";
	public static final String USSD_MIN_LOAN_AMOUNT_CENTS = "ussd.min.loan.amount.cents";
	public static final String INSERT_RAG_USER_CAT_PROC_NAME = "insert.raguser.cat.proc.name";

	public static final String TOPIC = "TOPIC";
	public static final String PRODUCT_IDS = "PRODUCT_IDS";
	public static final String UPSELL_1 = "Upsell_1";
	public static final String UPSELL_2 = "Upsell_2";
	public static final String CROSS_SELL = "Cross_sell";
	public static final String LANG_CODE = "LANG_CODE";
	public static final String REQUEST_STARTTIME = "REQUEST_STARTTIME";
	public static final String TEMPLATE_ID = "TEMPLATE_ID";
	public static final String TEMPLATE = "TEMPLATE";
	public static final String OFFER_ORDER_CSV = "OFFER_ORDER_CSV";
	public static final String NBA_DATE_TIME = "NBA_DATE_TIME";

	public static final String DEFAULT_MESSAGE = "DEFAULT_MESSAGE";
	public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
	public static final String CONFIRMATION_MSG_ERROR = "Default Confirmation Msg could not be loaded from table ECMP_T_DEFAULT_MESSAGE!";
	public static final String STATUS_SUCCESS = "SUCCESS";
	public static final String STATUS_USSD_OFFER_SELECTED = "USSD_OFFER_SELECTED";
	public static final String STATUS_RESPONSE_SENT = "RESPONSE_SENT";
	public static final String STATUS_FINAL_RESPONSE_SENT = "FINAL_RESPONSE_SENT";
	public static final String STATUS_REQUEST_RECEIVED = "REQUEST_RECEIVED";
	public static final String STATUS_EXCEPTION = "EXCEPTION";
	public static final String STATUS_USER_NOT_FOUND = "USER_NOT_FOUND";
	public static final String STATUS_USER_INELIGIBLE = "USER_INELIGIBLE";
	public static final String STATUS_OFFERS_NOT_FOUND = "OFFERS_NOT_FOUND";
	public static final String STATUS_USSD_OFFER_FOUND = "USSD_OFFER_FOUND";
	public static final String STATUS_PROD_IDS_INSUF = "PROD_IDS_INSUF";
	public static final String STATUS_USER_RESPONSE_RECEIVED = "USER_RESPONSE_RECEIVED";
	public static final String STATUS_USSD_TIMEOUT = "USSD_TIMEOUT";
	public static final String STATUS_INVALID_REQUEST = "INVALID_REQUEST";
	public static final String STATUS_AA_LOAN_MENU_REQUEST_RECEIVED = "AA_LOAN_MENU_REQUEST_RECEIVED";
	public static final String STATUS_AA_LOAN_MENU_RESPONSE_SENT = "AA_LOAN_MENU_RESPONSE_SENT";

	public static final String SUBMENU_REQUEST_RECEIVED = "SUBMENU_REQUEST_RECEIVED";
	public static final String SUBMENU_RESPONSE_SENT = "SUBMENU_RESPONSE_SENT";
	public static final String NEWCUSTOMER_MENU_SENT = "NEWCUSTOMER_MENU_SENT";
	public static final String FINAL_REQUEST_RECEIVED = "FINAL_REQUEST_RECEIVED";
	public static final String OCS_BALANCE_RECEIVED = "OCS_BALANCE_RECEIVED";

	public static final String PAYMENT_MENU_REQUEST_RECEIVED = "PAYMENT_MENU_REQUEST_RECEIVED";
	public static final String CURRENCY_MENU_REQUEST_RECEIVED = "CURRENCY_MENU_REQUEST_RECEIVED";
	public static final String PAYMENT_MENU_RESPONSE_SENT = "PAYMENT_MENU_RESPONSE_SENT";
	public static final String CURRENCY_MENU_RESPONSE_SENT = "CURRENCY_MENU_RESPONSE_SENT";

	// RAG status
	public static final String RAG_MENU_REQUEST_RECEIVED = "RAG_MENU_REQUEST_RECEIVED";
	public static final String RAG_MENU_RESPONSE_SENT = "RAG_MENU_RESPONSE_SENT";
	public static final String RAG_SUBMENU_REQUEST_RECEIVED = "RAG_SUBMENU_REQUEST_RECEIVED";
	public static final String RAG_SUBMENU_RESPONSE_SENT = "RAG_SUBMENU_RESPONSE_SENT";
	public static final String RAG_OFFERMENU_REQUEST_RECEIVED = "RAG_OFFERMENU_REQUEST_RECEIVED";
	public static final String RAG_OFFERMENU_RESPONSE_SENT = "RAG_OFFERMENU_RESPONSE_SENT";

	// SAG status
	public static final String SAG_MENU_REQUEST_RECEIVED = "SAG_MENU_REQUEST_RECEIVED";
	public static final String SAG_MENU_RESPONSE_SENT = "SAG_MENU_RESPONSE_SENT";
	public static final String SAG_SUBMENU_REQUEST_RECEIVED = "SAG_SUBMENU_REQUEST_RECEIVED";
	public static final String SAG_SUBMENU_RESPONSE_SENT = "SAG_SUBMENU_RESPONSE_SENT";
	public static final String SAG_OFFERMENU_REQUEST_RECEIVED = "SAG_OFFERMENU_REQUEST_RECEIVED";
	public static final String SAG_OFFERMENU_RESPONSE_SENT = "SAG_OFFERMENU_RESPONSE_SENT";
	public static final String MY_REWARDS_MENU_REQUEST_RECEIVED = "MY_REWARDS_MENU_REQUEST_RECEIVED";
	public static final String MY_REWARDS_MENU_RESPONSE_SENT = "MY_REWARDS_MENU_RESPONSE_SENT";

	public static final String ML_CUSTOMER_FLAG = "ML_CUSTOMER_FLAG";
	public static final String RANDOM_FLAG = "RANDOM_FLAG";
	public static final String J4U_ELIGIBILITY = "J4U_ELIGIBILITY";
	public static final String ML_FLAG = "ML_FLAG";
	public static final String RAG_MSISDN = "RAG_MSISDN";
	public static final String SAG_MSISDN = "SAG_MSISDN";
	public static final String MPESA_USER_FLAG = "MPESA_USER_FLAG";
	public static final String PED_ELIGIBILITY = "PED_ELIGIBILITY";
	public static final String FLAG_Y = "Y";
	public static final String FLAG_N = "N";
	public static final String FLAG_O = "O";
	public static final String VALUE_N = "N";
	public static final String SEL_PROD_TYPE = "SEL_PROD_TYPE";

	public static final int PROD_IDS_NOT_FOUND = 404;
	public static final int INVALID_SELECTION = 401;
	public static final int EIGHTEEN = 18;
	public static final int SEVENTEEN = 17;
	public static final String ZERO_0 = "0";
	public static final String ONE_1 = "1";
	public static final String TWO_2 = "2";
	public static final String NINTY = "90";
	public static final String NINTY_NINE = "99";
	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final int TWO = 2;
	public static final int THREE = 3;
	public static final int FOUR = 4;
	public static final int FIVE = 5;
	public static final String STATUS_FAILED = "FAILED";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String PID_1 = "PID_1";
	public static final String PID_2 = "PID_2";
	public static final String PID_3 = "PID_3";
	public static final String PID_4 = "PID_4";
	public static final String PID_5 = "PID_5";

	public static final String MSISDN = "MSISDN";
	public static final String TRX_ID = "TRX_ID";
	public static final String DATE_TIME = "DATE_TIME";
	public static final String MSG_CODE = "MSG_CODE";

	public static final String PRODUCT_ID = "PRODUCT_ID";
	public static final String PRODUCT_PRICE = "PRODUCT_PRICE";
	public static final String CURRENCY_TYPE = "CURRENCY_TYPE";
	public static final String TRANSACTION_ID = "TRANSACTION_ID";
	public static final String PROG_SHORT_CD = "PROG_SHORT_CD";
	public static final String PRODUCT_DESC = "PRODUCT_DESC";
	public static final String B_VALUE = "B_VALUE";
	public static final String C_VALUE = "C_VALUE";

	public static final String MENU_MIN = "ussd.menuSelection.min";
	public static final String MENU_MAX = "ussd.menuSelection.max";

	public static final String USSD_DELIVERSM_THREADCOUNT = "ussd.deliversm.threadcount";
	public static final String USSD_SUBMITSM_THREADCOUNT = "ussd.submitsm.threadcount";

	public static final String SUBID_LENGTH = "ussd.msisdn.length";
	public static final String SUBID_CNTRYCD = "ussd.msisdn.countryCode";
	public static final String USSD_INCOMING_REQ_CODE = "ussd.incoming.request.codes";
	public static final String USSD_ML_LOAN_MENU_TEMPLATE = "ussd.ml.loan.menu.template";
	public static final String USSD_ML_NOT_ENOUGH_BAL_TEMPLATE = "ussd.ml.not.enough.balance.template";
	public static final String USSD_ML_PAYMENT_MENU_TEMPLATE = "ussd.ml.payment.menu.template";
	public static final String USSD_ML_CURRENCY_MENU_TEMPLATE = "ussd.ml.currency.menu.template";
	public static final String USSD_SUB_MENU_PROD_TYPE = "ussd.sub.menu.prod.type";
	public static final String USSD_OFFER_REFRESH_FLAG_MAP = "ussd.offer.refresh.flag.map";
	public static final String USSD_ML_NOT_ENOUGH_PRODS_TEMPLATE = "ussd.ml.sub.menu.not.enough.prods.template";
	public static final String USSD_ML_NO_AVAILABLE_PRODS_TEMPLATE = "ussd.ml.sub.menu.no.available.prods.template";
	public static final String USSD_ACCOUNT_DISP_FLAG = "ussd.account.display.flag";
	public static final String USER_IN_ELIGIBLE_TEMPLATE = "user.ineligible.template";
	public static final String USSD_MPESA_ENABLED = "ussd.mpesa.enabled";
	public static final String USSD_AA_ENABLED = "ussd.aa.enabled";
	public static final String USSD_RAG_ATL_ENABLED = "ussd.rag.atl.enabled";
	public static final String USSD_RAG_ATL_REWARD_MAP = "ussd.rag.atl.reward.map";

	// PED STATUS
	public static final String PED_SUBMENU_REQUEST_RECEIVED = "PED_SUBMENU_REQUEST_RECEIVED";
	public static final String PED_SUBMENU_RESPONSE_SENT = "PED_SUBMENU_RESPONSE_SENT";
	public static final String PED_HISTORY_MENU_REQUEST_RECEIVED = "PED_HISTORY_MENU_REQUEST_RECEIVED";
	public static final String PED_HISTORY_MENU_RESPONSE_SENT = "PED_HISTORY_MENU_RESPONSE_SENT";
	public static final String PED_PLAY_MENU_REQUEST_RECEIVED = "PED_PLAY_MENU_REQUEST_RECEIVED";
	public static final String PED_PLAY_MENU_RESPONSE_SENT = "PED_PLAY_MENU_RESPONSE_SENT";
	public static final String PED_AVAIL_PLAY_MENU_REQUEST_RECEIVED = "PED_AVAIL_PLAY_MENU_REQUEST_RECEIVED";
	public static final String PED_AVAIL_PLAY_MENU_RESPONSE_SENT = "PED_AVAIL_PLAY_MENU_RESPONSE_SENT";

	// HourlyData Menu Templates
	public static final String POOL_ID = "POOL_ID";
	public static final String LOCATION_MODULE_GET_POOL_ID = "location.module.get.poolid";
	public static final String ECMP_P_J4U_ML_LOCATION_OFFER = "location.module.get.offer";
	public static final String ENBA_P_GET_LOCATION_INFO = "location.module.get.offer.info";
	public static final String USSD_P_ML_AA_OFFER = "ussd,procedure.fetch.ml.offer";
	public static final String USSD_NON_PROMO_PRODUCT_INFO = "ussd.procedure.fetch.non.promotion.product.info";

	public static final String LOCATION_RANDOM_FLAG = "LOCATION_RANDOM_FLAG";
	// PED Menu Templates
	public static final String USSD_PED_ENABLED = "ussd.ped.enabled";
	public static final String USSD_ML_PED_SUB_MENU = "ussd.ml.ped.sub.menu.template";
	public static final String USSD_ML_PED_PLAY_ALERT_MENU = "ussd.ml.ped.play.alert.menu";
	public static final String USSD_ML_PED_NO_PLAY_MENU = "ussd.ml.ped.no.play.menu.template";
	public static final String USSD_ML_PED_OFFER_MENU = "ussd.ml.ped.offer.menu.template";
	public static final String USSD_ML_PED_NO_PRIZE_MENU = "ussd.ml.ped.no.prize.menu.template";
	public static final String USSD_ML_PED_AVAILABLE_PLAYS_MENU = "ussd.ml.ped.available.plays.menu.template";
	public static final String USSD_ML_PED_HISTORY_MENU = "ussd.ml.ped.history.menu.template";
	public static final String USSD_ML_PED_NO_HISTORY_MENU = "ussd.ml.ped.no.history.menu.template";

	// Srithar ML_RAG Changes
	public static final String USSD_ML_RAG_FIRST_OPT_IN_MENU = "ussd.ml.rag.first.opt.in.menu.template";
	public static final String USSD_ML_RAG_ALREADY_OPT_IN_MENU = "ussd.ml.rag.already.opt.in.menu.template";
	public static final String USSD_ML_RAG_GOAL_REACHED_MENU = "ussd.ml.rag.goal.reached.template";
	public static final String USSD_ML_RAG_INELIGIBLE_MENU = "ussd.ml.rag.ineligible.template";
	public static final String USSD_ML_RAG_OFFER_INFO = "ussd.ml.rag.offer.info";

	public static final String USSD_ML_RAG_SUB_MENU_OPTIONS = "ussd.ml.rag.sub.menu.options";
	public static final String USSD_ML_RAG_SUB_MENU_OFFER_INFO_SELECTION = "ussd.ml.rag.sub.menu.offer.info.selection";
	public static final String USSD_ML_RAG_SUB_MENU_OPT_OUT_SELECTION = "ussd.ml.rag.sub.menu.opt.out.selection";
	public static final String USSD_ML_RAG_OFFER_INFO_OPTIONS = "ussd.ml.rag.offer.info.options";
	public static final String USSD_ML_RAG_OFFER_INFO_BACK_SELECTION = "ussd.ml.rag.offer.info.back.selection";

	public static final String USSD_PRODUCT_PRICE_LIST_PROC = "ussd.product.price.list.proc";

	public static final String SMS_TOPIC_NAME = "sms.topic.name";
	public static final String SMS_OPT_IN_TEMPLATE = "sms.optin.template";
	public static final String SMS_OPT_OUT_TEMPLATE = "sms.optout.template";

	/**
	 * Query Balance plugin entries
	 */

	public static final String OCS_QUERY_BAL_TOPIC = "ocs.query.bal.topic";
	public static final String USSD_CONSUMER_TOPIC = "ussd.consumer.topic";
	public static final String OCS_QUERY_BAL_STATUS = "STATUS";
	public static final String OCS_QUERY_BAL_SUCCESS = "OCS_SUCCESS";
	public static final String ACCOUNT_BALANCE = "ACCOUNT_BALANCE";
	public static final String AA_PROV_AMOUNT = "AA_PROV_AMOUNT";
	public static final String PRODUCT_TYPE = "PRODUCT_TYPE";
	public static final String PRODUCT_SUBTYPE = "PRODUCT_SUBTYPE";
	public static final String MENU_CONTENT = "MENU_CONTENT";
	public static final String RF_VALUE = "RF_VALUE";

	public static final int LOCATION_NUMBER = 0x1500;
	public static final int CELL_ID = 0x1600;
	public static final String CELL_ID_KEY_NAME = "CELL_ID";
	public static final String LOCATION_NUMBER_KEY = "LOCATION_NUMBER";
	public static final int MAX_PRODIDS_CNT_3 = 3;
	public static final int MAX_PRODIDS_CNT_2 = 2;
	public static final int MAX_PRODIDS_CNT_1 = 1;
	public static final String USER_SELECTION = "USER_SELECTION";
	public static final String DEST_ADDRESS = "DEST_ADDRESS";
	public static final String USER_MSG_REF = "USER_MSG_REF";
	public static final String SHORT_MSG = "SHORT_MSG";
	public static final String OFFER_REFRESH_FLAG = "OFFER_REFRESH_FLAG";
	public static final String AA_ELIGIBLE = "AA_ELIGIBLE";
	public static final String AIRTIME_ADVANCE_BALANCE = "AIRTIME_ADVANCE_BALANCE";
	public static final String REFRESH_RF_VALUE = "REFRESH_RF_VALUE";
	public static final String OFFER_ACTIVATION_FAILURE = "Offer Activation Failure";
	public static final String REW_PRODUCT_PRICE = "PRODUCT_PRICE";
	public static final String PREF_PAY_MET_MPESA = "M";
	public static final String PREF_PAY_METHOD = "PREF_PAY_METHOD";
	public static final String PREF_PAY_METHOD_G = "G";
	public static final String A_VALUE = "A_VALUE";

	/**
	 * USSD menu options
	 */
	public static final String PED_SUB_MENU = "PED_SUB_MENU";
	public static final String PED_PLAY_MENU = "PED_PLAY_MENU";
	public static final String PED_AVAIL_PLAY_MENU = "PED_AVAIL_PLAY_MENU";
	public static final String PED_HISTORY_MENU = "PED_HISTORY_MENU";
	public static final String PED_OFFER_MENU = "PED_OFFER_MENU";
	public static final String PED_BACK_MENU = "PED_BACK_MENU";
	public static final String J4U_MENU = "J4U_MENU";
	public static final String ML_MENU_MENU = "ML_MENU_MENU";
	public static final String ML_SUB_MENU = "ML_SUB_MENU";
	public static final String ML_OFFER_MENU = "ML_OFFER_MENU";
	public static final String ML_PAYMENT_MENU = "ML_PAYMENT_MENU";
	public static final String ML_LOAN_MENU = "ML_LOAN_MENU";
	public static final String ML_CURRENCY_MENU = "ML_CURRENCY_MENU";
	public static final String RAG_MAIN_MENU = "RAG_MAIN_MENU";
	public static final String RAG_SUB_MENU = "RAG_SUB_MENU";
	public static final String RAG_OFFER_MENU = "RAG_OFFER_MENU";
	public static final String RAG_MENU = "RAG_MENU";
	public static final String SAG_MENU = "SAG_MENU";
	public static final String SAG_MAIN_MENU = "SAG_MAIN_MENU";
	public static final String SAG_SUB_MENU = "SAG_SUB_MENU";
	public static final String SAG_OFFER_MENU = "SAG_OFFER_MENU";
	public static final String USER_SEL_0 = "0";
	public static final String USER_SEL_1 = "1";
	public static final String USER_SEL_2 = "2";
	public static final String USER_SEL_3 = "3";
	public static final String USER_SEL_4 = "4";
	public static final String USER_SEL_5 = "5";
	public static final String USER_SEL_6 = "6";
	public static final String USER_SEL_7 = "7";
	public static final String USER_SEL_8 = "8";
	public static final String J4U_VOICE = "J4U_VOICE";
	public static final String J4U_DATA = "J4U_DATA";
	public static final String J4U_INTEGRATED = "J4U_INTEGRATED";
	public static final String J4U_HOURLY_DATA = "J4U_HOURLY DATA";
	public static final String HOURLY_SUB_MENU = "HOURLY_SUB_MENU";
	public static final String J4U_SOCIAL_DATA = "J4U_SOCIAL_Media";
	public static final String ML_SOCIAL_SUB_MENU = "ML_SOCIAL_SUB_MENU";
	public static final String MORNING_SUB_MENU = "ML_MORNING_SUB_MENU";
	public static final String MORNING_OFFER_MENU = "ML_MORNING_OFFER_MENU";
	public static final String NEW_USER_INVALID_MENU_SELECTION = "NEW_USER_INVALID_MENU_SELECTION";
	public static final String INVALID_MENU_SELECTION = "INVALID_MENU_SELECTION";
	public static final String MY_REWARDS_MENU = "MY_REWARDS_MENU";
	public static final String MY_REWARDS = "MY_REWARDS";

	// wrong input template ids for PED
	public static final String ML_PED_OFFER_MENU_WRONG_SELECTION = "ML_PED_OFFER_MENU_WRONG_SELECTION";
	public static final String ML_PED_HISTORY_MENU_WRONG_SELECTION = "ML_PED_HISTORY_MENU_WRONG_SELECTION";
	public static final String ML_PED_AVAIL_PLAY_MENU_WRONG_SELECTION = "ML_PED_AVAIL_PLAY_MENU_WRONG_SELECTION";
	public static final String ML_PED_PLAY_MENU_WRONG_SELECTION = "ML_PED_PLAY_MENU_WRONG_SELECTION";
	public static final String ML_PED_SUB_MENU_WRONG_SELECTION = "ML_PED_SUB_MENU_WRONG_SELECTION";

	// wrong input template ids for J4u and RAG
	public static final String ML_RAG_MAIN_MENU_WRONG_SELECTION = "ML_RAG_MAIN_MENU_WRONG_SELECTION";
	public static final String ML_RAG_SUB_MENU_WRONG_SELECTION = "ML_RAG_SUB_MENU_WRONG_SELECTION";
	public static final String ML_RAG_OFFER_MENU_WRONG_SELECTION = "ML_RAG_OFFER_MENU_WRONG_SELECTION";
	public static final String ML_J4U_MENU_WRONG_SELECTION = "ML_j4U_MENU_WRONG_SELECTION";
	public static final String ML_J4U_STATIC_MENU_WRONG_SELECTION = "ML_J4U_STATIC_MENU_WRONG_SELECTION";
	public static final String ML_J4U_OFFER_WRONG_SELECTION = "ML_J4U_OFFER_WRONG_SELECTION";
	public static final String ML_J4U_LOAN_MENU_WRONG_SELECTION = "ML_J4U_LOAN_MENU_WRONG_SELECTION";
	public static final String ML_CURRENCY_MENU_WRONG_SELECTION = "ML_CURRENCY_MENU_WRONG_SELECTION";
	public static final String ML_PAYMENT_MENU_WRONG_SELECTION = "ML_PAYMENT_MENU_WRONG_SELECTION";

	// OPENNET
	public static final String ROUTER_COUNTRY_CODE = "ussd.msisdn.countryCode";
	public static final String ROUTER_MSISDN_RANGE_LENGTH = "ussd.msisdnrange.length";
	public static final String CS_TYPE_HW = "HW";
	public static final String CS_TYPE_IM = "IM";
	public static final String CS_TYPE_OP = "OP";
	public static final String ROUTER_CS_MSISDNRANGE_QUERY = "ussd.cs.msisdnrange.query";
	public static final String ROUTER_CS_MSISDN_QUERY = "ussd.cs.msisdn.query";
	public static final String OP_REWARDSPUB_TOPIC_NAME = "ussd.op.rewardspublisher.topic.name";
	public static final String CCS_QUERY_BAL_TOPIC = "ussd.op.ccs.query.bal.topic";
	public static final String USSD_ENABLE_MULTI_CS = "ussd.enable.multi.cs";

	// ##J4U Social Data Bundle
	public static final String USSD_SOCIAL_ENABLED = "ussd.social.enabled";
	public static final String USSD_ML_SOCIAL_QUERY_OFFER_PROC = "ussd.ml.social.query.offer.proc";
	public static final String J4U_SOCIAL_PRODINFO_PROC_NAME = "ussd.social.prodinfo.proc";
	public static final String USSD_ML_SOCIAL_OFFER_BYRANK_PROC = "ussd.ml.social.offer.byrank.proc";
	public static final String USSD_ML_SOCIAL_OFFER_BYEXP_PROC = "ussd.ml.social.offer.byexp.proc";

	public static final String USSD_LOCATION_PRODINFO_PROC_NAME = "ussd.location.prodinfo.proc";
	public static final String WEEK_START_DATE = "WEEK_START_DATE";
	public static final String WEEK_END_DATE = "WEEK_END_DATE";
	public static final String NEXT_AVAILABLE_OFFER_DATE = "NEXT_AVAILABLE_OFFER_DATE";
	public static final String REWARD_CODE = "REWARD_CODE";
	public static final String REWARD_INFO = "REWARD_INFO";
	public static final String TARGET_TYPE = "TARGET_TYPE";
	public static final String OTHER = "OTHER";
	public static final String PAYMENT_METHOD = "PAYMENT_METHOD";
	public static final String RECHARGE_TARGET = "RECHARGE_TARGET";
	public static final String SPEND_TARGET = "SPEND_TARGET";
	public static final String VALUE_P = "P";
	public static final String RAG_OPT_FLAG = "RAG_OPT_FLAG";
	public static final String RAG_GOAL_REACHED_FLAG = "RAG_GOAL_REACHED_FLAG";
	public static final String SAG_OPT_FLAG = "SAG_OPT_FLAG";
	public static final String SAG_GOAL_REACHED_FLAG = "SAG_GOAL_REACHED_FLAG";
	public static final String PRODUCT_VALIDITY = "PRODUCT_VALIDITY";
	public static final String PRODUCT_VALUE = "PRODUCT_VALUE";
	public static final String LAST_RECHARGE_TIME = "LAST_RECHARGE_TIME";
	public static final String LAST_SPEND_TIME = "LAST_SPEND_TIME";
	public static final String REMAINING_EFFORT = "REMAINING_EFFORT";

	// J4U Morning Offer
	public static final String MORNING_OFFER_ELIGIBILITY = "MORNING_OFFER_ELIGIBILITY";
	public static final String JUSTE_POUR_TOI = "JUSTE POUR TOI";
	public static final String USSD_MORNING_OFFER_WINDOW_START = "ussd.ml.morning.offer.window.start";
	public static final String USSD_MORNING_OFFER_WINDOW_END = "ussd.ml.morning.offer.window.end";
	public static final String USSD_ML_MORNING_OFFER_PRODINFO_PROC = "ussd.ml.morning.offer.prodinfo.proc";
	public static final String USSD_ML_GET_MORNING_OFFER_PROC = "ussd.ml.get.morning.offer.proc";
	public static final String USSD_ML_MO_FAILURE_MSG_TEMPLATE = "ussd.ml.morning.offer.failure.msg.template";
	public static final String TEMP_FR_MORNING_OFFER_MENU = "ussd.non.ml.morning.offer.menu.template";
	public static final String MORNING_OFFER_FAILURE_MSG_REQUEST_RECIEVED = "MORNING_OFFER_FAILURE_MSG_REQUEST_RECIEVED";
	public static final String MORNING_OFFER_ACTIVATION_REQUEST_RECIEVED = "MORNING_OFFER_ACTIVATION_REQUEST_RECIEVED";
	public static final String NO_OFFERS_AVAILABLE_FAILURE_MSG_REQUEST_RECIEVED = "NO_OFFERS_AVAILABLE_FAILURE_MSG_REQUEST_RECIEVED";
	public static final String USSD_ML_NO_OFFERS_AVAILABLE_FAILURE_MSG_TEMPLATE = "ussd.ml.no.offers.available.failure.msg.template";

	// J4U SPENT and GET
	public static final String USSD_SAG_ATL_ENABLED = "ussd.sag.atl.enabled";
	public static final String USSD_SAG_ATL_REWARD_MAP = "ussd.sag.atl.reward.map";
	public static final String USSD_ML_MY_REWARDS_ALL = "ussd.ml.my.rewards.all";
	public static final String USSD_ML_MY_REWARDS_RP = "ussd.ml.my.rewards.rp";
	public static final String USSD_ML_MY_REWARDS_RS = "ussd.ml.my.rewards.rs";
	public static final String USSD_ML_MY_REWARDS_PS = "ussd.ml.my.rewards.ps";
	public static final String USSD_ML_MY_REWARDS_R = "ussd.ml.my.rewards.r";
	public static final String USSD_ML_MY_REWARDS_P = "ussd.ml.my.rewards.p";
	public static final String USSD_ML_MY_REWARDS_S = "ussd.ml.my.rewards.s";
	public static final String USSD_ML_SAG_FIRST_OPT_IN_MENU = "ussd.ml.sag.first.opt.in.menu.template";
	public static final String USSD_ML_SAG_ALREADY_OPT_IN_MENU = "ussd.ml.sag.already.opt.in.menu.template";
	public static final String USSD_ML_SAG_GOAL_REACHED_MENU = "ussd.ml.sag.goal.reached.template";
	public static final String USSD_ML_SAG_OFFER_INFO = "ussd.ml.sag.offer.info.template";
	public static final String USSD_ML_SAG_INELIGIBLE_MENU = "ussd.ml.sag.ineligible.template";
	public static final String USSD_SAG_SMS_OPT_IN_TEMPLATE = "ussd.sag.sms.optin.template";
	public static final String USSD_SAG_SMS_OPT_OUT_TEMPLATE = "ussd.sag.sms.optout.template";
	public static final String USSD_P_ML_SAG_USER_RECORD_SELECT = "ussd.ml.sag.user.record.select.procedure";
	public static final String USSD_P_ML_SAG_USER_RECORD_INSERT = "ussd.ml.sag.user.record.insert.procedure";
	public static final String USSD_P_ML_SAG_INFO_SELECT = "ussd.ml.sag.info.select.procedure";
	public static final String USSD_P_INSERT_SAG_OPT_INFO = "ussd.ml.sag.opt.info.insert.procedure";
	public static final String USSD_P_UPDATE_SAG_OPT_INFO = "ussd.ml.sag.opt.info.update.procedure";
	public static final String USSD_P_INSERT_ECMP_SAG_OPT_INFO = "ussd.ml.sag.opt.info.insert.ecmp.procedure";
	public static final String INSERT_SAG_USER_CAT_PROC_NAME = "insert.saguser.cat.proc.name";

	// CONSENT MENU
	public static final String OVERALL_CONSENT_ENABLED = "ussd.overall.consent.enable";
	public static final String VOICE_CONSENT_ENABLED = "ussd.ml.consent.voice.enable";
	public static final String DATA_CONSENT_ENABLED = "ussd.ml.consent.data.enable";
	public static final String INTEGRATED_CONSENT_ENABLED = "ussd.ml.consent.integrated.enable";
	public static final String HOURLY_CONSENT_ENABLED = "ussd.ml.consent.hourly.enable";
	public static final String SOCIAL_CONSENT_ENABLED = "ussd.ml.consent.social.enable";
	public static final String NON_ML_CONSENT_ENABLED = "ussd.non.ml.consent.enable";
	public static final String CLEAR_CONSENT_MENU = "CLEAR_CONSENT_MENU";
	public static final String CLEAR_USERINFO_MAP = "CLEAR_USERINFO_MAP";
	public static final String CONSENT_DENIED = "CONSENT_DENIED";
	public static final String CONSENT_ACCEPTED = "CONSENT_ACCEPTED";
	public static final String CONSENT_FLAG = "CONSENT_FLAG";
	public static final String CONSENT_OPT_OUT_MENU = "CONSENT_OPT_OUT_MENU";
	public static final String CONSENT_OPT_OUT = "CONSENT_OPT_OUT";
	public static final String CONSENT_OPTOUT = "CONSENT_OPTOUT";
	public static final String CONSENT_OPT_OUT_REJECTED = "CONSENT_OPT_OUT_REJECTED";
	public static final String CONSENT_MENU_REQUEST_RECEIVED = "CONSENT_MENU_REQUEST_RECEIVED";
	public static final String USSD_CONSENT_MENU_TEMPLATE_SENT = "USSD_CONSENT_MENU_TEMPLATE_SENT";
	public static final String CONSENT_OPT_OUT_REQUEST_RECEIVED = "CONSENT_OPT_OUT_REQUEST_RECEIVED";
	public static final String USSD_CONSENT_OPT_OUT_TEMPLATE_SENT = "USSD_CONSENT_OPT_OUT_TEMPLATE_SENT";
	public static final String USSD_CONSENT_OPT_OUT_FINAL_TEMPLATE_SENT = "USSD_CONSENT_OPT_OUT_FINAL_TEMPLATE_SENT";
	public static final String CONSENT_OPT_OUT_RESPONSE_SENT = "CONSENT_OPT_OUT_RESPONSE_SENT";
	public static final String USSD_ML_CONSENT_MENU_TEMPLATE = "ussd.ml.consent.menu.template";
	public static final String USSD_ML_CONSENT_DENIED_TEMPLATE = "ussd.ml.consent.denied.msg.template";
	public static final String USSD_ML_CONSENT_OPT_OUT_MENU_TEMPLATE = "ussd.ml.consent.opt.out.menu.template";
	public static final String USSD_ML_CONSENT_OPTED_OUT_TEMPLATE = "ussd.ml.consent.opted.out.template";
	public static final String USSD_ML_CONSENT_OPT_OUT_REJECTED_TEMPLATE = "ussd.ml.consent.opt.out.rejected.template";

	// main menu when social disabled
	public static final String USSD_ML_MAIN_MENU_MO_CONSENT_PART_1 = "ussd.ml.main.menu.mo.consent.template.part1";
	public static final String USSD_ML_MAIN_MENU_MO_CONSENT_PART_2 = "ussd.ml.main.menu.mo.consent.template.part2";
	public static final String USSD_ML_MAIN_MENU_MO = "ussd.ml.main.menu.mo.template";
	public static final String USSD_ML_MAIN_MENU_NO_MS_CONSENT = "ussd.ml.main.menu.no.ms.consent.template";
	public static final String USSD_ML_MAIN_MENU_NO_MS = "ussd.ml.main.menu.no.ms.template";

	// main menu when social enabled
	public static final String USSD_ML_MAIN_MENU_MS_CONSENT_PART_1 = "ussd.ml.main.menu.ms.consent.template.part1";
	public static final String USSD_ML_MAIN_MENU_MS_CONSENT_PART_2 = "ussd.ml.main.menu.ms.consent.template.part2";
	public static final String USSD_ML_MAIN_MENU_MS_PART_1 = "ussd.ml.main.menu.ms.template.part1";
	public static final String USSD_ML_MAIN_MENU_MS_PART_2 = "ussd.ml.main.menu.ms.template.part2";
	public static final String USSD_ML_MAIN_MENU_SO_CONSENT_PART_1 = "ussd.ml.main.menu.so.consent.template.part1";
	public static final String USSD_ML_MAIN_MENU_SO_CONSENT_PART_2 = "ussd.ml.main.menu.so.consent.template.part2";
	public static final String USSD_ML_MAIN_MENU_SO = "ussd.ml.main.menu.so.template";

	public static final String USSD_NON_ML_CONSENT_OPT_OUT = "ussd.non.ml.consent.opt.out.template";
	public static final String USSD_P_GET_CONSENT_STATUS = "ussd.get.consent.status.procedure";
	public static final String USSD_P_UPSERT_J4U_CONSENT_STATUS = "ussd.upsert.j4u.consent.status.procedure";
	public static final String USSD_P_UPDATE_J4U_CONSENT_STATUS = "ussd.update.j4u.consent.status.procedure";

	// MPESA_QUERY_BALANCE PLUGIN
	public static final String MPESA_QUERY_BALANCE_TOPIC = "mpesa.querybalance.topic";
	public static final String MPESA_BALANCE_RECEIVED = "MPESA_BALANCE_RECEIVED";

	// BLACKLIST CR
	public static final String BLACKLIST_MSISDN = "BLACKLIST_MSISDN";
	public static final String USSD_BLACKLIST_ENABLED = "blacklisting.msisdns.enabled";
	public static final String STATUS_BLACKLIST_FINAL_RESPONSE_SENT = "BLACKLIST_FINAL_RESPONSE_SENT";
	public static final String USSD_ML_BLACKLIST_MSISDN = "ussd.ml.blacklist.msisdn.template";
	public static final String USSD_GET_USER_INFO_WITH_BLIST_PROC = "ussd.blacklist.reduced.ccr.get.userinfo";
	public static final String P_GET_OPENET_BLACKLIST_MSISDN = "ussd.get.openet.blacklist.msisdn.proc";

	public static final String SUCCESS = "SUCCESS";

	// Display main balance CR
	public static final String SUBMENU_QUERYBALANCE_CALL_ENABLED = "ussd.submenu.querybal.call.enabled";
	public static final String MICRO_DOLLAR = "currency.multiplier";
	public static final String BAL_DECIMAL_PLACES = "balance.decimal.places";
	public static final String BALANCE_TEMPLATE = "ussd.balance.template";
	public static final String J4U_MORNING_OFFER = "J4U_MORNING_OFFER";
	public static final String MAIN_MENU = "MAIN_MENU";
	public static final String OCS_QUERY_BAL_TIMEOUT = "TIMEOUT";
	public static final String VALUE_G = "G";
	public static final String FAILED = "FAILED";

	// J4U Town CR
	public static final String USSD_ML_J4U_TOWN_OFFER_PRODINFO_PROC = "ussd.ml.j4u.town.offer.prodinfo.proc";
	public static final String USSD_ML_J4U_CELLID_TOWNNAME_MAP_PROC = "ussd.ml.j4u.cellid.townname.map.proc";
	public static final String USSD_ML_GET_TOWN_PRODUCT_IDS_PROC = "ussd.ml.j4u.get.town.productids.proc";
	public static final String USSD_GET_NEW_CUSTOMER_PROFILE_PROC = "ussd.get.new.customer.profile.proc";
	public static final String USSD_P_ML_NEW_USER_RECORD_INSERT = "ussd.ml.new.user.record.insert.procedure";
	public static final String USSD_ML_TOWN_FAILURE_MSG_TEMPLATE = "ussd.ml.town.offer.failure.msg.template";
	public static final String USSD_ML_GET_TOWN_OFFER_PROC = "ussd.ml.get.town.offer.proc";
	public static final String ML_1_OFFER_SUBMENU_TEMPLATE = "ussd.ml.1offer.submenu.template";
	public static final String ML_2_OFFER_SUBMENU_TEMPLATE = "ussd.ml.2offer.submenu.template";
	public static final String ML_3_OFFER_SUBMENU_TEMPLATE = "ussd.ml.3offer.submenu.template";
	public static final String ML_4_OFFER_SUBMENU_TEMPLATE = "ussd.ml.4offer.submenu.template";
	public static final String ML_5_OFFER_SUBMENU_TEMPLATE = "ussd.ml.5offer.submenu.template";
	public static final String USSD_ICAP_LANG_CODE_MAP = "ussd.new.customer.icap.lang.code.map";
	public static final String ICAP_SUBSCRIBER_NOT_FOUND_ERROR_CODE = "icap.subscriber.not.found.error.code";
	public static final String J4U_TOWN_SUB_MENU = "ML_TOWN_SUB_MENU";
	public static final String TOWN_NAME = "TOWN_NAME";
	public static final String STATUS_ICAP_FAILURE = "ICAP_FAILURE";
	public static final String ENABLED = "ENABLED";
	public static final String STAR = "*";
	public static final String HASH = "#";
	public static final String MAINMENUSEGMENTS = "MAINMENUSEGMENTS";
	public static final String BALANCE_PATTERN = "@balance@";
	public static final String J4U_TOWN = "J4U_TOWN";
	public static final String OFFER_PAYMENT_METHOD = "OFFER_PAYMENT_METHOD";
	public static final String PREPAID = "P";
	public static final String HYBRID = "M";
	public static final String SUBSCRIBER_STATE_ACTIVE = "Active";
	public static final String FRENCH = "French";
	
	public static final String AD_HOC = "@AdHoc";
	public static final int TWO_HUNDRED = 200;
	public static final int ONE_HUNDRED_SIXTY = 160;
	public static final int TEN_THOUSAND = 10000;
	public static final int SIX	 = 6;
	public static final int SEVEN = 7;
	public static final int EIGHT = 8;
	public static final int NINE = 9;
	public static final int TEN = 10;
	public static final int ELEVEN = 11;
	public static final int TWELVE = 12;
	public static final int THIRTEEN = 13;
	public static final int FOURTEEN = 14;
	public static final int FIFTEEN = 15;
	public static final int SIXTEEN = 16;
	public static final int NINETEEN = 19;
	
}