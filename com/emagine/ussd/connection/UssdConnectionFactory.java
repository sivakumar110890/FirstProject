package com.emagine.ussd.connection;

import static com.comviva.icap.utils.ICAPConstants.ERROR_CODE;
import static com.comviva.icap.utils.ICAPConstants.ICAP_LANGUAGE_CATEGORY;
import static com.comviva.icap.utils.ICAPConstants.ICAP_OFFER_PAYMENT_METHOD;
import static com.comviva.icap.utils.ICAPConstants.ICAP_PAYMENT_METHOD;
import static com.comviva.icap.utils.ICAPConstants.ICAP_STATUS;
import static com.comviva.icap.utils.ICAPConstants.ICAP_SUBSCRIBER_START_DATE;
import static com.comviva.icap.utils.ICAPConstants.ICAP_SUBSCRIBER_STATE;
import static com.comviva.icap.utils.ICAPConstants.ICAP_MSISDN;
import static com.emagine.ussd.utils.USSDConstants.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.comviva.icap.service.QuerySubscriberProfile;
import com.comviva.ped.service.PEDProcessService;
import com.emagine.ussd.config.PropertiesLoader;
import com.emagine.ussd.dao.LookUpDAO;
import com.emagine.ussd.dao.UpdaterDAO;
import com.emagine.ussd.model.InboundUssdMessage;
import com.emagine.ussd.model.J4UNewCustomerProfile;
import com.emagine.ussd.model.TemplateDTO;
import com.emagine.ussd.model.TownProdInfo;
import com.emagine.ussd.model.UserInfo;
import com.emagine.ussd.service.InboundMessageService;
import com.emagine.ussd.service.publisher.UssdEventPublisher;
import com.emagine.ussd.utils.CellIDPoolIDCache;
import com.emagine.ussd.utils.ProductInfoCache;
import com.emagine.ussd.utils.QueryBaljsonMap;
import com.emagine.ussd.utils.RAGDefaultsCache;
import com.emagine.ussd.utils.SAGDefaultsCache;
import com.emagine.ussd.utils.UserInfoMapCache;
import static com.emagine.ussd.utils.Utils.getCurrentTimeStamp;
import com.emagine.ussd.utils.Utils;

import ie.omk.smpp.Address;
import ie.omk.smpp.AlreadyBoundException;
import ie.omk.smpp.BadCommandIDException;
import ie.omk.smpp.Connection;
import ie.omk.smpp.event.ReceiverExitEvent;
import ie.omk.smpp.event.SMPPEventAdapter;
import ie.omk.smpp.message.BindResp;
import ie.omk.smpp.message.DeliverSM;
import ie.omk.smpp.message.DeliverSMResp;
import ie.omk.smpp.message.EnquireLink;
import ie.omk.smpp.message.EnquireLinkResp;
import ie.omk.smpp.message.InvalidParameterValueException;
import ie.omk.smpp.message.SMPPPacket;
import ie.omk.smpp.message.SMPPProtocolException;
import ie.omk.smpp.message.SMPPRequest;
import ie.omk.smpp.message.SMPPResponse;
import ie.omk.smpp.message.SubmitSM;
import ie.omk.smpp.message.SubmitSMResp;
import ie.omk.smpp.message.Unbind;
import ie.omk.smpp.message.UnbindResp;
import ie.omk.smpp.message.tlv.Tag;
import ie.omk.smpp.util.AlphabetEncoding;
import ie.omk.smpp.util.EncodingFactory;
import ie.omk.smpp.util.MessageEncoding;
import ie.omk.smpp.util.SequenceNumberScheme;
import ie.omk.smpp.version.VersionException;

/**
 * UssdConnectionFactory class
 */
public class UssdConnectionFactory {

    private static final Logger LOG = Logger.getLogger(UssdConnectionFactory.class);
    private static final String ERROR_OCCURED = "Error occured at :: ";
    private static final int ESME_ROK = 0x00000000;
    private static final int ESME_RTHROTTLED = 0x00000058;
    private static final int ESME_RMSGQFUL = 0x00000014;
    private static final int ACTIVE = 2;    
    private static Properties properties = new Properties();
    private static boolean isClearActiveSession = true;
    private static String ussdHostName = "";
    private static int ussdPort;
    private static String ussdSystemId = "";
    private static String ussdSystemPassword = "";
    private static String ussdSystemType = "";
    private static int esmeTON;
    private static int esmeNPI = 1;
    private static String esmeAddressRangeRegex = "";
    private static int keepAliveCycleInSeconds;
    private static int bufferSize;
    private static int maxMessagesPerSecond;
    private static int index;
    private static int retryCount;
    private static int retryPollCycleSeconds = 60;
    private static boolean displayFlag;

    private static ConcurrentLinkedDeque<TransmitMessage> transmitMessageBuffer;

    private static Map<Integer, UssdSession> activeSessions = new ConcurrentHashMap<>();    
    private static List<String> ussdIngReqList;
    private static Map<String, String> mlRefreshFlagMap;
    private static Map<String, String> mlSubMenuProdTypeMenu;
    private static Map<String, String> ragAtlRewardCodeTargetMap;
    private static Map<String, String> sagAtlRewardCodeTargetMap;
    private static Map<String, String> icapLangCodeMap;
    private static QuerySubscriberProfile querySubscriberProfile;
    private static LookUpDAO lookUpDAO;
    private static UpdaterDAO updaterDao;
    private static boolean isMpesaEnabled;
    private static boolean isAAEnabled;
    private static boolean isRagAtlEnabled;
    private static boolean isPEDEnabled;
    private static int isSocialEnabled;
    private static int isGatewayEnabled;
    private static boolean isSagAtlEnabled;
    private static boolean isVoiceConsentEnabled;
    private static boolean isDataConsentEnabled;
    private static boolean isIntegratedConsentEnabled;
    private static boolean isHourlyConsentEnabled;
    private static boolean isSocialConsentEnabled;
    private static boolean isNonMlConsentEnabled;
    private static boolean isOverallConsentEnabled;
    private static boolean isBlacklistEnabled;
    private static boolean isSubMenuCallEnabled;
    
    static {
        try {
            loadProperties(USSD_CONNECTION_PROPERTIES_FILE);
            ussdIngReqList = Arrays.asList(PropertiesLoader.getValue(USSD_INCOMING_REQ_CODE).split(","));
            displayFlag = PropertiesLoader.getValue(USSD_ACCOUNT_DISP_FLAG).equalsIgnoreCase(TRUE);
            isMpesaEnabled = PropertiesLoader.getValue(USSD_MPESA_ENABLED).equalsIgnoreCase(TRUE);
            isAAEnabled = PropertiesLoader.getValue(USSD_AA_ENABLED).equalsIgnoreCase(TRUE);
            isRagAtlEnabled = PropertiesLoader.getValue(USSD_RAG_ATL_ENABLED).equalsIgnoreCase(TRUE);
            isPEDEnabled = PropertiesLoader.getValue(USSD_PED_ENABLED).equalsIgnoreCase(TRUE);
            String sagEnable = PropertiesLoader.getValue(USSD_SAG_ATL_ENABLED);
            isSagAtlEnabled = !(null != sagEnable  && sagEnable.equalsIgnoreCase(FALSE));          		
            isSocialEnabled = PropertiesLoader.getIntValue(USSD_SOCIAL_ENABLED);
            isGatewayEnabled = PropertiesLoader.getIntValue(USSD_GATEWAY_ENABLED);
            isVoiceConsentEnabled = PropertiesLoader.getValue(VOICE_CONSENT_ENABLED).equalsIgnoreCase(ONE_1);
            isDataConsentEnabled = PropertiesLoader.getValue(DATA_CONSENT_ENABLED).equalsIgnoreCase(ONE_1);
            isIntegratedConsentEnabled = PropertiesLoader.getValue(INTEGRATED_CONSENT_ENABLED).equalsIgnoreCase(ONE_1);
            isHourlyConsentEnabled = PropertiesLoader.getValue(HOURLY_CONSENT_ENABLED).equalsIgnoreCase(ONE_1);
            isSocialConsentEnabled = PropertiesLoader.getValue(SOCIAL_CONSENT_ENABLED).equalsIgnoreCase(ONE_1);
            isNonMlConsentEnabled = PropertiesLoader.getValue(NON_ML_CONSENT_ENABLED).equalsIgnoreCase(ONE_1);
            isOverallConsentEnabled=PropertiesLoader.getValue(OVERALL_CONSENT_ENABLED).equalsIgnoreCase(ONE_1);
            isBlacklistEnabled = PropertiesLoader.getValue(USSD_BLACKLIST_ENABLED).equalsIgnoreCase(ONE_1);          	
            isSubMenuCallEnabled = PropertiesLoader.getValue(SUBMENU_QUERYBALANCE_CALL_ENABLED).equalsIgnoreCase(ONE_1);
            
            mlRefreshFlagMap = loadMlRefreshFlagMap();
            mlSubMenuProdTypeMenu = getProdTypeMap();
            ragAtlRewardCodeTargetMap = getRagAtlRewardMap();
            sagAtlRewardCodeTargetMap = getSagAtlRewardMap();
            icapLangCodeMap = getIcapLangCode();
        } catch (Exception e) {
            LOG.error("Error in loading the USSD Connection Properties File", e);
        }
    }
      
    private static BiPredicate<UserInfo, String> checkMainMenuSegments = (userInfo, messageBody) ->{
    	if(messageBody.equals(HASH) || messageBody.equals(STAR)) {
			return (userInfo.isMorningOfferFlag() && ONE == isSocialEnabled && userInfo.isConsentFlag()) ||
			           (!userInfo.isMorningOfferFlag() && ONE == isSocialEnabled && userInfo.isConsentFlag()) ||
			           (userInfo.isMorningOfferFlag() && ONE == isSocialEnabled && !userInfo.isConsentFlag()) ||
			           (userInfo.isMorningOfferFlag() && ZERO == isSocialEnabled && userInfo.isConsentFlag());
		}		
		return false;
	};
    
    private static Predicate<String> checkJ4UTownMenu = messageBody -> (messageBody.equals(USER_SEL_5));
    
    private static Predicate<String> checkJ4UNewUserValidSelection = messageBody -> messageBody.equals(USER_SEL_1) || messageBody.equals(USER_SEL_2) ||
    		messageBody.equals(USER_SEL_3) || messageBody.equals(USER_SEL_4) || messageBody.equals(USER_SEL_6);

    private static Predicate<UserInfo> checkRagEligibility = userInfo -> (userInfo.isRagUser() || isRagAtlEnabled);
    private static Predicate<UserInfo> checkSagEligibility = userInfo -> (userInfo.isSagUser() || isSagAtlEnabled);
    private static Predicate<UserInfo> checkPEDEligibility = userInfo -> ( userInfo.isPedEligibility() || isPEDEnabled);
    private static Predicate<UserInfo> checkRagDisability = userInfo -> !(userInfo.isRagUser() && isRagAtlEnabled);
    private static Predicate<UserInfo> checkSagDisability = userInfo -> !(userInfo.isSagUser() && isSagAtlEnabled);
    private static Predicate<UserInfo> checkPEDDisability = userInfo -> !(userInfo.isPedEligibility() && isPEDEnabled);

    
    private static Predicate<UserInfo> checkMorningEligibility = userInfo -> userInfo.isMorningOfferFlag() && Utils.checkMorningWindow();
    private static BiPredicate<Integer, String> checkValidSubmenu = (offerCount,messageBody) ->{
    	if(offerCount == MAX_PRODIDS_CNT_1) {
    		return 	USER_SEL_1.equals(messageBody);
    	}else if(offerCount == MAX_PRODIDS_CNT_2) {
    		return	(USER_SEL_1.equals(messageBody) || USER_SEL_2.equals(messageBody));
    	}else if(offerCount == MAX_PRODIDS_CNT_3) {
    		return	(USER_SEL_1.equals(messageBody) || USER_SEL_2.equals(messageBody) || USER_SEL_3.equals(messageBody));
    	}else if(offerCount == FOUR) {
    		return	(USER_SEL_1.equals(messageBody) || USER_SEL_2.equals(messageBody) 
    				|| USER_SEL_3.equals(messageBody) || USER_SEL_4.equals(messageBody));
    	}else if(offerCount == FIVE) {
    		return	(USER_SEL_1.equals(messageBody) || USER_SEL_2.equals(messageBody) 
    				|| USER_SEL_3.equals(messageBody) || USER_SEL_4.equals(messageBody) 
    				|| USER_SEL_5.equals(messageBody));
    	}
    	return false;
    };

    private static Predicate<String> checkConsentMenuOptionsSocial = messageBody ->
	    (isVoiceConsentEnabled && messageBody.equals(USER_SEL_1)) ||
	    (isDataConsentEnabled && messageBody.equals(USER_SEL_2)) ||
	    (isIntegratedConsentEnabled && messageBody.equals(USER_SEL_3)) ||
	    (isHourlyConsentEnabled && messageBody.equals(USER_SEL_4)) ||
	    (isSocialConsentEnabled && messageBody.equals(USER_SEL_6));

    private static Predicate<String> checkConsentMenuOptionsNoSocial = messageBody ->
	    (isVoiceConsentEnabled && messageBody.equals(USER_SEL_1)) ||
	    (isDataConsentEnabled && messageBody.equals(USER_SEL_2)) ||
	    (isIntegratedConsentEnabled && messageBody.equals(USER_SEL_3)) ||
	    (isHourlyConsentEnabled && messageBody.equals(USER_SEL_4));


    private static BiPredicate<UserInfo,String> checkMorningMenu = (userInfo,messageBody) ->(userInfo.isMorningOfferFlag() && messageBody.equals(USER_SEL_0));
    private static BiPredicate<UserInfo,String> checkNewCustMorningMenuNotEligible = (userInfo,messageBody) ->(!userInfo.isMorningOfferFlag() && userInfo.isJ4uNewUser() && messageBody.equals(USER_SEL_0));
    public static Predicate<String> checkSocialMenu = messageBody -> (ONE == isSocialEnabled && messageBody.equals(USER_SEL_6));
    private static BiPredicate<UserInfo, String> consentOptOut = (userInfo,messageBody) ->{
    	if(ONE == isSocialEnabled) {
    		return	userInfo.isConsentFlag() && messageBody.equals(USER_SEL_8);	
    	}else {
    		return userInfo.isConsentFlag() && messageBody.equals(USER_SEL_7);
    	}
    };
    private static BiPredicate<UserInfo, String> nonMLConsentOptOut = (userInfo,messageBody) -> userInfo.isConsentFlag() && messageBody.equals(USER_SEL_4);
    private static Predicate<String> checkMyRewardsMenu = menu -> (ONE == isSocialEnabled)?menu.equals(USER_SEL_7):menu.equals(USER_SEL_6);
    private static BiPredicate<String, UserInfo> checkRAGMenuSelection = (menu, userInfo) -> checkRagEligibility.test(userInfo) && menu.equals(USER_SEL_1);
    private static BiPredicate<String, UserInfo> checkPEDMenuSelection = (menu, userInfo) -> {
    	if(checkPEDEligibility.test(userInfo)) {
    		if (checkRagEligibility.test(userInfo)) {
    			return menu.equals(USER_SEL_2) ; 
    		}else {
    			return menu.equals(USER_SEL_1);
    		}
    	}
    	return false;
    };    
    private static BiPredicate<String, UserInfo> checkSAGMenuSelection = (menu, userInfo) -> { 
    	if(checkSagEligibility.test(userInfo)) {
    		if(checkRagEligibility.test(userInfo) && checkPEDEligibility.test(userInfo) ){
    			return USER_SEL_3.equals(menu);
    		}else if (checkRagEligibility.test(userInfo) || checkPEDEligibility.test(userInfo)){
    			return USER_SEL_2.equals(menu);
    		}else {
    			return USER_SEL_1.equals(menu);
    		}
    	}
    	return false;
    };
    
    
    
    private static Predicate<String> checkSagSubMenuSelection = messageBody -> (USER_SEL_1.equals(messageBody) || USER_SEL_2.equals(messageBody));


    private static Predicate<UserInfo> checkMyRewardsAllmenu = myRewardsUserInfo ->(checkRagEligibility.test(myRewardsUserInfo) 
    		&& checkPEDEligibility.test(myRewardsUserInfo)  && checkSagEligibility.test(myRewardsUserInfo) );
    private static Predicate<UserInfo> checkMyRewardsRPmenu = myRewardsUserInfo ->(checkRagEligibility.test(myRewardsUserInfo) 
    		&& checkPEDEligibility.test(myRewardsUserInfo)  && checkSagDisability.test(myRewardsUserInfo) ); 
    private static Predicate<UserInfo> checkMyRewardsRSmenu = myRewardsUserInfo ->(checkRagEligibility.test(myRewardsUserInfo) 
    		&& checkPEDDisability.test(myRewardsUserInfo)  && checkSagEligibility.test(myRewardsUserInfo) );    
    private static Predicate<UserInfo> checkMyRewardsPSmenu = myRewardsUserInfo ->(checkRagDisability.test(myRewardsUserInfo) 
    		&& checkPEDEligibility.test(myRewardsUserInfo)  && checkSagEligibility.test(myRewardsUserInfo) );		
    private static Predicate<UserInfo> checkMyRewardsRmenu = myRewardsUserInfo ->(checkRagEligibility.test(myRewardsUserInfo) 
    		&& checkPEDDisability.test(myRewardsUserInfo)  &&  checkSagDisability.test(myRewardsUserInfo)  );		
    private static Predicate<UserInfo> checkMyRewardsPmenu = myRewardsUserInfo ->(checkRagDisability.test(myRewardsUserInfo) 
    		&& checkPEDEligibility.test(myRewardsUserInfo)  &&  checkSagDisability.test(myRewardsUserInfo) );		
    private static Predicate<UserInfo> checkMyRewardsSmenu = myRewardsUserInfo ->(checkRagDisability.test(myRewardsUserInfo)
    		&& checkPEDDisability.test(myRewardsUserInfo)  && checkSagEligibility.test(myRewardsUserInfo) );


   
    private final AtomicLong lastActivityTime = new AtomicLong(System.currentTimeMillis());
    private final LinkedHashMap<Integer, TransmitMessage> transmitMessageCache;
    private final SequenceNumberScheme sequenceNumberScheme;
    private final TransmitDelay transmitDelay;
    private final MessageTransmitter transmitter;
    private int retryPollCycleMilliSec = 0;
    private int connectionStatus;
    private boolean connectionReset = false;
    private Connection ussdConnection;
    private ReentrantLock reentrantLock;
    private Condition isConnected;
    private MessageListenerImpl messageListenerImpl;
    private ScheduledExecutorService keepAliveExecutorService;
    private ScheduledExecutorService messageTransmitterExecutorService;
    private ScheduledExecutorService removeInactiveSessionsService;
    private ScheduledExecutorService retryExcService;
    private Map<Integer, UserInfo> menuLevelOne = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> menuLevelTwo = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> myRewardsMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> ragMainMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> ragSubMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> sagMainMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> sagSubMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> paymentMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> currencyMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> loanMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> pedPlayMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> pedSubMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> pedViewPlayMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> pedHistoryMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> pedNoHistoryMenu = new ConcurrentHashMap<>();
    private Map<Integer, UserInfo> pedNoPrizeMenu = new ConcurrentHashMap<>();   
	private Map<Integer, UserInfo> consentMenu = new ConcurrentHashMap<>();
	private Map<Integer, UserInfo> consentOptOutMenu = new ConcurrentHashMap<>();
	private Map<Integer, String> consentMenuBody= new ConcurrentHashMap<>();
	private Map<Integer, UserInfo> userInfoMap = new ConcurrentHashMap<>();
    private ExecutorService executor;
    private ExecutorService smExecutor;
    private SecureRandom random;
  
    public UssdConnectionFactory() throws Exception {

        reentrantLock = new ReentrantLock();
        isConnected = reentrantLock.newCondition();

        transmitMessageBuffer = new ConcurrentLinkedDeque<>();
        sequenceNumberScheme = new UssdSequenceNumberScheme(index);
        transmitMessageCache = new LinkedHashMap<Integer, TransmitMessage>(bufferSize) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, TransmitMessage> eldest) {
                return size() > bufferSize;
            }
        };
        transmitDelay = new TransmitDelay(maxMessagesPerSecond);
        transmitter = new MessageTransmitter();
        retryPollCycleMilliSec = retryPollCycleSeconds * 1000;
        lookUpDAO = new LookUpDAO();
        updaterDao = new UpdaterDAO();
        querySubscriberProfile = new QuerySubscriberProfile();
        random = new SecureRandom();
        LOG.info("UssdConnectionFactory=> constructor initialization end!!!");
    }

    private static Map<String, String> loadMlRefreshFlagMap() throws Exception {
    	Map<String, String> mlRefreshFlagMap = new HashMap<>();
    	try {
    		List<String> offerRefreshFlagList = Arrays.asList(PropertiesLoader.getValue(USSD_OFFER_REFRESH_FLAG_MAP).split(","));
            for (String key : offerRefreshFlagList) {
                String[] mapAry = key.split(":");
                mlRefreshFlagMap.put(mapAry[ZERO], mapAry[ONE]);
            }
        } catch (Exception e) {
            LOG.error("Error occured at offerRefreshFlagMap ==> ", e);
        }
    	
        return mlRefreshFlagMap;
    }

    private static Map<String, String> getProdTypeMap() {
        List<String> prodTypeList;
        Map<String, String> mlSubMenuProdTypeMenu = new HashMap<>();
        try {
            prodTypeList = Arrays.asList(PropertiesLoader.getValue(USSD_SUB_MENU_PROD_TYPE).split(","));
            for (String key : prodTypeList) {
                String[] prodMapping = key.split(":");
                mlSubMenuProdTypeMenu.put(prodMapping[ZERO], prodMapping[ONE]);
            }
        } catch (Exception e) {
            LOG.error("Error occured at prodTypeMap ==> ", e);
        }

        return mlSubMenuProdTypeMenu;
    }
    
    private static Map<String, String> getIcapLangCode() {
        List<String> langCodeList;
        Map<String, String> icapLangCodeMap = new HashMap<>();
        try {
        	langCodeList = Arrays.asList(PropertiesLoader.getValue(USSD_ICAP_LANG_CODE_MAP).split(","));
            for (String key : langCodeList) {
                String[] langCodeMapping = key.split(":");
                icapLangCodeMap.put(langCodeMapping[ONE], langCodeMapping[ZERO]);
            }
        } catch (Exception e) {
            LOG.error("Error occured at icapLangCodeMap ==> ", e);
        }

        return icapLangCodeMap;
    }

    private static Map<String, String> getRagAtlRewardMap() {
        List<String> rewardList;
        Map<String, String> ragAtlRewardCodeTargetMap = new HashMap<>();
        try {
            rewardList = Arrays.asList(PropertiesLoader.getValue(USSD_RAG_ATL_REWARD_MAP).split(","));
            for (String key : rewardList) {
                String[] rewardMapping = key.split(":");
                ragAtlRewardCodeTargetMap.put(rewardMapping[ZERO], rewardMapping[ONE]);
            }
        } catch (Exception e) {
            LOG.error(ERROR_OCCURED, e);
        }
        return ragAtlRewardCodeTargetMap;
    }
    
    private static Map<String, String> getSagAtlRewardMap() {
        List<String> rewardList;
        Map<String, String> sagAtlRewardCodeTargetMap = new HashMap<>();
        try {
            rewardList = Arrays.asList(PropertiesLoader.getValue(USSD_SAG_ATL_REWARD_MAP).split(","));
            for (String key : rewardList) {
                String[] rewardMapping = key.split(":");
                sagAtlRewardCodeTargetMap.put(rewardMapping[ZERO], rewardMapping[ONE]);
            }
        } catch (Exception e) {
            LOG.error(ERROR_OCCURED, e);
        }
        return sagAtlRewardCodeTargetMap;
    }

    /*
     * Load Properties File and set the values
     */
    private static void loadProperties(String fileName) {
    	try(FileInputStream fileInputStream = new FileInputStream(fileName)) {
    		properties.load(fileInputStream);
		    ussdHostName = properties.getProperty("ussd.hostname");
		    ussdPort = Integer.parseInt(properties.getProperty("ussd.port"));
		    ussdSystemId = properties.getProperty("ussd.systemId");
		    ussdSystemPassword = properties.getProperty("ussd.systemPassword");
		    ussdSystemType = properties.getProperty("ussd.systemType");
		    esmeTON = Integer.parseInt(properties.getProperty("ussd.esmeTON"));
		    esmeNPI = Integer.parseInt(properties.getProperty("ussd.esmeNPI"));
		    esmeAddressRangeRegex = properties.getProperty("ussd.esmeAddressRangeRegex");
		    keepAliveCycleInSeconds = Integer.parseInt(properties.getProperty("ussd.keepAliveCycleInSeconds"));
		    bufferSize = Integer.parseInt(properties.getProperty("ussd.queueBufferSize"));
		    retryPollCycleSeconds = Integer.parseInt(properties.getProperty("ussd.retryPollCycleSeconds"));
		    retryCount = Integer.parseInt(properties.getProperty("ussd.num.retries"));
		} catch (Exception e) {
			LOG.error("Error in loading the USSD Connection Properties File", e);
		}	    
    }

    /*
     * Bind the Connection to USSD Gateway
     */
    public void bind() throws Exception {
        String fullIdentifier = "(" + esmeAddressRangeRegex + ")" + ussdSystemId + "@" + ussdHostName + ":" + ussdPort;
        try {
            LOG.info("Defining new Tags");
            if (ONE == isGatewayEnabled) {
                if (!Tag.isTagDefined(LOCATION_NUMBER)) {
                    Tag.defineTag(LOCATION_NUMBER, byte[].class, null, ONE, 30);
                }
                if (!Tag.isTagDefined(CELL_ID)) {
                    Tag.defineTag(CELL_ID, byte[].class, null, ONE, 20);
                }
            } else {
                if (!Tag.isTagDefined(LOCATION_NUMBER)) {
                    Tag.defineTag(LOCATION_NUMBER, Number.class, null, ONE, 30);
                }
                if (!Tag.isTagDefined(CELL_ID)) {
                    Tag.defineTag(CELL_ID, Number.class, null, ONE, 20);
                }
            }
            ussdConnection = new Connection(ussdHostName, ussdPort, IS_ASYNC);
            Field myField = Class.forName("ie.omk.smpp.Connection").getDeclaredField("supportOptionalParams");
            myField.setAccessible(true);
            myField.set(ussdConnection, true);

        } catch (UnknownHostException | ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
        	LOG.error("Exception in binding :: ", e);
            throw new UssdConnectionException("Unable to connect to the USSD Gateway as " + fullIdentifier);
        }

        String logDetail = displayFlag ? ("Connected to USSD Gateway as " + fullIdentifier) : "Connected to USSD Gateway";
        LOG.info(logDetail);
        ussdConnection.addObserver(new ReceiverObserver());

        LOG.info("Binding to USSD for sending messages");
        @SuppressWarnings("unused")
        boolean isTimedOut = false;
        reentrantLock.lock();
        try {
            LOG.info("Binding to USSD GW for Transmitting Messages");
            ussdConnection.bind(Connection.TRANSCEIVER, ussdSystemId, ussdSystemPassword, ussdSystemType, esmeTON, esmeNPI, esmeAddressRangeRegex);
            LOG.info("Bound to USSD GW for Transmitting Messages");
            isTimedOut = !(isConnected.await(CONNECTION_TIMEOUT, TimeUnit.SECONDS));
            this.setConnectionStatus(ussdConnection.getState());
            this.setMessageListener(new MessageListenerImpl());
        } catch (InterruptedException e) {
            LOG.error(ERROR_OCCURED, e);
            throw e;
        } catch (Exception e) {
            throw new UssdConnectionException("Unable to bind the connection to USSD GW as " + fullIdentifier + " with error " + e.getMessage(), e);
        } finally {
            reentrantLock.unlock();
        }

        keepAliveExecutorService = Executors.newSingleThreadScheduledExecutor();
        messageTransmitterExecutorService = Executors.newSingleThreadScheduledExecutor();
        removeInactiveSessionsService = Executors.newSingleThreadScheduledExecutor();
        executor = Executors.newFixedThreadPool(PropertiesLoader.getIntValue(USSD_DELIVERSM_THREADCOUNT));
        smExecutor = Executors.newFixedThreadPool(PropertiesLoader.getIntValue(USSD_SUBMITSM_THREADCOUNT));
        // Thread to transmit the messages every 1millisecond
        messageTransmitterExecutorService.scheduleWithFixedDelay(transmitter, ONE, ONE, TimeUnit.MILLISECONDS);
        removeInactiveSessionsService.submit(new TimeoutHandler());
        isClearActiveSession = true;
        
        // Keep Alive Worker Thread
        if (keepAliveCycleInSeconds != ZERO) {
            keepAliveExecutorService.scheduleWithFixedDelay(new KeepConnectionAlive(ussdConnection), keepAliveCycleInSeconds, keepAliveCycleInSeconds, TimeUnit.SECONDS);
        }
        if (null == retryExcService || retryExcService.isShutdown()) {
            retryExcService = Executors.newScheduledThreadPool(5);
            retryExcService.scheduleWithFixedDelay(new RetryConnection(), retryPollCycleSeconds, retryPollCycleSeconds, TimeUnit.SECONDS);
        }

        LOG.info("USSD Binding Completed!!!!! ");

        if (connectionReset) {
            connectionReset = false;
            LOG.debug("Setting connectionReset flag => " + connectionReset);
        }
    }

    /*
     * Unbind the connection to USSD gateway
     */
    public void unbind() throws UssdConnectionException {
        if (null == ussdConnection) {
            return;
        }
        keepAliveExecutorService.shutdown();
        messageTransmitterExecutorService.shutdown();
        removeInactiveSessionsService.shutdown();
        // Retry connection issue fix.
        /*
         * if (!connectionReset) {
         * executor.shutdown();
         * }
         */
        isClearActiveSession = false;
        activeSessions.clear();

        try {
            keepAliveExecutorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("Exception in making the Keep Alive Thread to wait", e);
            Thread.currentThread().interrupt();
        }

        if (ussdConnection.isBound()) {
            LOG.info("Sending Unbind request to USSD Gateway");
            boolean isTimedOut = false;
            reentrantLock.lock();
            try {
                ussdConnection.unbind();
                LOG.info("Connectiong to USSD Gateway is unbound");
                isTimedOut = !(isConnected.await(CONNECTION_TIMEOUT, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                LOG.error("Thread interrupted while waiting for connection", e);
                // Restore interrupted status
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOG.error("Error unbinding the connection to USSD Gateway", e);
            } finally {
                reentrantLock.unlock();
            }
            LOG.info("Closing the Network Link to USSD Gateway");
            try {
                ussdConnection.closeLink();
            } catch (Exception e) {
                throw new UssdConnectionException("Unable to close the network link to USSD Gateway", e);
            } finally {
                ussdConnection = null;
            }

            LOG.info("Closed the network link to USSD Gateway");

            if (isTimedOut) {
                throw new UssdConnectionException("Unable to receive the Unbind Response");
            }
        }
    }

    /*
     * Method used to set the message parameters
     */
    public void sendMessage(TransmitMessage message) {
    	addToSendList(message);
    }

    public void sendMessage(UserInfo userInfo) throws Exception {
        try {
            LOG.debug("Into UserInfo Send Message Method");
            TransmitMessage message = new TransmitMessage();
            message.setSourceAddress(userInfo.getDestAddress());
            message.setDestinationAddress(userInfo.getMsisdn());
            message.setMessageText(userInfo.getMessageBody());
            message.setTransactionId(userInfo.getTxId());
            message.setMessageEncoding(ONE);
            message.setCellId(userInfo.getCellId());
            message.setPoolId(userInfo.getPoolId());
            message.setTownName(userInfo.getTownName());
            message.setLstarttime(userInfo.getlStartTime());
            if (userInfo.getServiceOp() != TWO) {
                message.setServiceOp(SEVENTEEN);
                // status
                message.setLogStatus(STATUS_FINAL_RESPONSE_SENT);
            } else {
                message.setServiceOp(TWO);
                // status
                message.setLogStatus(SUBMENU_RESPONSE_SENT);
            }
            // setting 2 for ussd log table
            message.setUssdLogStatus(TWO); 
            LOG.debug("Checking product Id=> " + userInfo.getProdIds());
            message.setReferenceNumber(userInfo.getUserMsgRef());
            message.setProdIds(userInfo.getProdIds());
            message.setMlFlag(userInfo.isMlFlag());
            LOG.debug("userInfo.getAirtimeAdvBal() - " + userInfo.getAirtimeAdvBal());
            boolean loanCheckFlag = false;
            if ((userInfo.getAaEligible() == ONE) && isAAEnabled) {
                loanCheckFlag = true;
            }
            LOG.debug("loanCheckFlag 1 - " + loanCheckFlag);
            if (loanCheckFlag) {

                AAMap aaMap = AAMap.instance();
                UserInfo aaMapUserInfo = aaMap.get(userInfo.getUserMsgRef());
                LOG.debug("aaMapUserInfo - " + aaMapUserInfo);

                aaMapUserInfo.setAaEligibleProdIdProdPriceMap(userInfo.getAaEligibleProdIdProdPriceMap());
                aaMapUserInfo.setAirtimeAdvBal(userInfo.getAirtimeAdvBal());
                aaMapUserInfo.setActBal(userInfo.getActBal());
                LOG.debug("UserMsgRef() - " + userInfo.getUserMsgRef());
                LOG.debug("AirtimeAdvBal() - " + userInfo.getAirtimeAdvBal());
                LOG.debug("ActBal() - " + userInfo.getActBal());
                aaMap.put(userInfo.getUserMsgRef(), aaMapUserInfo);
            }
            addToSendList(message);

            if (userInfo.isUpdateCCR()) {
                LOG.debug("Update Reduced CCR || MSISDN => " + userInfo.getMsisdn() + " | Prod Type => " 
                		+ mlRefreshFlagMap.get(userInfo.getSelProdType()) + " | Offer Refresh Flag => " + userInfo.getOfferRefreshFlag());
                updateReducedCCR(userInfo.getMsisdn(), mlRefreshFlagMap.get(userInfo.getSelProdType()), userInfo.getOfferRefreshFlag());
            }
        } catch (Exception e) {
            LOG.error("Exception inside sendMessage: " + e);
        }

    }

    private void updateReducedCCR(String subscriberId, String prodType, String offerRefFlag) throws Exception {

        if (offerRefFlag.indexOf(prodType) >= ZERO) {
            /*
             * if reward provision is not success, need to remove the
             * data/voice/integrated
             * from OFFER_REFRESH_FLAG column
             */
            if (offerRefFlag.equalsIgnoreCase(prodType)) {
                offerRefFlag = VALUE_N;
            } else {
                offerRefFlag = offerRefFlag.replace(prodType, "");
            }
            LOG.debug("Updating ERED_T_REDUCED_CCR [OFFER_REFRESH_FLAG] = " + offerRefFlag + " for MSISDN = " + subscriberId);
            updaterDao.updateReducedCCR(offerRefFlag, subscriberId);
        }
    }

    /*
     * Method to add all the list of messages that needs to be sent into a
     * Linked
     * Blocking Queue
     */
    private void addToSendList(TransmitMessage message) {
        try {
        	message.setMessageText(message.getMessageText().replace("\r", ""));
            transmitMessageBuffer.addLast(message);
            LOG.info("Added to the queue --- MSISDN|TRANSACTION_ID - " + message.getDestinationAddress() + "|" + message.getTransactionId());
        } catch (Exception e) {
            LOG.error("Exception inside addToSendList: " + e);
        }
    }

    private boolean verifyToRestart() {
        boolean restart = false;
        if ((EnquiryLinkTime.getEnquireLinkRequestTime().get() > EnquiryLinkTime.getEnquireLinkResponseRecievedTime().get()) && 
        		((EnquiryLinkTime.getEnquireLinkRequestTime().get() - EnquiryLinkTime.getEnquireLinkResponseRecievedTime().get()) > retryPollCycleMilliSec)) {
            restart = true;
            LOG.debug("Retry Just For You USSD Process :: RESTART => " + restart);
        } else {
            restart = false;
            LOG.debug("Retry Just For You USSD Process :: RESTART => " + restart);
        }
        return restart;
    }

    public void shutdown() {
        try {
            clearActiveSession();
            if (null != messageTransmitterExecutorService) {
                messageTransmitterExecutorService.shutdown();
            }
            if (null != removeInactiveSessionsService) {
                removeInactiveSessionsService.shutdown();
            }
            if (null != retryExcService) {
                retryExcService.shutdownNow();
            }
            if (null != executor) {
                executor.shutdown();
            }
            if (null != smExecutor) {
                smExecutor.shutdown();
            }
            if (null != keepAliveExecutorService) {
                keepAliveExecutorService.shutdown();
                keepAliveExecutorService.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            LOG.error("Exception in making the Keep Alive Thread to wait", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.error("Exception while shutting down the keep alivers", e);
        } finally {
            isClearActiveSession = false;
            if (null != activeSessions) {
                activeSessions.clear();
            }
        }
    }

    private void clearActiveSession() {
        messageListenerImpl.cleanUp();
    }

    public int getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(int connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public void setMessageListener(MessageListenerImpl messageListenerImpl) {
        this.messageListenerImpl = messageListenerImpl;
    }

    public void insertRagUserRecord(UserInfo ragMainMenuInfo) {
        LOG.debug("insertRagUserRecord - START");
        Map<String, String> ragUserRecord = null;
        try {
            String msisdn = ragMainMenuInfo.getMsisdn();
            ragUserRecord = getRagUserRecord(ragMainMenuInfo, ragAtlRewardCodeTargetMap);
            LOG.debug("calling calculateWeekStartAndEndDate =>");
            Map<String, String> weekDateMap = this.calculateWeekStartAndEndDate();
            ragUserRecord.put(WEEK_START_DATE, weekDateMap.get("weeksStartDate"));
            ragUserRecord.put(WEEK_END_DATE, weekDateMap.get("weeksEndDate"));
            ragUserRecord.put(NEXT_AVAILABLE_OFFER_DATE, weekDateMap.get("nextAvailableweek"));

            updaterDao.insertRagUserRecordDB(ragUserRecord);
            updaterDao.insertRagUserCatFile(ragUserRecord, msisdn);
        } catch (Exception e) {
            LOG.error("Exception while insertRagUserRecord - ", e);
        }

    }
    
    public void insertSagUserRecord(UserInfo sagMainMenuInfo) {
        LOG.debug("insertSagUserRecord - START");
        Map<String, String> sagUserRecord = null;
        try {
            String msisdn = sagMainMenuInfo.getMsisdn();
            sagUserRecord = getSagUserRecord(sagMainMenuInfo, sagAtlRewardCodeTargetMap);
            LOG.debug("calling calculateWeekStartAndEndDate =>");
            Map<String, String> weekDateMap = this.calculateWeekStartAndEndDate();
            sagUserRecord.put(WEEK_START_DATE, weekDateMap.get("weeksStartDate"));
            sagUserRecord.put(WEEK_END_DATE, weekDateMap.get("weeksEndDate"));
            sagUserRecord.put(NEXT_AVAILABLE_OFFER_DATE, weekDateMap.get("nextAvailableweek"));

            updaterDao.insertSagUserRecordDB(sagUserRecord);
            updaterDao.insertSagUserCatFile(sagUserRecord, msisdn);
        } catch (Exception e) {
            LOG.error("Exception while insertSagUserRecord - ", e);
        }

    }

    public Map<String, String> calculateWeekStartAndEndDate() throws ParseException {
        LOG.debug("calculateWeekStartAndEndDate START =>");
        Map<String, String> weekDateMap = new HashMap<>();
        // Get calendar set to current date and time
        Calendar calendar = Calendar.getInstance();

        // Set the calendar to WEDNESDAY of the current week
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);

        // Print dates of the current week starting on WEDNESDAY
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        Date currentdate = new Date();
        Date weekStartingDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
        String weeksStartDate = "";
        String weeksEndDate = "";
        String nextAvailableweek = "";
        weeksStartDate = dateFormat.format(weekStartingDate);

        LOG.debug("current week Start Date = " + weeksStartDate);

        if (currentdate.before(weekStartingDate)) {
            nextAvailableweek = weeksStartDate;
            calendar.add(Calendar.DATE, -7);
            weeksStartDate = dateFormat.format(calendar.getTime());
            calendar.add(Calendar.DATE, 6);
            weeksEndDate = dateFormat.format(calendar.getTime());
        } else {
            calendar.add(Calendar.DATE, 6);
            weeksEndDate = dateFormat.format(calendar.getTime());
            calendar.add(Calendar.DATE, 7);
            nextAvailableweek = dateFormat.format(calendar.getTime());
        }

        LOG.debug("Week Start Date = " + weeksStartDate);
        LOG.debug("Week End Date = " + weeksEndDate);
        LOG.debug("nextAvailableweek Date = " + nextAvailableweek);
        weekDateMap.put("weeksStartDate", weeksStartDate);
        weekDateMap.put("weeksEndDate", weeksEndDate);
        weekDateMap.put("nextAvailableweek", nextAvailableweek);
        LOG.debug("calculateWeekStartAndEndDate method END!");

        return weekDateMap;

    }

    public Map<String, String> getRagUserRecord(UserInfo ragMainMenuInfo, Map<String, String> ragAtlRewardCodeTargetMap) throws Exception {
        HashMap<String, String> ragUserRecord = null;
        String msisdn = ragMainMenuInfo.getMsisdn();
        if (msisdn.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
            msisdn = msisdn.replaceFirst(PropertiesLoader.getValue(SUBID_CNTRYCD), "");
        }
        RAGDefaultsCache ragDefaultsCache = RAGDefaultsCache.instance() ;
        ragUserRecord = new HashMap<>();
        Object[] rewardKeys = ragAtlRewardCodeTargetMap.keySet().toArray();
        Object rewardKey = null;
        try {
            rewardKey = rewardKeys[random.nextInt(rewardKeys.length)];
        } catch (Exception e) {
        	LOG.error(ERROR_OCCURED, e);
            rewardKey = rewardKeys[ZERO];
        }
        ragUserRecord.put(MSISDN, msisdn);
        ragUserRecord.put(WEEK_START_DATE, ragDefaultsCache.getWeekStartDate());
        ragUserRecord.put(WEEK_END_DATE, ragDefaultsCache.getWeekEndDate());
        ragUserRecord.put(NEXT_AVAILABLE_OFFER_DATE, ragDefaultsCache.getNextOfferAvailableDate());
        ragUserRecord.put(REWARD_CODE, rewardKey.toString());
        ragUserRecord.put(TARGET_TYPE, OTHER);
        ragUserRecord.put(PAYMENT_METHOD, VALUE_P);
        ragUserRecord.put(RECHARGE_TARGET, ragAtlRewardCodeTargetMap.get(rewardKey));

        return ragUserRecord;
    }
    
    public Map<String, String> getSagUserRecord(UserInfo sagMainMenuInfo, Map<String, String> sagAtlRewardCodeTargetMap) throws Exception {
        HashMap<String, String> sagUserRecord = null;
        String msisdn = sagMainMenuInfo.getMsisdn();
        if (msisdn.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
            msisdn = msisdn.replaceFirst(PropertiesLoader.getValue(SUBID_CNTRYCD), "");
        }
        SAGDefaultsCache sagDefaultsCache = SAGDefaultsCache.instance() ;
        sagUserRecord = new HashMap<>();
        Object[] rewardKeys = sagAtlRewardCodeTargetMap.keySet().toArray();
        Object rewardKey = null;
        try {
            rewardKey = rewardKeys[random.nextInt(rewardKeys.length)];
        } catch (Exception e) {
        	LOG.error(ERROR_OCCURED, e);
            rewardKey = rewardKeys[ZERO];
        }
        sagUserRecord.put(MSISDN, msisdn);
        sagUserRecord.put(WEEK_START_DATE, sagDefaultsCache.getWeekStartDate());
        sagUserRecord.put(WEEK_END_DATE, sagDefaultsCache.getWeekEndDate());
        sagUserRecord.put(NEXT_AVAILABLE_OFFER_DATE, sagDefaultsCache.getNextOfferAvailableDate());
        sagUserRecord.put(REWARD_CODE, rewardKey.toString());        
        sagUserRecord.put(PAYMENT_METHOD, VALUE_P);
        sagUserRecord.put(SPEND_TARGET, sagAtlRewardCodeTargetMap.get(rewardKey));

        return sagUserRecord;
    }


    /*
     * Inner Class Message Transmitter which is used to Transmit the messages to
     * the User
     */
    private class MessageTransmitter implements Runnable {

        private LinkedList<TransmitMessage> messageSegments = new LinkedList<>();

        @Override
        public void run() {
            try {
                while (hasMessages()) {
                   	TransmitMessage msg = messageSegments.peek() ;
                    submitSM(msg);
                    messageSegments.removeFirst();
                }
            } catch (Exception e) {
                LOG.error("Error = " + e.getMessage() + "::", e);
            }
        }
        
 
        /*
         * Method which verifies whether there are messages to send or not
         */
        private boolean hasMessages() throws Exception {
            if (hasMessagesInBuffer()) {
                return true;
            }
            TransmitMessage message = null;
            message = transmitMessageBuffer.pollFirst();

            if (null == message) {
                return false;
            }
            LOG.debug("Poll from the Queue - TransactionID - " + message.getTransactionId());

            if (isSplitMessage(message)) {
                messageSegments.add(message);
                return true;
            }

            List<String> segments = MessageUtils.splitMessageText(message.getMessageText(), message.getMessageEncoding(), "#BREAK#", ONE_HUNDRED_SIXTY);
            int size = segments.size();
            if (size == ZERO) {
                segments.add("");
                size = ONE;
            }
            long messageKey = MessageId.nextMessageId(size);
            for (int i = 0; i < size; i++) {
                TransmitMessage messageSegment = new TransmitMessage(message);
                messageSegment.setMessageKey(messageKey + i);
                messageSegment.setMessageText(segments.get(i));
                messageSegment.setPartNumber(i + ONE);
                messageSegment.setPartsTotal(size);
                messageSegment.setPartsReference((int) (messageKey & 0xFFFF));
                messageSegment.setTransactionId(message.getTransactionId());
                messageSegments.add(messageSegment);
            }
            return true;
        }

        private boolean hasMessagesInBuffer() {
            return !messageSegments.isEmpty();
        }

        private boolean isSplitMessage(TransmitMessage message) {
            return (null != message.getMessageKey());
        }
        
        public void submitSM(TransmitMessage message) {
            smExecutor.submit(() -> {
                try {
                    sendMessage(message);
                } catch (InterruptedException e) {
                    // Restore interrupted status
                    Thread.currentThread().interrupt();
                    LOG.error("Thread interrupted while sending message", e);
                } catch (Exception ex) {
                    LOG.error("submitSM Exception - ", ex);
                }
            });
        }
        
        /*
         * Method to Transmit Message via USSD Gateway
         */
        private void sendMessage(TransmitMessage message) throws InterruptedException, 
		        AlreadyBoundException, VersionException, SMPPProtocolException, 
		        UnsupportedOperationException, IOException {
            if (isDestinationAddressInvalid(message) || isTextMessageBlank(message)) {
                return;
            }
            long lstarttime = message.getLstarttime();
            int sequenceNumber = sequenceNumberScheme.nextNumber();
            SubmitSM submitMessage = null;

            try {
                submitMessage = createSubmitMessage(message);
                long interval = transmitDelay.pause(message.getRetryCount() + ONE);
                lastActivityTime.set(System.currentTimeMillis());
                if (null != submitMessage) {
                    submitMessage.setSequenceNum(sequenceNumber);
                    LOG.info("SubmitSM: " + getMessageAsString(submitMessage, message, interval));
                }
            } catch (final InvalidParameterValueException e) {
                String errorMessage = "Invalid parameter in communication to send to destination: " + 
                		message.getDestinationAddress() + " and message key: " + message.getMessageKey();
                LOG.error(errorMessage, e);
                return;
            }

            reentrantLock.lock();
            try {
                ussdConnection.sendRequest(submitMessage);
            } finally {
                reentrantLock.unlock();
            }
            long lendtime = System.currentTimeMillis();
            message.setlTimeTaken(lendtime - lstarttime);
            messageListenerImpl.logInfo(message.getReferenceNumber(), message);
            transmitMessageCache.put(sequenceNumber, message);
        }

        /*
         * Method to check whether Destination Address to send message is valid
         * or not
         */
        private boolean isDestinationAddressInvalid(TransmitMessage message) {
            if (null != message.getDestinationAddress() && message.getDestinationAddress().trim().length() > ZERO) {
                return false;
            }
            LOG.error("The message has no destination address for Message Key: " + message.getMessageKey());
            LOG.debug(message);

            return true;
        }

        /*
         * Is Message blank to send
         */
        private boolean isTextMessageBlank(TransmitMessage message) {
        	if (null != message.getMessageText() && message.getMessageText().trim().length() > ZERO) {
        		return false;
        	}
        	LOG.warn("No message text to send to destination: " + message.getDestinationAddress() + " and message key: " + message.getMessageKey());
        	LOG.debug(message);
        	return true;
        }

        /*
         * Create a Submit SM Message
         */
        private SubmitSM createSubmitMessage(TransmitMessage message) {
            SubmitSM result = null;

            final Address source = new Address(message.getSourceTON(), message.getSourceNPI(), null != message.getSourceAddress() ? message.getSourceAddress().trim() : null);

            final Address destination = new Address(message.getDestinationTON(), message.getDestinationNPI(), message.getDestinationAddress().trim());

            try {
                result = (SubmitSM) ussdConnection.newInstance(SMPPPacket.SUBMIT_SM);
                if (null != source.getAddress()) {
                    result.setSource(source);
                }
                result.setDestination(destination);

                result.setMessageEncoding(EncodingFactory.getInstance().getEncoding(message.getMessageEncoding()));
                result.setOptionalParameter(Tag.USER_MESSAGE_REFERENCE, new AtomicInteger((int) (message.getReferenceNumber() & 0xFFFF)));
                result.setOptionalParameter(Tag.USSD_SERVICE_OP, (short) (message.getServiceOp() & 0x7FFF));

                if (message.getPartsTotal() > ONE) {
                    setMultiPartMessageText(result, message);
                } else {
                    result.setMessageText(message.getMessageText());
                }

                result.setDataCoding(message.getDataCoding());
            } catch (final BadCommandIDException e) {
                // This should never end up in here.
                LOG.error("Cannot create SubmitSM SMPP message object", e);
            }

            return result;
        }

        /*
         * Create Multipart Message as the SMPP API used supports till 157
         * characters
         */
        private void setMultiPartMessageText(SubmitSM result, TransmitMessage message) {
            result.setEsmClass(0x40);

            short ref = (short) message.getPartsReference();
            byte[] udh = { (byte) 0x6, (byte) 0x8, (byte) 0x4, (byte) ((ref >> 8) & 0xFF), (byte) (ref & 0xFF), (byte) message.getPartsTotal(), (byte) message.getPartNumber() };
            result.setMessage(udh, 0, udh.length, null);

            MessageEncoding encoding = result.getMessageEncoding();
            if (!(encoding instanceof AlphabetEncoding)) {
                encoding = EncodingFactory.getInstance().getDefaultAlphabet();
            }
            AlphabetEncoding a = (AlphabetEncoding) encoding;
            byte[] text = a.encodeString(message.getMessageText());
            byte[] msg = new byte[udh.length + text.length];
            System.arraycopy(udh, ZERO, msg, ZERO, udh.length);
            System.arraycopy(text, ZERO, msg, udh.length, text.length);
            result.setMessage(msg);
        }

        /*
         * Get the message to be sent as String
         */
        private String getMessageAsString(final SMPPRequest smppRequest, final TransmitMessage message, final long interval) {
            final StringBuffer s = new StringBuffer();
            s.append("message-key=");
            s.append(null == message ? "null" : message.getMessageKey());
            s.append('\t');
            s.append("smpp-header=");
            s.append(smppRequest.getLength());
            s.append(':');
            s.append(smppRequest.getCommandId());
            s.append(':');
            s.append(smppRequest.getCommandStatus());
            s.append(':');
            s.append(smppRequest.getSequenceNum());
            s.append('\t');
            s.append("service-type=");
            s.append(smppRequest.getServiceType());
            s.append('\t');
            s.append("source-addr=");
            if (null == smppRequest.getSource()) {
                s.append("null\t");
            } else {
                s.append(smppRequest.getSource().getTON());
                s.append(':');
                s.append(smppRequest.getSource().getNPI());
                s.append(':');
                s.append(smppRequest.getSource().getAddress());
                s.append('\t');
            }
            s.append("dest-addr=");
            s.append(smppRequest.getDestination().getTON());
            s.append(':');
            s.append(smppRequest.getDestination().getNPI());
            s.append(':');
            s.append(smppRequest.getDestination().getAddress());
            s.append('\t');
            s.append("esm-class=binary:");
            s.append(Integer.toBinaryString(smppRequest.getEsmClass()));
            s.append('\t');
            s.append("protocol-id=");
            s.append(smppRequest.getProtocolID());
            s.append('\t');
            s.append("priority-flag=");
            s.append(smppRequest.getPriority());
            s.append('\t');
            s.append("schedule-delivery-time=");
            s.append(smppRequest.getDeliveryTime());
            s.append('\t');
            s.append("validity-period=");
            s.append(smppRequest.getExpiryTime());
            s.append('\t');
            s.append("registered-delivery=binary:");
            s.append(Integer.toBinaryString(smppRequest.getRegistered()));
            s.append('\t');
            s.append("replace-if-present-flag=");
            s.append(smppRequest.getReplaceIfPresent());
            s.append('\t');
            s.append("data-coding=");
            s.append(smppRequest.getDataCoding());
            s.append('\t');
            s.append("sm-default-msg-id=");
            s.append(smppRequest.getDefaultMsg());
            s.append('\t');
            s.append("user-message-reference=");
            s.append(smppRequest.getOptionalParameter(Tag.USER_MESSAGE_REFERENCE));

            s.append('\t');
            s.append("ussd-service-op=");
            s.append(smppRequest.getOptionalParameter(Tag.USSD_SERVICE_OP));

            s.append('\t');
            s.append("sm-length=");
            s.append(smppRequest.getMessageLen());
            s.append('\t');
            s.append("pause-duration=");
            s.append(interval);
            s.append('\t');
            byte[] msg = smppRequest.getMessage();
            if (null != msg && msg.length > ZERO) {
                if (null != message && message.getPartsTotal() > ONE) {
                    final StringBuffer ms = new StringBuffer();
                    try {
                        int length = (int) msg[ZERO];
                        ms.append("udh-length=");
                        ms.append(length);
                        ms.append('\t');
                        ms.append("udh-msg-ref-num=");
                        if (msg[ZERO] == (byte) 0x5) {
                            ms.append((int) msg[THREE]);
                        } else {
                            ms.append((int) ((msg[THREE] << 8) + (msg[FOUR] & 0xff)));
                        }
                        ms.append('\t');
                        ms.append("udh-total-segments=");
                        ms.append((int) msg[length - ONE]);
                        ms.append('\t');
                        ms.append("udh-segment-seqnum=");
                        ms.append((int) msg[length]);
                        ms.append('\t');
                        ms.append("short-message=");
                        MessageEncoding encoding = smppRequest.getMessageEncoding();
                        if (encoding instanceof AlphabetEncoding) {
                            ms.append(((AlphabetEncoding) encoding).decodeString(Arrays.copyOfRange(msg, length + ONE, msg.length)));
                        }
                    } catch (Exception e) {
                    	LOG.error(ERROR_OCCURED, e);
                        ms.append("short-message=");
                        ms.append(smppRequest.getMessageText());
                    } finally {
                        s.append(ms);
                    }
                } else {
                    s.append("short-message=");
                    s.append(smppRequest.getMessageText());
                }
            }

            return s.toString();
        }
    }

    /*
     * Inner Class - Observer for User Inputs
     */
    private class ReceiverObserver extends SMPPEventAdapter {

        @Override
        public void submitSMResponse(Connection connection, SubmitSMResp submitSMResp) {
            final long receivedAt = System.currentTimeMillis();

            lastActivityTime.set(receivedAt);

            if (submitSMResp.getCommandStatus() == ESME_RTHROTTLED || submitSMResp.getCommandStatus() == ESME_RMSGQFUL) {
                TransmitMessage message = transmitMessageCache.get(submitSMResp.getSequenceNum());
                if (null != message) {
                    LOG.debug("MESSAGE THROLLED Or USSD GATEWAY Q FULL - " + message.serializeToString());
                    addToSendList(message);
                }
            } else {
                transmitMessageCache.remove(submitSMResp.getSequenceNum());
            }
        }
        

        // Handle message delivery. This method does not need to acknowledge the
        // deliver_sm message as we set the Connection object to
        // automatically acknowledge them.
        @Override
        public void deliverSM(Connection connection, DeliverSM deliverSM) {
            executor.submit(() -> deliverSMThread(deliverSM));
        }

        public void deliverSMThread(DeliverSM deliverSM) {
        	final long receivedAt = System.currentTimeMillis();
        	lastActivityTime.set(receivedAt);
        	int status = deliverSM.getCommandStatus();
        	long lstarttime = System.currentTimeMillis();
        	if (status != ZERO) {
        		LOG.error("DeliverSM: !Error! status = " + status);
        		return;
        	}
        	String messageBody = null;

        	if (deliverSM.getMessageLen() == ZERO || null != deliverSM.getOptionalParameter(Tag.MESSAGE_PAYLOAD)) {
        		try {

        			messageBody = new String((byte[]) deliverSM.getOptionalParameter(Tag.MESSAGE_PAYLOAD), "UTF-8");
        		} catch (UnsupportedEncodingException e) {
        			// UTF-8 is used
        			LOG.error("Exception in accepting the UTF-8 Encoding", e);
        		}
        	} else {
        		messageBody = deliverSM.getMessageText();
        	}
        	LOG.info("Before fetching The Tags");
        	String strLocationNumber ;
        	String strCellID ;

        	if(isGatewayEnabled == ONE) {

        		byte[] locationNumber = (byte[]) deliverSM.getOptionalParameter(Tag.getTag(LOCATION_NUMBER)); 
        		byte[] cellId = (byte[]) deliverSM.getOptionalParameter(Tag.getTag(CELL_ID));
        		strLocationNumber = new String(locationNumber, ZERO, locationNumber.length);
        		strCellID = new String(cellId, ZERO, cellId.length);
        	} else {            
        		int locationNumber = (int) deliverSM.getOptionalParameter(Tag.getTag(LOCATION_NUMBER));
        		int cellId = (int) deliverSM.getOptionalParameter(Tag.getTag(CELL_ID));
        		strLocationNumber = Integer.toString(locationNumber);
        		strCellID = Integer.toString(cellId);
        	}

        	if (strLocationNumber.isEmpty() || Utils.containsSpecialChar(strLocationNumber)) {
        		strLocationNumber = ZERO_0;
        	}
        	if (strCellID.isEmpty() || Utils.containsSpecialChar(strCellID)) {
        		strCellID = ZERO_0;
        	}

        	LOG.info("After Defining The Tags");
        	LOG.info("LocationNumber=>" + strLocationNumber);
        	LOG.info("cellId=>" + strCellID);

        	LOG.info("DeliverSM: \" messageAsString:" + transmitter.getMessageAsString(deliverSM, null, 0L) + "\"");

        	TransmitMessage request = null;

        	int serviceOp = null != deliverSM.getOptionalParameter(Tag.USSD_SERVICE_OP) ? ((Integer) deliverSM.getOptionalParameter(Tag.USSD_SERVICE_OP)).intValue() : -1;
        	String msisdn = deliverSM.getSource().getAddress();

        	LOG.info("msisdn ==> " + msisdn + " :: messageBody ==> " + messageBody + " :: Serivce OP ==>" + serviceOp);
        	if (null != messageBody && serviceOp > ZERO) {
        		try {

        			Integer sessionId = (Integer) deliverSM.getOptionalParameter(Tag.USER_MESSAGE_REFERENCE);
        			LOG.debug("User_Message_Reference :: sessionId = " + sessionId);
        			if (null != sessionId) {
        				if (ussdIngReqList.contains(messageBody)) {
        					UserInfo userInfo = null;        					
        					if (EIGHTEEN == serviceOp) {
        						if (consentMenuBody.containsKey(sessionId)) {
        							userInfo = consentMenu.get(sessionId);
        							if (messageBody.equals(ONE_1)) {
        								messageListenerImpl.logInfo(sessionId, userInfo, messageBody, CONSENT_ACCEPTED);
        								updaterDao.upsertUserConsentStatusDB(msisdn);
        								if(userInfo.isMlFlag()) {
        									messageBody = consentMenuBody.get(sessionId);
        									consentMenuBody.remove(sessionId);
        								}
        							}        							
        						} else if (menuLevelOne.containsKey(sessionId)) {
        							userInfo = menuLevelOne.get(sessionId);
        						} else if (menuLevelTwo.containsKey(sessionId)) {
        							userInfo = menuLevelTwo.get(sessionId);
        						} else if (myRewardsMenu.containsKey(sessionId)) {
        							userInfo = myRewardsMenu.get(sessionId);
        						} else if (consentOptOutMenu.containsKey(sessionId)) {
        							userInfo = consentOptOutMenu.get(sessionId);
        						} else if (userInfoMap.containsKey(sessionId)) {
        							userInfo = userInfoMap.get(sessionId);
        						}else {         							
        							userInfo = lookUpDAO.getUserInfo(msisdn);        							
        						}
        					} else {
        						userInfo = lookUpDAO.getUserInfo(msisdn);        						
        						if(null != userInfo && isOverallConsentEnabled ) {
        							userInfo.setConsentFlag(lookUpDAO.getConsentInfo(msisdn));
        						}
        					}
        					
        					if (null != userInfo && userInfo.isJFUEligible()) {
        						if (isBlacklistEnabled && userInfo.isBlacklistedUser()) {
        							setUserInfo(userInfo, msisdn, sessionId, deliverSM, strCellID, strLocationNumber, messageBody);
        							LOG.info("Blacklisted Msisdn :: " + msisdn + ", TransactionID: " + userInfo.getTxId());
        							request = processBlacklistedUser(userInfo, messageBody, sessionId, msisdn);
        						}else {
        							
        							switch (serviceOp) {
        							// service op 1 --> *2222# --> PPSR/PSSN
        							case ONE:
        								// ML Flow
        								setUserInfo(userInfo, msisdn, sessionId, deliverSM, strCellID, strLocationNumber, messageBody);

        								clearMenu(ML_OFFER_MENU, sessionId);                                    
        								clearMenu(RAG_OFFER_MENU, sessionId);
        								clearMenu(PED_OFFER_MENU, sessionId);
        								clearMenu(SAG_MENU, sessionId);                                  
        								clearMenu(CLEAR_CONSENT_MENU, sessionId);
        								clearMenu(CLEAR_USERINFO_MAP, sessionId);


        								if (userInfo.isMlFlag()) {
        								messageListenerImpl.logInfo(sessionId, userInfo, messageBody, STATUS_REQUEST_RECEIVED);
        								// put in menuLevelOne
        					        	generateUserBalanceReq(userInfo, ZERO);
        					        	userInfo.setMessageBody(STAR);
        					        	menuLevelOne.put(sessionId, userInfo);
        					        	UserInfoMapCache userInfoMapCache = UserInfoMapCache.instance();
        					        	userInfoMapCache.put(sessionId, userInfo);

        								} else {
        									if(isOverallConsentEnabled && !userInfo.isConsentFlag() && isNonMlConsentEnabled){        							
        										LOG.debug("Non-Ml Consent flow, MlFlag:: "+ userInfo.isMlFlag() );
        										request = createConsentMenu(messageBody, sessionId, userInfo);
        										consentMenu.put(sessionId, userInfo);
        										consentMenuBody.put(sessionId, messageBody);        									
        									}else {
        										UssdMessage ussdMessage = getUSSDMessageModel(deliverSM, messageBody, sessionId);
        										request = onReceivedPSSR(sessionId, ussdMessage, userInfo);
        										if (null != request) {
        											request.setReferenceNumber(sessionId);
        										}        								
        									}        								
        								}

        								break;

        								// service op 18 --> 1,2,3,4
        							case 18:

        								if (!userInfo.isMlFlag() && consentMenu.containsKey(sessionId) && messageBody.equals(ONE_1)) {
        									UssdMessage ussdMessage = getUSSDMessageModel(deliverSM, messageBody, sessionId);
        									request = onReceivedPSSR(sessionId, ussdMessage, userInfo);
        									if (null != request) {
        										request.setReferenceNumber(sessionId);
        									}
        									consentMenu.remove(sessionId);
        									consentMenuBody.remove(sessionId);
        									break;
        								}else if (isOverallConsentEnabled && userInfo.isMlFlag() && !consentMenu.containsKey(sessionId) && 
        										checkConsentMenuRequired(messageBody) && !userInfo.isConsentFlag() && !userInfo.isJ4uNewUser()) {
        									request = createConsentMenu(messageBody, sessionId, userInfo);
        									consentMenu.put(sessionId, userInfo);
        									consentMenuBody.put(sessionId, messageBody);
        									break;
        								} 

        								if(userInfo.isMlFlag() && !(messageBody.equals(STAR) || messageBody.equals(HASH))) {
        									consentMenu.put(sessionId, userInfo);
        								}

        								String menuOption = getMenuOption(sessionId, msisdn, userInfo, messageBody);
        								LOG.debug("msisdn = " + msisdn + " :: menuOption = " + menuOption);
        								switch (menuOption) {
        								case MAINMENUSEGMENTS:                                    	
        									request = getMainMenuSegments(sessionId, userInfo, messageBody);
											break;
        								case MY_REWARDS_MENU:                                    	
        									request = processMyRewardsMenu(messageBody, sessionId);
        									clearMenu(MY_REWARDS, sessionId);
        									break;		
        								case PED_OFFER_MENU:
        									UserInfo pedOfferMenuUserInfo = pedSubMenu.get(sessionId);
        									request = processPEDOfferMenu(messageBody, sessionId, pedOfferMenuUserInfo);
        									break;
        								case PED_HISTORY_MENU:
        									UserInfo pedHistoryMenuUserInfo = pedSubMenu.get(sessionId);
        									request = processPEDHistoryMenu(messageBody, sessionId, pedHistoryMenuUserInfo);

        									break;
        								case PED_AVAIL_PLAY_MENU:
        									UserInfo pedViewPlayMenuUserInfo = pedSubMenu.get(sessionId);
        									request = processPEDAvailablePlayMenu(messageBody, sessionId, pedViewPlayMenuUserInfo);

        									break;
        								case PED_PLAY_MENU:
        									UserInfo pedPlayMenuUserInfo = pedSubMenu.get(sessionId);
        									request = processPEDPlayMenu(messageBody, sessionId, pedPlayMenuUserInfo);

        									break;
        								case PED_SUB_MENU:
        									UserInfo pedSubMenuUserInfo = null;
        									if(myRewardsMenu.containsKey(sessionId)) {
        										pedSubMenuUserInfo = myRewardsMenu.get(sessionId);
        										userInfoMap.put(sessionId, pedSubMenuUserInfo);
        									}else {
        										pedSubMenuUserInfo = pedSubMenu.get(sessionId);
        									}
        									request = processPEDSubMenu(messageBody, sessionId, pedSubMenuUserInfo);
        									clearMenu(MY_REWARDS_MENU, sessionId);
        									break;

        								case RAG_OFFER_MENU:
        									UserInfo ragFinalMenuInfo = ragSubMenu.get(sessionId);
        									request = ragOfferInfoRequest(sessionId, messageBody, ragFinalMenuInfo);
        									ragMainMenu.put(sessionId, ragFinalMenuInfo);
        									clearMenu(RAG_SUB_MENU, sessionId);
        									break;                                        

        								case RAG_SUB_MENU:
        									UserInfo ragSubMenuInfo = ragMainMenu.get(sessionId);
        									request = ragSubMenuRequest(sessionId, messageBody, ragSubMenuInfo);
        									if(null != request && request.getServiceOp() == SEVENTEEN) {
        										clearMenu(RAG_MENU, sessionId);
        									}else {
        										ragMainMenu.put(sessionId, ragSubMenuInfo);
        										ragSubMenu.put(sessionId, ragSubMenuInfo);
        									}
        									break;                                        

        								case RAG_MAIN_MENU:
        									UserInfo ragMainMenuInfo = myRewardsMenu.get(sessionId);
        									LOG.debug("isRagAtlEnabled - " + isRagAtlEnabled + " isRagUser - " + ragMainMenuInfo.isRagUser());
        									if (ragMainMenuInfo.isRagUser() || isRagAtlEnabled) {
        										if (isRagAtlEnabled && !ragMainMenuInfo.isRagUser()) {
        											insertRagUserRecord(ragMainMenuInfo);
        										}
        										request = ragMainMenuRequest(sessionId, messageBody, ragMainMenuInfo);
        										ragMainMenu.put(sessionId, ragMainMenuInfo);
        										userInfoMap.put(sessionId, ragMainMenuInfo);
        										clearMenu(RAG_SUB_MENU, sessionId);
        										clearMenu(MY_REWARDS_MENU, sessionId);
        									} else {
        										LOG.error(msisdn + "User input is not valid");
        										request = sendWrongInputErrorNotification(sessionId, ragMainMenuInfo, messageBody, STATUS_INVALID_REQUEST, ML_RAG_MAIN_MENU_WRONG_SELECTION);
        									}

        									break;

        								case SAG_MAIN_MENU:
        									UserInfo sagMainMenuInfo = myRewardsMenu.get(sessionId);
        									LOG.debug("isSagAtlEnabled - " + isSagAtlEnabled + " isSagUser - " + sagMainMenuInfo.isSagUser());

        									if (!sagMainMenuInfo.isSagUser()) { 
        										//Msisdn not present in whitelist so insert in Whitelist Table
        										insertSagUserRecord(sagMainMenuInfo);
        									}
        									request = sagMainMenuRequest(sessionId, messageBody, sagMainMenuInfo);
        									sagMainMenu.put(sessionId, sagMainMenuInfo);
        									userInfoMap.put(sessionId, sagMainMenuInfo);
        									clearMenu(SAG_SUB_MENU, sessionId);
        									clearMenu(MY_REWARDS_MENU, sessionId);                                            

        									break; 

        								case SAG_SUB_MENU:
        									UserInfo sagSubMenuInfo = sagMainMenu.get(sessionId);
        									request = sagSubMenuRequest(sessionId, messageBody, sagSubMenuInfo);
        									if(null != request && request.getServiceOp() == SEVENTEEN) {
        										clearMenu(SAG_MENU, sessionId);
        									}else {
        										sagMainMenu.put(sessionId, sagSubMenuInfo);
        										sagSubMenu.put(sessionId, sagSubMenuInfo);
        									}                                        
        									break;     

        								case SAG_OFFER_MENU:
        									UserInfo sagFinalMenuInfo = sagSubMenu.get(sessionId);
        									request = sagOfferInfoRequest(sessionId, messageBody, sagFinalMenuInfo);
        									sagMainMenu.put(sessionId, sagFinalMenuInfo);        								
        									clearMenu(SAG_SUB_MENU, sessionId);
        									break;    

        								case ML_OFFER_MENU:
        									request = processMLOfferMenu(messageBody, sessionId);
        									break;

	        							case ML_SUB_MENU:
	        								request = processMLSubMenu(messageBody, msisdn, sessionId);
	        								break;
	
	        							case ML_SOCIAL_SUB_MENU:
	        								request = processMLSocialSubMenu(messageBody, sessionId);
	        								break;                                    
	
	        							case MORNING_SUB_MENU:                                    	                                    	
	        								request = processMLMorningSubMenu(messageBody, sessionId);
	        								clearMenu(MORNING_SUB_MENU, sessionId);
	        								break;
	
	        							case MORNING_OFFER_MENU:                                    	
	        								request = processMLMorningOfferMenu(messageBody, sessionId);
	        								clearMenu(MORNING_OFFER_MENU, sessionId);
	        								break;
	        								
	        							case J4U_TOWN_SUB_MENU:                                    	
	        								request = processMLTownSubMenu(messageBody, msisdn, sessionId);
	        								clearMenu(J4U_TOWN_SUB_MENU, sessionId);
        									break;	

	        							case NEW_USER_INVALID_MENU_SELECTION:
	        								request = processNewUserJ4UmenuSelection(messageBody, sessionId);
	        								break;                            	

	        							case J4U_MENU:
	    									// J4U Flow
	    									UserInfo userInfoNotify = new UserInfo();
	    									userInfoNotify.setDestAddress(userInfo.getDestAddress());
	    									userInfoNotify.setMsisdn(msisdn);
	    									userInfoNotify.setMessageBody(messageBody);
	    									userInfoNotify.setTxId(new Utils().getTransactionID());
	    									userInfoNotify.setLangCode("" + userInfo.getLangCode());
	    									
	    									if (!messageBody.equalsIgnoreCase(USER_SEL_1) && !messageBody.equalsIgnoreCase(USER_SEL_2) && !messageBody.equalsIgnoreCase(USER_SEL_3)) {
	    										request = sendWrongInputErrorNotification(sessionId, userInfoNotify, messageBody, STATUS_INVALID_REQUEST, ML_J4U_MENU_WRONG_SELECTION);
	    									} else {
	    										UssdMessage ussdMessage = getUSSDMessageModel(deliverSM, messageBody, sessionId);
	    										request = onReceivedUSSRAck(sessionId, ussdMessage);
	    										if (null != request) {
	    											request.setReferenceNumber(sessionId);
	    										}
	    									}
	    									break;
	
	    								case INVALID_MENU_SELECTION:                                    	
	    									//Case like #1413# ->7/8 is handled here
	    									if(menuLevelOne.containsKey(sessionId)) {
	    										UserInfo menuLevelOneInfo = menuLevelOne.get(sessionId);
	    										request = sendWrongInputErrorNotification(sessionId, menuLevelOneInfo, messageBody, STATUS_INVALID_REQUEST, ML_J4U_MENU_WRONG_SELECTION);        									
	    									}else if(menuLevelTwo.containsKey(sessionId)){
	    										UserInfo menuLevelTwoInfo = menuLevelTwo.get(sessionId);
	    										request = sendWrongInputErrorNotification(sessionId, menuLevelTwoInfo, messageBody, STATUS_INVALID_REQUEST, ML_J4U_MENU_WRONG_SELECTION);        									
	    									}else if(consentMenu.containsKey(sessionId)) {
	    										UserInfo consentUserInfo = consentMenu.get(sessionId);
	    										request = sendWrongInputErrorNotification(sessionId, consentUserInfo, messageBody, STATUS_INVALID_REQUEST, ML_J4U_MENU_WRONG_SELECTION);        									
	    									}else if(myRewardsMenu.containsKey(sessionId)) {
	    										UserInfo myRewardsUserInfo = myRewardsMenu.get(sessionId);
	    										request = sendWrongInputErrorNotification(sessionId, myRewardsUserInfo, messageBody, STATUS_INVALID_REQUEST, ML_J4U_MENU_WRONG_SELECTION);
	    										clearMenu(MY_REWARDS_MENU, sessionId);          									
	    									}else if(sagMainMenu.containsKey(sessionId)) {
	    										UserInfo sagSubMenuUserInfo = sagMainMenu.get(sessionId);
	    										request = sendWrongInputErrorNotification(sessionId, sagSubMenuUserInfo, messageBody, STATUS_INVALID_REQUEST, ML_J4U_MENU_WRONG_SELECTION);
	    										clearMenu(SAG_MENU, sessionId);        									
	    									}else if(sagSubMenu.containsKey(sessionId)) {
	    										UserInfo sagOfferMenuUserInfo = sagSubMenu.get(sessionId);
	    										request = sendWrongInputErrorNotification(sessionId, sagOfferMenuUserInfo, messageBody, STATUS_INVALID_REQUEST, ML_J4U_MENU_WRONG_SELECTION);
	    										clearMenu(SAG_MENU, sessionId);        									
	    									}
	    									break;	
	
	    								case CONSENT_OPT_OUT_MENU:                                    	
	    									request = consentOptOutMenu(messageBody, sessionId, userInfo);
	    									menuLevelOne.remove(sessionId);
	    									break;		

        								case CONSENT_OPT_OUT:                                    	
        									request = consentOptOut(messageBody, sessionId, userInfo);
        									clearMenu(CLEAR_CONSENT_MENU, sessionId);
        									break;		

        								case CONSENT_DENIED:
        									UserInfo consentMenuUserInfo = consentMenu.get(sessionId);
        									request = sendDenialNotification(sessionId, consentMenuUserInfo,
        											messageBody, CONSENT_DENIED);
        									clearMenu(CLEAR_CONSENT_MENU, sessionId);
        									break;
        								default:
        									LOG.error("unknown menuOption :: "+ menuOption);
        									break;

        								}									
        								//Switch case 18 break
        								break;
        							default:
        								LOG.error("unknown serviceOp :: "+ serviceOp);
        								break;
        							}
        						} 
        					} else {
        						if(null != userInfo){
        							//User present in reduced CCR but not J4UEligible
        							UserInfo userInfoNotify = new UserInfo();
            						setUserInfo(userInfoNotify, msisdn, sessionId, deliverSM, strCellID, strLocationNumber, messageBody);
        							request= notifyUser(sessionId, userInfoNotify, messageBody, STATUS_USER_INELIGIBLE, userInfo.getLangCode(), false);
        						}else {
        							//New user,User not present in reduced CCR but assuming J4UEligible                            		
        							UserInfo newUserInfo = getJ4UnewCustomerInfo(deliverSM, messageBody, sessionId, strCellID, strLocationNumber);
        							request = processJ4UnewUserMenu(newUserInfo, sessionId, messageBody, msisdn);
        						}                            	
        					}
        				} else {
        					request = handleInvalidUserInput(msisdn, deliverSM, messageBody, sessionId);
        				}
        			}
        		} catch (Exception e) {
        			LOG.error("Error Occurred..! " + e.getMessage() + "::", e);
        		}
        	}

        	final DeliverSMResp deliverSMResp = new DeliverSMResp(deliverSM);
        	if (ussdConnection.isBound()) {
        		reentrantLock.lock();
        		try {
        			ussdConnection.sendResponse(deliverSMResp);
        		} catch (IOException e) {
        			LOG.error(ERROR_OCCURED, e);
        		} finally {
        			reentrantLock.unlock();
        		}
        	}

        	if (null != request) {
        		request.setLstarttime(lstarttime);
        		addToSendList(request);
        	}
        }
        
        private TransmitMessage handleInvalidUserInput(String msisdn, DeliverSM deliverSM, String messageBody, int sessionId) throws Exception {
        	// reject
			LOG.error(msisdn + " User input is not valid");
			UserInfo userInfo = lookUpDAO.getUserInfo(msisdn);
			if (null == userInfo) {
				userInfo = new UserInfo();
				// default french
				userInfo.setLangCode(ONE_1); 
			}
			userInfo.setDestAddress(deliverSM.getDestination().getAddress());
			userInfo.setMsisdn(msisdn);
			userInfo.setMessageBody(messageBody);
			userInfo.setTxId(new Utils().getTransactionID()); 

            return sendWrongInputErrorNotification(sessionId, userInfo, messageBody, STATUS_INVALID_REQUEST, ML_J4U_STATIC_MENU_WRONG_SELECTION);
        }
        
        private TransmitMessage getMainMenuSegments(Integer sessionId, UserInfo userInfo, String messageBody) throws Exception {
        	TransmitMessage transmitMessage;
        	if (ONE == isSocialEnabled ) {
        		if(messageBody.equals(STAR)) {
        			transmitMessage = initialSocialRequestPart1(sessionId, userInfo, messageBody);
            	}else {
            		transmitMessage = initialSocialRequestPart2(sessionId, userInfo, messageBody);
            		
    			}
        	} else {
        		if(messageBody.equals(STAR)) {
        			transmitMessage = initialMLRequestPart1(sessionId, userInfo, messageBody);
            	}else {
            		transmitMessage = initialMLRequestPart2(sessionId, userInfo, messageBody);	
    			}
			}
        	return transmitMessage;
        }
        
        private void setUserInfo(UserInfo userInfo, String msisdn, Integer sessionId, DeliverSM deliverSM, String strCellID, String strLocationNumber, String messageBody) {
        	userInfo.setMsisdn(msisdn);
			userInfo.setTxId(new Utils().getTransactionID());
			userInfo.setUserMsgRef(sessionId);
			userInfo.setDestAddress(deliverSM.getDestination().getAddress());
			userInfo.setCellId(strCellID);
			userInfo.setLocationNumber(strLocationNumber);
			userInfo.setMessageBody(messageBody);
			
			if (null != strCellID) {
				CellIDPoolIDCache cellIDPoolIDCache = CellIDPoolIDCache.instance() ;
				String poolID = cellIDPoolIDCache.getPoolIDForCellID(strCellID);
				LOG.debug("Pool ID received is ==>" + poolID);
				userInfo.setPoolId(poolID);
			}
		}
        
        /**
         * @param messageBody
         * @return boolean
         */
        private boolean checkConsentMenuRequired(String messageBody) {

        	boolean bResult = false;
        	if(ONE == isSocialEnabled && checkConsentMenuOptionsSocial.test(messageBody)) {        		
        		bResult =  true;        		
        	}else {
        		if (checkConsentMenuOptionsNoSocial.test(messageBody)) {
        			bResult =  true;
        		}
        	}
        	return bResult;
        }        
        
        private TransmitMessage processPEDOfferMenu(String messageBody, Integer sessionId, UserInfo userInfo) throws Exception {
            
            // pedNoPrizeMenu
            if (!messageBody.equalsIgnoreCase(USER_SEL_1) && !messageBody.equalsIgnoreCase(USER_SEL_2)) {
                return sendWrongInputErrorNotification(sessionId, userInfo, messageBody, STATUS_INVALID_REQUEST, ML_PED_OFFER_MENU_WRONG_SELECTION);

            }

            TransmitMessage message = null;
            if (USER_SEL_2.equals(messageBody)) {
            	clearMenu(PED_BACK_MENU, sessionId);
            	message = processPEDSubMenu(messageBody, sessionId, userInfo);
            } else if (USER_SEL_1.equals(messageBody)) {
            	if (pedNoPrizeMenu.containsKey(sessionId)) {
            		clearMenu(PED_OFFER_MENU, sessionId);
            		if (ONE == isSocialEnabled ) {
            			// sending j4u main menu 0.Morning/nothing, 1.voice, 2.data
            			//3.integrated 4. hourly data, 5.Social 6.MyRewards 
            			message = initialSocialRequestPart1(sessionId, userInfo, messageBody);
            		}else {
            			// sending j4u main menu 0.Morning/nothing, 1.voice, 2.data
            			//3.integrated 4. hourly data, 5.MyRewards 
            			message = initialMLRequestPart1(sessionId, userInfo, messageBody);
            		}
            		consentMenu.remove(sessionId);
            		userInfo.setMessageBody(STAR);
            		menuLevelOne.put(sessionId, userInfo);
            	} else if (pedPlayMenu.containsKey(sessionId) || pedViewPlayMenu.containsKey(sessionId)) {
            		message = processPEDPlayMenu(messageBody, sessionId, userInfo);
            	}

            }
            return message;
        }

        private TransmitMessage processPEDAvailablePlayMenu(String messageBody, Integer sessionId, UserInfo userInfo) throws Exception {
            TransmitMessage message = null;
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, PED_AVAIL_PLAY_MENU_REQUEST_RECEIVED);
            InboundUssdMessage inboundUssdMessage = messageListenerImpl.pedAvailablePlayReqReceived(userInfo);
            if (null != inboundUssdMessage) {
                message = new TransmitMessage();

                if (inboundUssdMessage.getSelProdId().equalsIgnoreCase(PropertiesLoader.getValue(USSD_ML_PED_NO_PLAY_MENU))) {
                    message.setDataCoding(ONE);
                    message.setMessageEncoding(ONE);
                    message.setSourceAddress(userInfo.getDestAddress());
                    message.setDestinationAddress(userInfo.getMsisdn());
                    message.setMessageText(inboundUssdMessage.getClobString());
                    message.setTransactionId(userInfo.getTxId());
                    message.setServiceOp(SEVENTEEN);
                    message.setReferenceNumber(sessionId);
                    message.setMlFlag(userInfo.isMlFlag());
                    message.setRandomFlag(userInfo.isRandomFlag());
                    message.setUssdLogStatus(TWO);
                    message.setLogStatus(PED_AVAIL_PLAY_MENU_RESPONSE_SENT);

                } else {
                    message.setMessageEncoding(ONE);
                    message.setSourceAddress(userInfo.getDestAddress());
                    message.setDestinationAddress(userInfo.getMsisdn());
                    message.setMessageText(inboundUssdMessage.getClobString());
                    message.setTransactionId(userInfo.getTxId());
                    message.setServiceOp(TWO);
                    message.setReferenceNumber(sessionId);
                    message.setMlFlag(userInfo.isMlFlag());
                    message.setRandomFlag(userInfo.isRandomFlag());
                    message.setUssdLogStatus(TWO);
                    message.setLogStatus(PED_AVAIL_PLAY_MENU_RESPONSE_SENT);
                }
                // setting 2 for ussd log table status
            }
            pedViewPlayMenu.put(sessionId, userInfo);
            return message;
        }

        private TransmitMessage processPEDPlayMenu(String messageBody, Integer sessionId, UserInfo userInfo) throws Exception {
            TransmitMessage message = null;
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, PED_PLAY_MENU_REQUEST_RECEIVED);

            InboundUssdMessage inboundUssdMessage = messageListenerImpl.pedPlayReqReceived(userInfo);
            if (null != inboundUssdMessage) {
                message = new TransmitMessage();
                if (inboundUssdMessage.getSelProdId().equalsIgnoreCase(PropertiesLoader.getValue(USSD_ML_PED_NO_PLAY_MENU))) {
                    message.setDataCoding(ONE);
                    message.setMessageEncoding(ONE);
                    message.setSourceAddress(userInfo.getDestAddress());
                    message.setDestinationAddress(userInfo.getMsisdn());
                    message.setMessageText(inboundUssdMessage.getClobString());
                    message.setTransactionId(userInfo.getTxId());
                    message.setServiceOp(SEVENTEEN);
                    message.setReferenceNumber(sessionId);
                    message.setMlFlag(userInfo.isMlFlag());
                    message.setRandomFlag(userInfo.isRandomFlag());
                    message.setUssdLogStatus(TWO);
                } else if (inboundUssdMessage.getSelProdId().equalsIgnoreCase(PropertiesLoader.getValue(USSD_ML_PED_NO_PRIZE_MENU))) {

                    pedNoPrizeMenu.put(sessionId, userInfo);
                    message.setDataCoding(ONE);
                    message.setMessageEncoding(ONE);
                    message.setSourceAddress(userInfo.getDestAddress());
                    message.setDestinationAddress(userInfo.getMsisdn());
                    message.setMessageText(inboundUssdMessage.getClobString());
                    message.setTransactionId(userInfo.getTxId());
                    message.setServiceOp(TWO);
                    message.setReferenceNumber(sessionId);
                    message.setMlFlag(userInfo.isMlFlag());
                    message.setRandomFlag(userInfo.isRandomFlag());
                    message.setUssdLogStatus(TWO);
                } else {
                    // prize
                    message.setDataCoding(ONE);
                    message.setMessageEncoding(ONE);
                    message.setSourceAddress(userInfo.getDestAddress());
                    message.setDestinationAddress(userInfo.getMsisdn());
                    message.setMessageText(inboundUssdMessage.getClobString());
                    message.setTransactionId(userInfo.getTxId());
                    message.setServiceOp(TWO);
                    message.setReferenceNumber(sessionId);
                    message.setMlFlag(userInfo.isMlFlag());
                    message.setRandomFlag(userInfo.isRandomFlag());
                    message.setUssdLogStatus(TWO);
                }
                message.setLogStatus(PED_PLAY_MENU_RESPONSE_SENT);
            }
            pedPlayMenu.put(sessionId, userInfo);
            return message;
        }

        private TransmitMessage processPEDHistoryMenu(String messageBody, Integer sessionId, UserInfo userInfo) throws Exception {
            TransmitMessage message = null;
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, PED_HISTORY_MENU_REQUEST_RECEIVED);

            InboundUssdMessage inboundUssdMessage = messageListenerImpl.pedPrizeHistoryReqReceived(userInfo);
            if (null != inboundUssdMessage) {
                message = new TransmitMessage();
                if (inboundUssdMessage.getSelProdId().equalsIgnoreCase(PropertiesLoader.getValue(USSD_ML_PED_NO_HISTORY_MENU))) {
                    LOG.debug("pedNoHistoryMenu template case=>");
                    pedNoHistoryMenu.put(sessionId, userInfo);
                    message.setDataCoding(ONE);
                    message.setMessageEncoding(ONE);
                    message.setSourceAddress(userInfo.getDestAddress());
                    message.setDestinationAddress(userInfo.getMsisdn());
                    message.setMessageText(inboundUssdMessage.getClobString());
                    message.setTransactionId(userInfo.getTxId());
                    message.setServiceOp(SEVENTEEN);
                    message.setReferenceNumber(sessionId);
                    message.setMlFlag(userInfo.isMlFlag());
                    message.setRandomFlag(userInfo.isRandomFlag());
                    message.setUssdLogStatus(TWO);
                    message.setLogStatus(PED_HISTORY_MENU_RESPONSE_SENT);
                } else {
                    message.setDataCoding(ONE);
                    message.setMessageEncoding(ONE);
                    message.setSourceAddress(userInfo.getDestAddress());
                    message.setDestinationAddress(userInfo.getMsisdn());
                    message.setMessageText(inboundUssdMessage.getClobString());
                    message.setTransactionId(userInfo.getTxId());
                    message.setServiceOp(SEVENTEEN);
                    message.setReferenceNumber(sessionId);
                    message.setMlFlag(userInfo.isMlFlag());
                    message.setRandomFlag(userInfo.isRandomFlag());
                    message.setUssdLogStatus(TWO);
                    message.setLogStatus(PED_HISTORY_MENU_RESPONSE_SENT);
                }

            }
            pedHistoryMenu.put(sessionId, userInfo);
            return message;
        }

        private TransmitMessage processPEDSubMenu(String messageBody, Integer sessionId, UserInfo userInfo) throws Exception {
            TransmitMessage message = null;
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, PED_SUBMENU_REQUEST_RECEIVED);

            PEDProcessService pedProcessService = new PEDProcessService();
            if (pedProcessService.checkPedProcessPlayFlag()) {
                String pedSubMenuTemplate = PropertiesLoader.getValue(USSD_ML_PED_PLAY_ALERT_MENU);
                LOG.debug("ped alert menu templated Id :: " + pedSubMenuTemplate);
                InboundUssdMessage inboundUssdMessage = messageListenerImpl.pedSubMenuReqReceived(pedSubMenuTemplate, userInfo.getLangCode());

                message = new TransmitMessage();
                message.setDataCoding(ONE);
                message.setMessageEncoding(ONE);
                message.setSourceAddress(userInfo.getDestAddress());
                message.setDestinationAddress(userInfo.getMsisdn());
                message.setMessageText(inboundUssdMessage.getClobString());
                message.setTransactionId(userInfo.getTxId());
                message.setServiceOp(SEVENTEEN);
                message.setReferenceNumber(sessionId);
                message.setMlFlag(userInfo.isMlFlag());
                message.setRandomFlag(userInfo.isRandomFlag());
                message.setUssdLogStatus(TWO);
                message.setLogStatus(PED_SUBMENU_RESPONSE_SENT);
            } else {
                if (!messageBody.equalsIgnoreCase(USER_SEL_1) && !messageBody.equalsIgnoreCase(USER_SEL_2) && !messageBody.equalsIgnoreCase(USER_SEL_3)) {
                    //  && !checkMyRewardsMenu.test(messageBody)
                    return sendWrongInputErrorNotification(sessionId, userInfo, messageBody, STATUS_INVALID_REQUEST, ML_PED_SUB_MENU_WRONG_SELECTION);
                }
                String pedSubMenuTemplate = PropertiesLoader.getValue(USSD_ML_PED_SUB_MENU);
                LOG.debug("ped sub menu templated Id :: " + pedSubMenuTemplate);
                InboundUssdMessage inboundUssdMessage = messageListenerImpl.pedSubMenuReqReceived(pedSubMenuTemplate, userInfo.getLangCode());
                if (null != inboundUssdMessage) {
                    message = new TransmitMessage();
                    message.setDataCoding(ONE);
                    message.setMessageEncoding(ONE);
                    message.setSourceAddress(userInfo.getDestAddress());
                    message.setDestinationAddress(userInfo.getMsisdn());
                    message.setMessageText(inboundUssdMessage.getClobString());
                    message.setTransactionId(userInfo.getTxId());
                    message.setServiceOp(TWO);
                    message.setReferenceNumber(sessionId);
                    message.setMlFlag(userInfo.isMlFlag());
                    message.setRandomFlag(userInfo.isRandomFlag());
                    message.setUssdLogStatus(TWO);
                    message.setLogStatus(PED_SUBMENU_RESPONSE_SENT);
                    // setting 2 for ussd log table status
                }
            }
            pedSubMenu.put(sessionId, userInfo);
            return message;
        }

        private TransmitMessage processMLSubMenu(String messageBody, String msisdn, Integer sessionId) throws Exception {
            // ML flow
        	TransmitMessage request = null;
            UserInfo menuLevelOneInfo = menuLevelOne.get(sessionId);
            menuLevelOneInfo.setMessageBody(messageBody);
            QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
        	JSONObject queryBaljson = queryBaljsonMap.get(sessionId);
        	messageListenerImpl.logInfo(sessionId, menuLevelOneInfo, messageBody, SUBMENU_REQUEST_RECEIVED);
        	
            // check random user or target user?
            // check random user
            if (menuLevelOneInfo.isLocationRandomFlag() || menuLevelOneInfo.isRandomFlag()) {
            	LOG.info("Random User msisdn: " + menuLevelOneInfo.getMsisdn());
            	AAMap aaMap = AAMap.instance();
            	aaMap.put(sessionId, menuLevelOneInfo);
            	if (isSubMenuCallEnabled &&  (queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(OCS_QUERY_BAL_TIMEOUT) 
            			|| queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(STATUS_EXCEPTION))) {
                	generateUserBalanceReq(menuLevelOneInfo, ZERO);
				} else {
					request = subMenuMLRequest(sessionId, messageBody, menuLevelOneInfo, true);
					processAaEligibleUser(menuLevelOneInfo, aaMap, sessionId, queryBaljson);				
				}
 	
            	// send to user and put in menuLevelTwo
            	menuLevelTwo.put(sessionId, menuLevelOneInfo);
            } else {
                // target user
                String offerRefFlag = mlRefreshFlagMap.get(messageBody);
                LOG.info("Target User :: " + menuLevelOneInfo.getMsisdn() + " offerRefFlag :: " + offerRefFlag);
                menuLevelOneInfo.setSelProdType(offerRefFlag);
                AAMap aaMap = AAMap.instance();
                aaMap.put(sessionId, menuLevelOneInfo);
                
                // send to ocs and put in menuLevelTwo
                menuLevelTwo.put(sessionId, menuLevelOneInfo);

                UserInfo testUserInfo = menuLevelTwo.get(sessionId);
                LOG.debug("testUserInfo sessionId - " + sessionId);
                LOG.debug("testUserInfo - " + testUserInfo);
                
                // send REFRESH_RF_VALUE value 1 as third param to
                // indicate offer refresh required
                if(menuLevelOneInfo.getPrefPayMethod().equals(PREF_PAY_MET_MPESA)) {
                	generateUserBalanceReq(menuLevelOneInfo, ONE);
                }else {
                	if (isSubMenuCallEnabled && (queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(OCS_QUERY_BAL_TIMEOUT) 
                			|| queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(STATUS_EXCEPTION))) {
	                	generateUserBalanceReq(menuLevelOneInfo, ONE);
					} else {
						queryBaljson.put(REFRESH_RF_VALUE, ONE);
                    	queryBaljson.put(USER_SELECTION, messageBody);
                    	queryBaljson.put(PRODUCT_TYPE, mlSubMenuProdTypeMenu.get(messageBody));
                    	UserInfo userInfo = messageListenerImpl.getSubMenuForTgtMLUser(queryBaljson);
                    	LOG.debug("User Info calling USSD gateway to send => " + userInfo);
                    	sendMessage(userInfo);
					}
				}                	
            }
            return request;
        }
        
        
        private TransmitMessage processMLSocialSubMenu(String messageBody, Integer sessionId) throws Exception {
        	// ML flow
        	TransmitMessage request = null;
        	UserInfo menuLevelOneInfo = menuLevelOne.get(sessionId);
        	menuLevelOneInfo.setMessageBody(messageBody);
        	QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
        	JSONObject queryBaljson = queryBaljsonMap.get(sessionId);
        	messageListenerImpl.logInfo(sessionId, menuLevelOneInfo, messageBody, SUBMENU_REQUEST_RECEIVED);
        	
        	// check random user or target user? 
        	// For Random User
        	if (menuLevelOneInfo.isRandomFlag()) {
        		LOG.info("Random User msisdn: " + menuLevelOneInfo.getMsisdn());
        		AAMap aaMap = AAMap.instance();
        		aaMap.put(sessionId, menuLevelOneInfo);        		
        		LOG.debug("aaMap sessionId  - " + sessionId);
        		if (isSubMenuCallEnabled && (queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(OCS_QUERY_BAL_TIMEOUT) 
        				|| queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(STATUS_EXCEPTION))) {
                	generateUserBalanceReq(menuLevelOneInfo, ZERO);
				} else {
					request = subMenuMLRequest(sessionId, messageBody, menuLevelOneInfo, true);
					processAaEligibleUser(menuLevelOneInfo, aaMap, sessionId, queryBaljson);
				}
        		
        		// send to user and put in menuLevelTwo
        		menuLevelTwo.put(sessionId, menuLevelOneInfo);
        	} else {
        		// target user
        		String offerRefFlag = mlRefreshFlagMap.get(messageBody);
        		LOG.info("Target User :: " + menuLevelOneInfo.getMsisdn() + " offerRefFlag :: " + offerRefFlag);
        		menuLevelOneInfo.setSelProdType(offerRefFlag);
        		AAMap aaMap = AAMap.instance();
        		aaMap.put(sessionId, menuLevelOneInfo);
        		if (menuLevelOneInfo.getOfferRefreshFlag().equalsIgnoreCase(VALUE_N) ||
        				menuLevelOneInfo.getOfferRefreshFlag().indexOf(offerRefFlag) == -1) {

        			if (isSubMenuCallEnabled && (queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(OCS_QUERY_BAL_TIMEOUT) 
        					|| queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(STATUS_EXCEPTION))) {
                    	generateUserBalanceReq(menuLevelOneInfo, ZERO);
    				} else {
    					LOG.debug("OFFER_REFRESH_FLAG = N OR index of OFFER_REFRESH_FLAG = -1 --> Condition is true  receiving  Cache offers =>");
    					request = subMenuMLRequest(sessionId, messageBody, menuLevelOneInfo, false);
    					processAaEligibleUser(menuLevelOneInfo, aaMap, sessionId, queryBaljson);
    				}
            		
        			LOG.debug("menuLevelOneInfo.getAaEligibleProdIdProdPriceMap().size() - " + menuLevelOneInfo.getAaEligibleProdIdProdPriceMap().size());
        			
        			menuLevelTwo.put(sessionId, menuLevelOneInfo);
        		} else {
        			// send to ocs and put in menuLevelTwo
        			menuLevelTwo.put(sessionId, menuLevelOneInfo);

        			UserInfo testUserInfo = menuLevelTwo.get(sessionId);
        			LOG.debug("testUserInfo sessionId - " + sessionId);
        			LOG.debug("testUserInfo - " + testUserInfo);
        			
        			// send REFRESH_RF_VALUE value 1 as third param to
        			// indicate offer refresh required
        			if(menuLevelOneInfo.getPrefPayMethod().equals(PREF_PAY_MET_MPESA)) {
                    	generateUserBalanceReq(menuLevelOneInfo, ONE);
                    }else {
                    	if (isSubMenuCallEnabled && (queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(OCS_QUERY_BAL_TIMEOUT) 
                    			|| queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(STATUS_EXCEPTION))) {
    	                	generateUserBalanceReq(menuLevelOneInfo, ONE);
    					} else {
    						queryBaljson.put(REFRESH_RF_VALUE, ONE);
                        	queryBaljson.put(USER_SELECTION, messageBody);
                        	queryBaljson.put(PRODUCT_TYPE, mlSubMenuProdTypeMenu.get(messageBody));
                        	UserInfo userInfo = messageListenerImpl.getSubMenuForTgtMLUser(queryBaljson);
                        	LOG.debug("User Info calling USSD gateway to send => " + userInfo);
                        	sendMessage(userInfo);
    					}
    				}
        		}
        	}
        	return request;
        }
        
        private void processAaEligibleUser(UserInfo menuLevelOneInfo, AAMap aaMap, Integer sessionId, JSONObject queryBaljson) {
        	if (menuLevelOneInfo.getAaEligible() == ONE && menuLevelOneInfo.getPrefPayMethod().equalsIgnoreCase(PREF_PAY_METHOD_G)) {
				UserInfo aaMapInfo = aaMap.get(sessionId);
				aaMapInfo.setAirtimeAdvBal(queryBaljson.getLong(AIRTIME_ADVANCE_BALANCE));
				aaMapInfo.setActBal(queryBaljson.getLong(ACCOUNT_BALANCE));
				LOG.debug("queryBaljson.getInt(USER_MSG_REF) cache flow - " + queryBaljson.getInt(USER_MSG_REF));
				LOG.debug("aaMapInfo.getAirtimeAdvBal cache flow - " + aaMapInfo.getAirtimeAdvBal());
				LOG.debug("aaMapInfo.setActBal cache flow - " + aaMapInfo.getActBal());
				aaMap.put(queryBaljson.getInt(USER_MSG_REF), aaMapInfo);
			}
    	}
        
        private TransmitMessage processMLMorningSubMenu(String messageBody, Integer sessionId) throws Exception {
        	TransmitMessage request = null;
        	UserInfo menuLevelOneInfo = menuLevelOne.get(sessionId);
        	//For Whitelisted customers        	
        	if (checkMorningEligibility.test(menuLevelOneInfo)) {
        		LOG.info("Morning Offer check Done!!");
        		LOG.info("Morning User msisdn: " + menuLevelOneInfo.getMsisdn());
        		
        		// send to user and put in menuLevelTwo
        		String offerRefFlag = mlRefreshFlagMap.get(messageBody);
        		LOG.info("Morning User :: " + menuLevelOneInfo.getMsisdn() + " offerRefFlag :: " + offerRefFlag);
        		menuLevelOneInfo.setSelProdType(offerRefFlag);        		
        		menuLevelOneInfo.setMessageBody(messageBody);  
        		messageListenerImpl.logInfo(sessionId, menuLevelOneInfo, messageBody, SUBMENU_REQUEST_RECEIVED);
        		QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
        		JSONObject queryBaljson = queryBaljsonMap.get(sessionId);
        		if (isSubMenuCallEnabled && (queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(OCS_QUERY_BAL_TIMEOUT) 
        				|| queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(STATUS_EXCEPTION))) {
                	generateUserBalanceReq(menuLevelOneInfo, ZERO);
                	UserInfoMapCache userInfoMapCache = UserInfoMapCache.instance();
                	userInfoMapCache.put(sessionId, menuLevelOneInfo);
				} else {
					request = messageListenerImpl.getSubMenuMLMorningRequest(queryBaljson, sessionId, messageBody, menuLevelOneInfo);
				}

        		if (null != request &&  request.getServiceOp() == TWO ) {
        			menuLevelOneInfo.setProdIds(request.getProductIdList());     			
        		}
        		menuLevelTwo.put(sessionId, menuLevelOneInfo);

        	}else {
        		//Customer not eligible or doesn't fall in window timing
        		request = morningFailureMsgRequest(sessionId, messageBody, menuLevelOneInfo);
        	}
        	return request;
        }
        
        private TransmitMessage processMLTownSubMenuRequest(String messageBody, Integer sessionId, UserInfo menuLevelOneInfo) throws Exception {
        	TransmitMessage request = null;      	       		
        	AAMap aaMap = AAMap.instance();
        	aaMap.put(sessionId, menuLevelOneInfo);
        	QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
        	JSONObject queryBaljson = queryBaljsonMap.get(sessionId);
        	if (isSubMenuCallEnabled && (queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(OCS_QUERY_BAL_TIMEOUT)
        			|| queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(STATUS_EXCEPTION))) {
        		generateUserBalanceReq(menuLevelOneInfo, ZERO);
        	} else {
        		request = messageListenerImpl.getSubMenuMLTownRequest(queryBaljson, sessionId, messageBody, menuLevelOneInfo);
        		processAaEligibleUser(menuLevelOneInfo, aaMap, sessionId, queryBaljson);
        		menuLevelOneInfo.setTownName(request.getTownName());
        	}

        	if (null != request &&  request.getServiceOp() == TWO ) {
        		menuLevelOneInfo.setProdIds(request.getProductIdList());     			
        	}
        	menuLevelOneInfo.setMpesaUser(true);
        	menuLevelOneInfo.setOfferCount(FIVE);
        	// send offers to user and put in menuLevelTwo 	
        	menuLevelTwo.put(sessionId, menuLevelOneInfo);
        	return request;
        } 
        private TransmitMessage processMLTownSubMenu(String messageBody, String msisdn, Integer sessionId) throws Exception {
        	TransmitMessage request = null;
        	UserInfo menuLevelOneInfo = menuLevelOne.get(sessionId);
        	messageListenerImpl.logInfo(sessionId, menuLevelOneInfo, messageBody, SUBMENU_REQUEST_RECEIVED); 
        	LOG.info("J4U Town User msisdn: " + menuLevelOneInfo.getMsisdn());
        	//User dialled 5 for TownOffers,this info later used in menuLevelTwo
        	menuLevelOneInfo.setMessageBody(messageBody);
        	if (menuLevelOneInfo.isJ4uNewUser()) {
    			if (null != menuLevelOneInfo.getTownName()) {
    				int langCd = lookUpDAO.getNewCustomerLangCode(msisdn);
	        		if (ZERO != langCd) {
	        			menuLevelOneInfo.setLangCode(""+langCd);
	        			request = processMLTownSubMenuRequest(messageBody, sessionId, menuLevelOneInfo);
	        		} else {
	        			request = querySubsProfileAndProcessMLTownSubMenu(messageBody, msisdn, sessionId, menuLevelOneInfo);	
	        		}
    			} else {
					request = messageListenerImpl.getNoOffersAvailableMsgRequest(sessionId, messageBody, menuLevelOneInfo, PropertiesLoader.getValue(USSD_ML_TOWN_FAILURE_MSG_TEMPLATE));
				}

			} else {
				request = processMLTownSubMenuRequest(messageBody, sessionId, menuLevelOneInfo);
			}
        	      	       		
        	return request; 
        }        
               
        private TransmitMessage querySubsProfileAndProcessMLTownSubMenu(String messageBody, String msisdn, Integer sessionId, UserInfo userInfo) throws Exception {
        	TransmitMessage message = null;
        	JSONObject response = querySubscriberProfile.checkCustomerProfile(msisdn);
        	if (response.getString(ICAP_STATUS).equalsIgnoreCase(SUCCESS)) {
        		if (SUBSCRIBER_STATE_ACTIVE.equalsIgnoreCase(response.getString(ICAP_SUBSCRIBER_STATE))) {
        			message = processActiveSubscriber(response, messageBody, msisdn, sessionId, userInfo);
        		} else {
        			// Process the user's menu selection for inactive subscribers
        			message = processInactiveSubscriber(messageBody, sessionId);
        		}
			} else {
				message = processInactiveOrErrorSubscriber(response, messageBody, sessionId, userInfo);
			}
			return message;
		}

        private TransmitMessage processInactiveOrErrorSubscriber(JSONObject response, String messageBody, Integer sessionId, UserInfo userInfo) throws Exception {
            TransmitMessage message;
            
            if (response.getString(ERROR_CODE).equals(PropertiesLoader.getValue(ICAP_SUBSCRIBER_NOT_FOUND_ERROR_CODE))) {
                message = processInactiveSubscriber(messageBody, sessionId);
            } else {
                message = notifyUser(sessionId, userInfo, messageBody, STATUS_ICAP_FAILURE, userInfo.getLangCode(), true);
            }

            return message;
        }
        
        private TransmitMessage processInactiveSubscriber(String messageBody, Integer sessionId) throws Exception {
            // Process the user's menu selection for inactive subscribers
            return processNewUserJ4UmenuSelection(messageBody, sessionId);
        }
        
        private TransmitMessage processActiveSubscriber(JSONObject response, String messageBody, String msisdn, Integer sessionId, UserInfo userInfo) throws Exception {
            String paymentMethod = response.has(ICAP_OFFER_PAYMENT_METHOD) ? response.getString(ICAP_OFFER_PAYMENT_METHOD) : "";
            TransmitMessage message;

            if (PREPAID.equalsIgnoreCase(paymentMethod) || HYBRID.equalsIgnoreCase(paymentMethod)) {
                J4UNewCustomerProfile newCustomerProfile = new J4UNewCustomerProfile();
                setNewCustomerInfo(newCustomerProfile, response);
                updaterDao.insertNewUserInfo(newCustomerProfile);
                userInfo.setLangCode(""+newCustomerProfile.getLangCode());
                message = processMLTownSubMenuRequest(messageBody, sessionId, userInfo);
            } else {
            	// Process the user's menu selection for postpaid subscribers
				message = processNewUserJ4UmenuSelection(messageBody, sessionId);
            }
            
            return message;
        }

		private void setNewCustomerInfo(J4UNewCustomerProfile newCustomerProfile, JSONObject response) {
			newCustomerProfile.setMsisdn(response.getString(ICAP_MSISDN));
			if (response.has(ICAP_LANGUAGE_CATEGORY)) {
				newCustomerProfile.setLangCategory(response.getString(ICAP_LANGUAGE_CATEGORY));
				newCustomerProfile.setLangCode(Integer.parseInt(icapLangCodeMap.get(response.getString(ICAP_LANGUAGE_CATEGORY))));
			} else {
				newCustomerProfile.setLangCategory(FRENCH);
				newCustomerProfile.setLangCode(ONE);
			}
			newCustomerProfile.setSubscriberState(response.getString(ICAP_SUBSCRIBER_STATE));
			newCustomerProfile.setPaymentMethod(response.has(ICAP_PAYMENT_METHOD) ? response.getString(ICAP_PAYMENT_METHOD) : "NA");
			newCustomerProfile.setOfferPaymentMethod(response.getString(ICAP_OFFER_PAYMENT_METHOD));
			newCustomerProfile.setSubscriberStartDate(response.has(ICAP_SUBSCRIBER_START_DATE) ? response.getString(ICAP_SUBSCRIBER_START_DATE) : "NA");
			newCustomerProfile.setDate(getCurrentTimeStamp());
		}


		private TransmitMessage processMLMorningOfferMenu(String messageBody, Integer sessionId) throws Exception {
        	TransmitMessage request = null;
        	LOG.info("User at Menu Level Two!!");        	
        	UserInfo menuLevelTwoInfo = menuLevelTwo.get(sessionId);
        	menuLevelTwoInfo.setSelectionProdOption(Integer.parseInt(messageBody));
        	LOG.debug("menuLevelTwoInfo-UserSelection Product Option - " + menuLevelTwoInfo.getSelectionProdOption());

        	if(checkMorningEligibility.test(menuLevelTwoInfo)) {
        		LOG.info("Morning Offer check Done!!");
        		LOG.info("Morning User msisdn: " + menuLevelTwoInfo.getMsisdn());
        		request = activateMLMorningRequest(sessionId, messageBody, menuLevelTwoInfo);
        	} else {
        		//Customer not eligible or doesn't fall in window timing
        		request = morningFailureMsgRequest(sessionId, messageBody, menuLevelTwoInfo);
        	}
        	return request;
        }         
     
        public TransmitMessage processMLOfferMenu(String messageBody, Integer sessionId) throws Exception {
            // ML flow provison the reward and notify the user

            TransmitMessage request;
            UserInfo menuLevelTwoInfo = menuLevelTwo.get(sessionId);

            String msisdn = menuLevelTwoInfo.getMsisdn();

            boolean isMpesaUser = menuLevelTwoInfo.isMpesaUser() && isMpesaEnabled;

            // Sending request to MPesa for amount detection.
            if (loanMenu.containsKey(sessionId) && msisdn.equalsIgnoreCase(loanMenu.get(sessionId).getMsisdn())) {
                request = processLoanMenu(messageBody, sessionId);
            } else {
                LOG.debug("check 2 - ");
                if (isMpesaUser && paymentMenu.containsKey(sessionId) && currencyMenu.containsKey(sessionId)) {
                    request = processPaymentCurrencyMenu(messageBody, sessionId);
                } else if (isMpesaUser && paymentMenu.containsKey(sessionId)) {
                    request = processPaymentMenu(messageBody, sessionId);
                } else if (isMpesaUser) {
                    request = processUserMenu(messageBody, sessionId, menuLevelTwoInfo);
                } else {
                    request = processProvisionRewardAndNotifyUser(messageBody, sessionId);
                }
            }

            return request;
        }

        private TransmitMessage processLoanMenu(String messageBody, int sessionId) throws Exception {
            TransmitMessage request;
            LOG.debug("check 1 - processLoanMenu");
            UserInfo loanMenuInfo = loanMenu.get(sessionId);
            if (messageBody.equalsIgnoreCase(USER_SEL_1)) {
                request = provisionRewardAndNotifyUser(sessionId, messageBody, loanMenuInfo, true);
            } else {
                request = sendWrongInputErrorNotification(sessionId, loanMenuInfo, messageBody, STATUS_INVALID_REQUEST, ML_J4U_LOAN_MENU_WRONG_SELECTION);
                clearMenu(ML_SUB_MENU, sessionId);
            }
            return request;
        }

        private TransmitMessage processProvisionRewardAndNotifyUser(String messageBody, int sessionId) throws Exception {
            TransmitMessage request;
            LOG.debug("Send request to reward plugin for provisioning using airtime payment");
            
            AAMap aaMap = AAMap.instance();
            LOG.debug("check -1 - " + aaMap.get(sessionId));
            if (null != aaMap.get(sessionId)) {
                LOG.debug("check 0 - ");
                menuLevelTwo.put(sessionId, aaMap.get(sessionId));
                aaMap.remove(sessionId);
            }
            UserInfo menuLevelTwoInfoNew = menuLevelTwo.get(sessionId);
            menuLevelTwoInfoNew.setSelectionProdOption(Integer.parseInt(messageBody));
            request = provisionRewardAndNotifyUser(sessionId, messageBody, menuLevelTwoInfoNew, false);
            return request;
        }

        private TransmitMessage processUserMenu(String messageBody, int sessionId, UserInfo menuLevelTwoInfo) throws Exception {
            Integer selection = Integer.parseInt(messageBody);
            Integer productCount = lookUpDAO.getProductCountForSelection(menuLevelTwoInfo);
            LOG.debug("Total Product Count=> " + productCount);

            if (selection > productCount) {
                return sendWrongInputErrorNotification(sessionId, menuLevelTwoInfo, messageBody, STATUS_INVALID_REQUEST, ML_J4U_OFFER_WRONG_SELECTION);
            }
            LOG.debug("Sending Payment menu to user");
            // Sending Payment menu to user having mpesa account
            AAMap aaMap = AAMap.instance();
            LOG.debug("check -1 - " + aaMap.get(sessionId));
            if (null != aaMap.get(sessionId)) {
                LOG.debug("check 0 - ");
                menuLevelTwo.put(sessionId, aaMap.get(sessionId));
                aaMap.remove(sessionId);
            }
            UserInfo menuLevelTwoInfoNew = menuLevelTwo.get(sessionId);
            menuLevelTwoInfoNew.setSelectionProdOption(Integer.parseInt(messageBody));
            LOG.debug("menuLevelTwoInfoNew.getSelProdOption - " + menuLevelTwoInfoNew.getSelectionProdOption());
            TransmitMessage request = paymentMenuRequest(sessionId, messageBody, menuLevelTwoInfoNew);
            paymentMenu.put(sessionId, menuLevelTwoInfoNew);
            return request;
        }

        private TransmitMessage processPaymentMenu(String messageBody, Integer sessionId) throws Exception {

            TransmitMessage request;
            UserInfo paymentMenuInfo = paymentMenu.get(sessionId);
            LOG.debug("processPaymentMenu");
            // Sending Currency menu to user or Reward provision
            // Sending Currency menu
            if (messageBody.equalsIgnoreCase(USER_SEL_2)) {
            	LOG.debug("Customer has chosen M-Pesa account for payment :: messageBody ==> " + messageBody);
                request = currencyMenuRequest(sessionId, messageBody, paymentMenuInfo);
                currencyMenu.put(sessionId, paymentMenuInfo);
            } else if (messageBody.equalsIgnoreCase(USER_SEL_1)) {
                LOG.debug("Send request to reward plugin using airtime payment :: messageBody ==> " + messageBody);
                // Send request to reward plugin using airtime payment
                request = provisionRewardAndNotifyUser(sessionId, messageBody, paymentMenuInfo, false);
                // Removing from Both the sessions after provisioning
                // the
                // reward
            } else {
                LOG.debug("Wrong input selection :: messageBody ==> " + messageBody);
                request = sendWrongInputErrorNotification(sessionId, paymentMenuInfo, messageBody, STATUS_INVALID_REQUEST, ML_PAYMENT_MENU_WRONG_SELECTION);
            }
            return request;
        }

        private TransmitMessage processPaymentCurrencyMenu(String messageBody, int sessionId) throws Exception {
            TransmitMessage request;
            UserInfo currencyMenuInfo = currencyMenu.get(sessionId);
            if (messageBody.equalsIgnoreCase(USER_SEL_2) || messageBody.equalsIgnoreCase(USER_SEL_1)) {
            	LOG.debug("processPaymentCurrencyMenu :: messageBody ==> " + messageBody);
                request = callMPesaAndNotifyUser(sessionId, messageBody, currencyMenuInfo);
            } else {
                LOG.debug("Invalid Request Input :: messageBody ==> " + messageBody);
                request = sendWrongInputErrorNotification(sessionId, currencyMenuInfo, messageBody, STATUS_INVALID_REQUEST, ML_CURRENCY_MENU_WRONG_SELECTION);
            }
            clearMenu(ML_OFFER_MENU, sessionId);
            return request;
        }
        
        private TransmitMessage processMyRewardsMenu(String messageBody, Integer sessionId) throws Exception {
        	
        	UserInfo myRewardsUserInfo = menuLevelOne.get(sessionId);        	
        	myRewardsMenu.put(sessionId, myRewardsUserInfo);
        	int langCode = myRewardsUserInfo.getLangCode();

        	TransmitMessage message = null;
        	messageListenerImpl.logInfo(sessionId, myRewardsUserInfo, messageBody, MY_REWARDS_MENU_REQUEST_RECEIVED);
        	InboundUssdMessage inboundUssdMessage = null;
        	if(checkMyRewardsAllmenu.test(myRewardsUserInfo)) {
        		inboundUssdMessage = messageListenerImpl.requestReceived(PropertiesLoader.getValue(USSD_ML_MY_REWARDS_ALL), langCode);
        	}else if(checkMyRewardsRPmenu.test(myRewardsUserInfo)){
        		inboundUssdMessage = messageListenerImpl.requestReceived(PropertiesLoader.getValue(USSD_ML_MY_REWARDS_RP), langCode);
        	}else if(checkMyRewardsRSmenu.test(myRewardsUserInfo)) {
        		inboundUssdMessage = messageListenerImpl.requestReceived(PropertiesLoader.getValue(USSD_ML_MY_REWARDS_RS), langCode);
        	}else if(checkMyRewardsPSmenu.test(myRewardsUserInfo)) {
        		inboundUssdMessage = messageListenerImpl.requestReceived(PropertiesLoader.getValue(USSD_ML_MY_REWARDS_PS), langCode);
        	}else if(checkMyRewardsRmenu.test(myRewardsUserInfo)) {
        		inboundUssdMessage = messageListenerImpl.requestReceived(PropertiesLoader.getValue(USSD_ML_MY_REWARDS_R), langCode);
        	}else if(checkMyRewardsPmenu.test(myRewardsUserInfo)) {
        		inboundUssdMessage = messageListenerImpl.requestReceived(PropertiesLoader.getValue(USSD_ML_MY_REWARDS_P), langCode);
        	}else if(checkMyRewardsSmenu.test(myRewardsUserInfo)) {
        		inboundUssdMessage = messageListenerImpl.requestReceived(PropertiesLoader.getValue(USSD_ML_MY_REWARDS_S), langCode);
        	}else {
                 //USSD_ML_SAG_INELIGIBLE_MENU
        		inboundUssdMessage = messageListenerImpl.userInEligible(langCode);        		
        	}

        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
        		message.setDataCoding(ONE);
        		message.setMessageEncoding(ONE);
        		message.setSourceAddress(myRewardsUserInfo.getDestAddress());
        		message.setDestinationAddress(myRewardsUserInfo.getMsisdn());
        		message.setMessageText(inboundUssdMessage.getClobString());
        		message.setTransactionId(myRewardsUserInfo.getTxId());
        		message.setServiceOp(TWO);
        		message.setReferenceNumber(sessionId);
        		message.setMlFlag(myRewardsUserInfo.isMlFlag());
        		message.setRandomFlag(myRewardsUserInfo.isRandomFlag());
        		message.setLogStatus(MY_REWARDS_MENU_RESPONSE_SENT);
        	}
        	return message; 	        	
        	
        }
        

        private boolean checkValidSubmenuItem (String messageBody) {
        	
        	boolean bValid = false ;
        	
        	if ( USER_SEL_1.equals(messageBody) || USER_SEL_2.equals(messageBody) || 
        	    USER_SEL_3.equals(messageBody) || USER_SEL_4.equals(messageBody) ){
        		bValid = true ;
        	}
        	
        	return bValid ;
        }         
       
     // Please do not change the order of If - else blocks
        private String getMenuOption(Integer sessionId, String msisdn, UserInfo userInfo, String messageBody) throws Exception {
        	LOG.info(" Inside getMenuOption method. Value of isSocialEnabled:  "+ isSocialEnabled);

        	boolean bIsMainMenuSegments = checkMainMenuSegments.test(userInfo, messageBody);
        	boolean bIsJ4UTown = checkJ4UTownMenu.test(messageBody);
        	boolean bIsMorning = checkMorningMenu.test(userInfo, messageBody);
        	boolean bIsNewCustMorningMenuNotEligible = checkNewCustMorningMenuNotEligible.test(userInfo, messageBody);
        	boolean bIsSocial = checkSocialMenu.test(messageBody) ; 
        	boolean bIsMyRewards = checkMyRewardsMenu.test(messageBody); 
        	boolean bIsConsentOptOut = consentOptOut.test(userInfo,messageBody);
        	boolean bIsNonMlConsentOptOut = nonMLConsentOptOut.test(userInfo,messageBody);
        	boolean bValidSubmenu = (checkValidSubmenuItem(messageBody) || bIsSocial) || (null != menuLevelOne.get(sessionId) 
        			&& menuLevelOne.get(sessionId).isJ4uNewUser() && messageBody.equals(USER_SEL_0));


        	boolean bConsentMenuContains = consentMenuBody.containsKey(sessionId);

        	if (bConsentMenuContains && USER_SEL_2.equals(messageBody)) {        		
        		return CONSENT_DENIED;
        	} else if(bConsentMenuContains && !USER_SEL_1.equals(messageBody) && !USER_SEL_2.equals(messageBody)) {
        		return INVALID_MENU_SELECTION;
        	} else if(consentOptOutMenu.containsKey(sessionId)) {
        		return CONSENT_OPT_OUT;
        	} else if(null != menuLevelOne.get(sessionId) && menuLevelOne.get(sessionId).isJ4uNewUser() && !messageBody.equals(USER_SEL_0) && !messageBody.equals(USER_SEL_5)) {
        		if(checkJ4UNewUserValidSelection.test(messageBody)) {
        			return NEW_USER_INVALID_MENU_SELECTION;
        		}else {
        			return INVALID_MENU_SELECTION;
        		}
        	} else if (null != menuLevelOne.get(sessionId) && !menuLevelOne.get(sessionId).isMlFlag() && !messageBody.equals(USER_SEL_0) ) {
        		if(menuLevelOne.get(sessionId).getOfferCount() == ZERO) {
        			return INVALID_MENU_SELECTION ;
        		}else if(bIsNonMlConsentOptOut) {
        			return CONSENT_OPT_OUT_MENU;    
        		}else { 
        			return J4U_MENU;
        		}
        	} else if (pedViewPlayMenu.containsKey(sessionId) || pedPlayMenu.containsKey(sessionId) || pedHistoryMenu.containsKey(sessionId)) {
        		return PED_OFFER_MENU;
        	} else if (pedSubMenu.containsKey(sessionId)) {
        		if (messageBody.equals(USER_SEL_1)) {
        			return PED_AVAIL_PLAY_MENU;
        		} else if (messageBody.equals(USER_SEL_2)) {
        			return PED_PLAY_MENU;
        		}else if(messageBody.equals(USER_SEL_3)) {
        			return PED_HISTORY_MENU;
        		} else {
        			return PED_SUB_MENU;
        		}
        	} 

        	//MyRewards Menu, 1)RAG 2)PED 3)SAG
        	//MyRewards Menu -> RAG_MAIN_MENU, PED_SUB_MENU, SAG_MAIN_MENU

        	else if (!menuLevelTwo.containsKey(sessionId) && !menuLevelOne.containsKey(sessionId) 
        			&& myRewardsMenu.containsKey(sessionId) ) {
        		if(checkRAGMenuSelection.test(messageBody, myRewardsMenu.get(sessionId)) ) {
        			return RAG_MAIN_MENU;
        		} else if(checkPEDMenuSelection.test(messageBody, myRewardsMenu.get(sessionId))) {
        			return PED_SUB_MENU;
        		} else if(checkSAGMenuSelection.test(messageBody, myRewardsMenu.get(sessionId))) {
        			return SAG_MAIN_MENU;
        		}

        	}        	

        	/* User will be in ragMainMenu and ragSubMenu. We will display offer
        	 * info and back option when customer selects option 1
        	 * !messageBody.equals(ragMenuSelection) &&
        	 */
        	else if (ragMainMenu.containsKey(sessionId) && ragSubMenu.containsKey(sessionId)) {
        		return RAG_OFFER_MENU;
        	}

        	/*
        	 * User will be in ragMainMenu and user will not be in ragSubMenu.
        	 * We will display offer info and opt-out option
        	 * !messageBody.equals(ragMenuSelection) &&
        	 */
        	else if (ragMainMenu.containsKey(sessionId) && !ragSubMenu.containsKey(sessionId)) {
        		return RAG_SUB_MENU;
        	}  

        	/*
        	 * User will be in sagMainMenu but not in sagSubMenu.
        	 * We will display offer info and opt-out option
        	 * !messageBody.equals(sagMenuSelection) &&
        	 */
        	else if (sagMainMenu.containsKey(sessionId) && !sagSubMenu.containsKey(sessionId) && checkSagSubMenuSelection.test(messageBody) ) {
        		return SAG_SUB_MENU;
        	} 

        	/* User will be in sagMainMenu and sagSubMenu. We will display offer
        	 * info and back option when customer selects option 1
        	 * !messageBody.equals(sagMenuSelection) &&
        	 */
        	else if (sagMainMenu.containsKey(sessionId) && sagSubMenu.containsKey(sessionId) && USER_SEL_1.equals(messageBody)) {
        		return SAG_OFFER_MENU;
        	}        	

        	/*
        	 * User will be in menuLevelTwo only after we showing static J4U
        	 * main menu and
        	 * submenu (Morning/Voice/Data/Integrated/Hourly/Social)
        	 */  	

        	else if (menuLevelTwo.containsKey(sessionId) && msisdn.equalsIgnoreCase(menuLevelTwo.get(sessionId).getMsisdn()) ) {
        		if( menuLevelTwo.get(sessionId).getMessageBody().equalsIgnoreCase(USER_SEL_0) && checkValidSubmenu.test(menuLevelTwo.get(sessionId).getOfferCount(), messageBody)) {
        			return MORNING_OFFER_MENU;
        		} else if (checkValidSubmenu.test(menuLevelTwo.get(sessionId).getOfferCount(), messageBody)) {
        			return ML_OFFER_MENU;
        		}   
        	}

        	/*
        	 * User will be in menuLevelOne only after we showing static J4U
        	 * main menu
        	 */


        	else if (menuLevelOne.containsKey(sessionId) && msisdn.equalsIgnoreCase(menuLevelOne.get(sessionId).getMsisdn()) 
        			&& (bValidSubmenu || bIsMorning || bIsMyRewards || bIsConsentOptOut || bIsJ4UTown)) {
        		if(bIsMorning) {
        			return MORNING_SUB_MENU;
        		} else if(bIsJ4UTown){
        			return J4U_TOWN_SUB_MENU;
        		} else if(bIsSocial){
        			return ML_SOCIAL_SUB_MENU;
        		} else if(bIsMyRewards) {
        			return MY_REWARDS_MENU;
        		} else if(bIsConsentOptOut) {
        			return CONSENT_OPT_OUT_MENU;  
        		}else if (bIsNewCustMorningMenuNotEligible) {
        			return INVALID_MENU_SELECTION;
        		} else {
        			return ML_SUB_MENU;
        		}
        	} else if(menuLevelOne.containsKey(sessionId) && msisdn.equalsIgnoreCase(menuLevelOne.get(sessionId).getMsisdn()) 
        			&& bIsMainMenuSegments) {
        		if(messageBody.equalsIgnoreCase(menuLevelOne.get(sessionId).getMessageBody())) {
        			return INVALID_MENU_SELECTION;
        		}
        		menuLevelOne.get(sessionId).setMessageBody(messageBody);
        		return MAINMENUSEGMENTS;
        	}

        	/*
        	 * Traditional J4U menu
        	 */

        	else { 
        		return INVALID_MENU_SELECTION;
        	}
        	return INVALID_MENU_SELECTION ;
        }


        private void clearMenu(String option, Integer sessionId) {
        	LOG.debug("clearMenu option :: " + option);
            switch (option) {
	            case PED_BACK_MENU:
	            	clearPedBackMenus(sessionId);
	                break;
	            case PED_OFFER_MENU:
	            	clearPedOfferMenus(sessionId);
	                break;
	            case ML_OFFER_MENU:
	            case ML_SUB_MENU:
	            case MORNING_OFFER_MENU:
	            case ML_SOCIAL_SUB_MENU:
	            	clearMenusForML(sessionId);
	                break;
	            case MY_REWARDS_MENU:
	            	myRewardsMenu.remove(sessionId);
	            	break;
	            case RAG_OFFER_MENU:
	            	clearRagOfferMenus(sessionId);
	            	break;
	        	case RAG_MENU:
	        		clearRagMenus(sessionId);
	        		break;
	        	case RAG_SUB_MENU:
	        		ragSubMenu.remove(sessionId);
	        		break;
	
	        	case SAG_SUB_MENU:
	        		sagSubMenu.remove(sessionId);
	        		break;            	
	        	case SAG_MENU:
	        		clearSagMenus(sessionId);
	        		break;	
	            case HOURLY_SUB_MENU:
	            case MORNING_SUB_MENU:
				case MY_REWARDS:
				case J4U_TOWN_SUB_MENU:
	                menuLevelOne.remove(sessionId);
	                break;     
				case CLEAR_CONSENT_MENU:
					clearConsentMenus(sessionId);
					break;
				case CLEAR_USERINFO_MAP:
					userInfoMap.remove(sessionId);
					break;
				default :
					break;
            }
        }
        
        private void clearPedBackMenus(Integer sessionId) {
        	pedHistoryMenu.remove(sessionId);
        	pedPlayMenu.remove(sessionId);
        	pedViewPlayMenu.remove(sessionId);
        }
        private void clearPedOfferMenus(Integer sessionId) {
        	menuLevelOne.remove(sessionId);
        	pedHistoryMenu.remove(sessionId);
        	pedPlayMenu.remove(sessionId);
        	pedViewPlayMenu.remove(sessionId);
        	pedSubMenu.remove(sessionId);
        	pedNoPrizeMenu.remove(sessionId);
        }
        
        private void clearMenusForML(Integer sessionId) {
            menuLevelOne.remove(sessionId);
            menuLevelTwo.remove(sessionId);
            paymentMenu.remove(sessionId);
            currencyMenu.remove(sessionId);
            loanMenu.remove(sessionId);
            AAMap.instance().remove(sessionId);
        }
        
        private void clearRagOfferMenus(Integer sessionId) {
        	menuLevelTwo.remove(sessionId);
        	ragMainMenu.remove(sessionId);
        	ragSubMenu.remove(sessionId);
        }
        
        private void clearRagMenus(Integer sessionId) {
        	ragMainMenu.remove(sessionId);
        	ragSubMenu.remove(sessionId);
        	menuLevelOne.remove(sessionId);
        	menuLevelTwo.remove(sessionId);
        }

        private void clearSagMenus(Integer sessionId) {
            sagMainMenu.remove(sessionId);
            sagSubMenu.remove(sessionId);
            menuLevelOne.remove(sessionId);
        }

        private void clearConsentMenus(Integer sessionId) {
        	consentMenu.remove(sessionId);
			consentMenuBody.remove(sessionId);
			menuLevelOne.remove(sessionId);
			consentOptOutMenu.remove(sessionId);
        }

        private TransmitMessage paymentMenuRequest(Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {
            TransmitMessage message = null;
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, PAYMENT_MENU_REQUEST_RECEIVED);
            LOG.debug("templated Id :: " + PropertiesLoader.getValue(USSD_ML_PAYMENT_MENU_TEMPLATE));
            InboundUssdMessage inboundUssdMessage = messageListenerImpl.paymentMenuReqReceived(PropertiesLoader.getValue(USSD_ML_PAYMENT_MENU_TEMPLATE), userInfo.getLangCode());
            if (inboundUssdMessage != null) {
                message = new TransmitMessage();
                message.setDataCoding(ONE);
                message.setMessageEncoding(ONE);
                message.setSourceAddress(userInfo.getDestAddress());
                message.setDestinationAddress(userInfo.getMsisdn());
                message.setMessageText(inboundUssdMessage.getClobString());
                message.setTransactionId(userInfo.getTxId());
                message.setServiceOp(TWO);
                message.setReferenceNumber(sessionId);
                message.setMlFlag(userInfo.isMlFlag());
                message.setRandomFlag(userInfo.isRandomFlag());
                message.setCellId(userInfo.getCellId());
                message.setPoolId(userInfo.getPoolId());
                message.setTownName(userInfo.getTownName());
                // setting 2 for ussd log table status
                message.setUssdLogStatus(TWO);
                message.setLogStatus(PAYMENT_MENU_RESPONSE_SENT);
            }

            return message;
        }

        private TransmitMessage currencyMenuRequest(Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {
            TransmitMessage message = null;
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, CURRENCY_MENU_REQUEST_RECEIVED);
            InboundUssdMessage inboundUssdMessage = messageListenerImpl.currencyMenuReqReceived(PropertiesLoader.getValue(USSD_ML_CURRENCY_MENU_TEMPLATE), userInfo.getLangCode());
            if (null != inboundUssdMessage) {
                message = new TransmitMessage();
                message.setDataCoding(ONE);
                message.setMessageEncoding(ONE);
                message.setSourceAddress(userInfo.getDestAddress());
                message.setDestinationAddress(userInfo.getMsisdn());
                message.setMessageText(inboundUssdMessage.getClobString());
                message.setTransactionId(userInfo.getTxId());
                message.setServiceOp(TWO);
                message.setReferenceNumber(sessionId);
                message.setMlFlag(userInfo.isMlFlag());
                message.setRandomFlag(userInfo.isRandomFlag());
                message.setCellId(userInfo.getCellId());
                message.setPoolId(userInfo.getPoolId());
                message.setTownName(userInfo.getTownName());
                // setting 2 for ussd log table status
                message.setUssdLogStatus(TWO);
                message.setLogStatus(CURRENCY_MENU_RESPONSE_SENT);
            }

            return message;
        }        

        private void generateUserBalanceReq(UserInfo userInfo, Integer refreshOffers) throws Exception {
        	JSONObject userBalJson = new JSONObject();
        	LOG.info("Asking user balance from OCS for the msisdn = " + userInfo.getMsisdn());
        	userBalJson.put(REFRESH_RF_VALUE, refreshOffers);
        	userBalJson.put(MSISDN, userInfo.getMsisdn());
        	userBalJson.put(USER_MSG_REF, userInfo.getUserMsgRef());
        	userBalJson.put(TRANSACTION_ID, userInfo.getTxId());
        	userBalJson.put(DEST_ADDRESS, userInfo.getDestAddress());
        	userBalJson.put(SHORT_MSG, userInfo.getMessageBody());
        	userBalJson.put(PRODUCT_TYPE, null != mlSubMenuProdTypeMenu.get(userInfo.getMessageBody()) ? mlSubMenuProdTypeMenu.get(userInfo.getMessageBody()) : MAIN_MENU);
        	userBalJson.put(LANG_CODE, userInfo.getLangCode());
        	userBalJson.put(REQUEST_STARTTIME, System.currentTimeMillis());
        	userBalJson.put(OFFER_REFRESH_FLAG, userInfo.getOfferRefreshFlag());
        	// send the query parameter for AA Eligibility
        	userBalJson.put(AA_ELIGIBLE, userInfo.getAaEligible());
        	userBalJson.put(PREF_PAY_METHOD, userInfo.getPrefPayMethod());
        	userBalJson.put(A_VALUE, Float.toString(userInfo.getaValue()));            
        	userBalJson.put(CELL_ID_KEY_NAME, null == userInfo.getCellId() ? ZERO_0 : userInfo.getCellId());
        	userBalJson.put(LOCATION_NUMBER_KEY, null == userInfo.getLocationNumber() ? "" : userInfo.getLocationNumber());
        	userBalJson.put(POOL_ID, null == userInfo.getPoolId() ? "" : userInfo.getPoolId());
        	LOG.info("cellId=>" + userInfo.getCellId());
        	LOG.info("LocationNumber=>" + userInfo.getLocationNumber());
        	UssdEventPublisher ussdEventPublisher = new UssdEventPublisher();

        	LOG.info("The Query Balance request JSON => :: " + userBalJson);

        	if(userInfo.getPrefPayMethod().equalsIgnoreCase(PREF_PAY_MET_MPESA) && !userBalJson.getString(PRODUCT_TYPE).equals(MAIN_MENU) && refreshOffers != ZERO) {
        		ussdEventPublisher.addEvent(PropertiesLoader.getValue(MPESA_QUERY_BALANCE_TOPIC), userBalJson.toString());
        		LOG.info("userBalance JSON Published to kafka MPESA_QUERY_BALANCE_PLUGIN_TOPIC  :"+ 
        				PropertiesLoader.getValue(MPESA_QUERY_BALANCE_TOPIC) +" :: "+ userBalJson);
        	}else {
        		if (PropertiesLoader.getIntValue(USSD_ENABLE_MULTI_CS) == ONE) {

        			String csType = lookUpDAO.getCSType(userInfo.getMsisdn());
        			if (csType.equalsIgnoreCase(CS_TYPE_OP)) {
        				ussdEventPublisher.addEvent(PropertiesLoader.getValue(CCS_QUERY_BAL_TOPIC), userBalJson.toString());
        				LOG.info("userBalJson Published / added to kafka CCS_QUERY_BAL_TOPIC  :: " + userBalJson);
        			} else {
        				ussdEventPublisher.addEvent(PropertiesLoader.getValue(OCS_QUERY_BAL_TOPIC), userBalJson.toString());
        				LOG.info("userBalJson Published / added to kafka OCS_QUERY_BAL_TOPIC  :: " + userBalJson);
        			}
        		} else {
        			ussdEventPublisher.addEvent(PropertiesLoader.getValue(OCS_QUERY_BAL_TOPIC), userBalJson.toString());
        			LOG.info("userBalJson Published / added to kafka OCS_QUERY_BAL_TOPIC  :: " + userBalJson);
        		}
        	}

        }

        private UssdMessage getUSSDMessageModel(DeliverSM deliverSM, String messageBody, Integer sessionId) throws Exception {
            UssdMessage ussdMessage = new UssdMessage();
            ussdMessage.setSessionId(sessionId);
            ussdMessage.setSourceAddress(deliverSM.getSource().getAddress());
            ussdMessage.setDestinationAddress(deliverSM.getDestination().getAddress());
            ussdMessage.setMessageText(messageBody);
            ussdMessage.setDeliverSM(deliverSM);
            return ussdMessage;
        }

        private UserInfo getJ4UnewCustomerInfo(DeliverSM deliverSM, String messageBody, Integer sessionId, String strCellID, String strLocationNumber) throws Exception {
            LOG.debug("getJ4UnewCustomerInfo");
            UserInfo newUserInfo = new UserInfo();
            newUserInfo.setUserMsgRef(sessionId);     
        	newUserInfo.setMsisdn(deliverSM.getSource().getAddress());
        	newUserInfo.setDestAddress(deliverSM.getDestination().getAddress());
        	newUserInfo.setMessageBody(messageBody);
        	newUserInfo.setTxId(new Utils().getTransactionID());
        	newUserInfo.setLangCode(ONE_1);
        	newUserInfo.setJFUEligible(true);
        	newUserInfo.setMlFlag(true);
        	newUserInfo.setJ4uNewUser(true);   
        	newUserInfo.setOfferCount(MAX_PRODIDS_CNT_3);
        	newUserInfo.setAaEligible(ZERO_0);
        	newUserInfo.setPrefPayMethod(PREF_PAY_METHOD_G);
        	newUserInfo.setaValue(ZERO);
        	newUserInfo.setOfferRefreshFlag(VALUE_G);
        	newUserInfo.setCellId(strCellID);
        	newUserInfo.setLocationNumber(strLocationNumber);

			if (null != newUserInfo.getCellId()) {
				CellIDPoolIDCache cellIDPoolIDCache = CellIDPoolIDCache.instance() ;
				String poolID = cellIDPoolIDCache.getPoolIDForCellID(strCellID);
				LOG.debug("Pool ID received is ==>" + poolID);
				newUserInfo.setPoolId(poolID);
			}
            LOG.debug("getJ4UnewCustomerInfo method ends");        	
            return newUserInfo;
        }

        private TransmitMessage notifyUser(int sessionId, UserInfo userInfo, String messageBody, String userStatus, int langCode, boolean isJFUEligible) throws Exception {
        	String msisdn = userInfo.getMsisdn();
        	LOG.info(msisdn + " :: " + userStatus);
        	TransmitMessage message = null;
        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, userStatus);
        	InboundUssdMessage inboundUssdMessage = isJFUEligible ? messageListenerImpl.getNotEnoughProdsMsg(msisdn, langCode) : messageListenerImpl.userInEligible(langCode);
        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
        		message.setSourceAddress(userInfo.getDestAddress());
        		message.setDestinationAddress(msisdn);
        		message.setMessageText(inboundUssdMessage.getClobString());
        		message.setTransactionId(userInfo.getTxId());
        		message.setServiceOp(SEVENTEEN);
        		message.setReferenceNumber(sessionId);
        		message.setMlFlag(userInfo.isMlFlag());
        		message.setRandomFlag(userInfo.isRandomFlag());
        		message.setCellId(userInfo.getCellId());
        		message.setPoolId(userInfo.getPoolId());
        		message.setTownName(userInfo.getTownName());
        	}
        	return message;
        }

        private TransmitMessage processBlacklistedUser(UserInfo userInfo, String messageBody, int sessionId,
        		String msisdn) throws Exception {

        	LOG.info("processBlacklistedUser, msisdn :: " + msisdn);
        	TransmitMessage message = null;
        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, STATUS_REQUEST_RECEIVED);       	
        
        	InboundUssdMessage inboundUssdMessage = messageListenerImpl.getUserTemplate
        			(PropertiesLoader.getValue(USSD_ML_BLACKLIST_MSISDN),userInfo.getLangCode());
        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
        		message.setSourceAddress(userInfo.getDestAddress());
        		message.setDestinationAddress(msisdn);
        		message.setMessageText(inboundUssdMessage.getClobString());
        		message.setTransactionId(userInfo.getTxId());
        		message.setServiceOp(SEVENTEEN);
        		message.setReferenceNumber(sessionId);
        		message.setUssdLogStatus(TWO);
    			message.setLogStatus(STATUS_BLACKLIST_FINAL_RESPONSE_SENT);
        	}
        	return message;
        }
        
        private TransmitMessage processNewUserJ4UmenuSelection(String messageBody,int sessionId) throws Exception {
        	
        	UserInfo menuLevelOneInfo = menuLevelOne.get(sessionId);
        	String msisdn = menuLevelOneInfo.getMsisdn();
            LOG.info(msisdn + " :: " + STATUS_USER_INELIGIBLE);
            TransmitMessage message = null;
            messageListenerImpl.logInfo(sessionId, menuLevelOneInfo, messageBody, STATUS_USER_INELIGIBLE);
            InboundUssdMessage inboundUssdMessage = messageListenerImpl.userInEligible(menuLevelOneInfo.getLangCode());
            if (null != inboundUssdMessage) {
                message = new TransmitMessage();
                message.setSourceAddress(menuLevelOneInfo.getDestAddress());
                message.setDestinationAddress(msisdn);
                message.setMessageText(inboundUssdMessage.getClobString());
                message.setTransactionId(menuLevelOneInfo.getTxId());
                message.setServiceOp(SEVENTEEN);
                message.setReferenceNumber(sessionId);
                message.setMlFlag(menuLevelOneInfo.isMlFlag());
                message.setRandomFlag(menuLevelOneInfo.isRandomFlag());
                message.setCellId(menuLevelOneInfo.getCellId());
                message.setPoolId(menuLevelOneInfo.getPoolId());        
            }
            return message;
        }
        
        private TransmitMessage processJ4UnewUserMenu(UserInfo newUserInfo, Integer sessionId,String messageBody, 
        		String msisdn) throws Exception {
        	TransmitMessage message = null;

        	if (isBlacklistEnabled && lookUpDAO.getBlacklistedMsisdn(msisdn)) {

        		LOG.info("New Customer-Blacklisted Msisdn :: "+ msisdn +", TransactionID: "+newUserInfo.getTxId());				
        		message = processBlacklistedUser(newUserInfo, messageBody, sessionId, msisdn);
        	}else {
        		ProductInfoCache townProductInfoCache = ProductInfoCache.instance(); 
    	    	TownProdInfo townInfo = townProductInfoCache.getCellIdTownName(newUserInfo.getCellId());
    	    	if (townInfo != null && townInfo.getTownName() != null) {
    	            newUserInfo.setTownName(townInfo.getTownName());
    	        }
        		//Get the morningOffers which is not activated by the customer
        		List<String> prodIds = lookUpDAO.getMorningOfferWhiteList(newUserInfo.getMsisdn());
        		if (!prodIds.isEmpty() || null != newUserInfo.getTownName()) {
        			newUserInfo.setMorningOfferFlag(!prodIds.isEmpty());
	        		UserInfoMapCache userInfoMapCache = UserInfoMapCache.instance();
	        		userInfoMapCache.put(sessionId, newUserInfo);
	        		messageListenerImpl.logInfo(sessionId, newUserInfo, messageBody, STATUS_REQUEST_RECEIVED);
	        		generateUserBalanceReq(newUserInfo, ZERO);
	        		menuLevelOne.put(sessionId, newUserInfo);
        		}else {
					message= notifyUser(sessionId, newUserInfo, messageBody, STATUS_USER_INELIGIBLE, newUserInfo.getLangCode(), true);
        		}  
        	}

        	return message;
        } 

        private TransmitMessage provisionRewardAndNotifyUser(Integer sessionId, String messageBody, UserInfo userInfo, boolean provFlag) throws Exception {
            TransmitMessage message = null;
            LOG.debug("provisionRewardAndNotifyUser - ");

            // Sending false for non - MPesa request
            // add other details for the request

            boolean loanCheckFlag = false;
            boolean notEnoughBal = false;

            // present a ussd option for the user with option to continue with
            // the loan amount or simply exit
            // check for the eligibility for the AA
            // 1. check whether the offer value is > balance
            // 2. check whether the user is eligible for advance airtime or not
            // 3. if yes then send a menu with option for the user to proceed
            // with the amount.
            // 4. If he selects 1 then proceed with the provision reward and
            // notify user, else just abort the process.

            if ((userInfo.getAaEligible() == ONE) && isAAEnabled && userInfo.getPrefPayMethod().equalsIgnoreCase(PREF_PAY_METHOD_G)) {
                loanCheckFlag = true;
            }
            LOG.debug("loanCheckFlag - " + loanCheckFlag +", provFlag: "+provFlag);
            int userSelection = userInfo.getSelectionProdOption();
            String productId = lookUpDAO.getProductForUserSelection(userInfo, userSelection);
            LOG.debug("User selected productId - " + productId);
            LOG.debug("productId.split(~).length - " + productId.split("~").length);

            if (loanCheckFlag && !provFlag) {
                LOG.debug("Customer is eligible for airtime loan.");
                
                Long offerPrice = null;
                if (productId.split("~").length == TWO) {
                    LOG.debug("Setting offer price for Random customer when  AA_ELIGIBLE = 1");
                    offerPrice = Long.parseLong(productId.split("~")[ONE]);
                    productId = productId.split("~")[ZERO];
                }
                Long advAirtimeAmount = userInfo.getAirtimeAdvBal();
                Long airBal = userInfo.getActBal();
                Long maxOfferPriceLimit = airBal + advAirtimeAmount;
                LOG.debug("advAirtimeAmount - " + advAirtimeAmount);
                LOG.debug("airBal - " + airBal);
                LOG.debug("maxOfferPriceLimit = airBal + advAirtimeAmount = " + maxOfferPriceLimit);
                LOG.debug("offerPrice - " + offerPrice);
                LOG.debug("productId - " + productId);
                LOG.debug("userSelection - " + userSelection);          

                if (null == offerPrice) {
                    LOG.debug("Setting offer price for Target customer when  AA_ELIGIBLE = 1");
                    LOG.debug("userInfo.getAaEligibleUserProdIds().size - " + userInfo.getAaEligibleProdIdProdPriceMap().size());
                    offerPrice = userInfo.getAaEligibleProdIdProdPriceMap().get(productId);
                    LOG.debug("offerPrice value: " + offerPrice);
                }

                if (offerPrice <= airBal) {
                    LOG.debug("offerPrice <= airBal");
                    message = new TransmitMessage();
                    provisionRewardFinalReq(sessionId, messageBody, userInfo, provFlag, message);
                } else if (offerPrice <= maxOfferPriceLimit) {
                    messageListenerImpl.logInfo(sessionId, userInfo, messageBody, STATUS_AA_LOAN_MENU_REQUEST_RECEIVED);
                    LOG.debug("offerPrice <= maxOfferPriceLimit");
                    // Loan Flow
                    InboundMessageService inboundMessageService = new InboundMessageService();
                    InboundUssdMessage inboundUSSDMsg = null;
                    Long loanAmount = offerPrice - airBal;
                    Long minLoanAmount = Long.parseLong(PropertiesLoader.getValue(USSD_MIN_LOAN_AMOUNT_CENTS));

                    LOG.debug("loanAmount 1 -" + loanAmount);
                    if (loanAmount < minLoanAmount) {
                        loanAmount = minLoanAmount;
                    }
                    LOG.debug("loanAmount 2 -" + loanAmount);
                    message = new TransmitMessage();
                    message.setSourceAddress(userInfo.getDestAddress());
                    message.setDestinationAddress(userInfo.getMsisdn());
                    TemplateDTO templateDTO = inboundMessageService.getLoanMenuTemplate(userInfo.getLangCode());
                    Float multiplier1 = Float.parseFloat(PropertiesLoader.getValue(USSD_CURRECY_MULTIPLIER_1));
                    double loanAmountDisp = Math.ceil((double) loanAmount * (double) multiplier1 * 100) / 100;
                    inboundUSSDMsg = inboundMessageService.generateLoanMenu(String.valueOf(loanAmountDisp), templateDTO);
                    LOG.debug("inboundUSSDMsg.getClobString()-" + inboundUSSDMsg.getClobString());
                    message.setMessageText(inboundUSSDMsg.getClobString());
                    message.setTransactionId(userInfo.getTxId());
                    message.setReferenceNumber(userInfo.getUserMsgRef());
                    message.setServiceOp(TWO);
                    message.setMlFlag(userInfo.isMlFlag());
                    message.setRandomFlag(userInfo.isRandomFlag());
                    message.setCellId(userInfo.getCellId());
                    message.setPoolId(userInfo.getPoolId());
                    message.setTownName(userInfo.getTownName());
                    message.setUssdLogStatus(TWO);
                    message.setLogStatus(STATUS_AA_LOAN_MENU_RESPONSE_SENT);
                    userInfo.setProvLoanAmt((long) (loanAmountDisp / multiplier1));
                    LOG.debug("ProvLoanAmt -" + userInfo.getProvLoanAmt());
                    loanMenu.put(sessionId, userInfo);
                } else {
                    notEnoughBal = true;
                }
            } else {
            	LOG.debug("Sending final Req for ML user :- " + userInfo.getMsisdn());
            	message = new TransmitMessage();
            	provisionRewardFinalReq(sessionId, messageBody, userInfo, provFlag, message);             
            }

            if (notEnoughBal) {
                LOG.debug("Don't have sufficient balance for this msisdn :- " + userInfo.getMsisdn());
                messageListenerImpl.logInfo(sessionId, userInfo, messageBody, FINAL_REQUEST_RECEIVED);
                InboundMessageService inboundMessageService = new InboundMessageService();
                InboundUssdMessage inboundUSSDMsg = null;
                TemplateDTO templateDTO = inboundMessageService.getNotEnoughBalanceTemplate(userInfo.getLangCode());
                inboundUSSDMsg = inboundMessageService.generateNotEnoughBalMessage(templateDTO);
                message = new TransmitMessage();
                message.setSourceAddress(userInfo.getDestAddress());
                message.setDestinationAddress(userInfo.getMsisdn());
                LOG.debug("inboundUSSDMsg.getClobString()-" + inboundUSSDMsg.getClobString());
                message.setMessageText(inboundUSSDMsg.getClobString());
                message.setTransactionId(userInfo.getTxId());
                message.setReferenceNumber(userInfo.getUserMsgRef());
                message.setServiceOp(SEVENTEEN);
                message.setMlFlag(userInfo.isMlFlag());
                message.setRandomFlag(userInfo.isRandomFlag());
                message.setCellId(userInfo.getCellId());
                message.setPoolId(userInfo.getPoolId());
                message.setTownName(userInfo.getTownName());
                message.setSelProdId(productId);
                clearMenu(ML_OFFER_MENU, sessionId);                
            }

            return message;
        }
        
        private void provisionRewardFinalReq(Integer sessionId, String messageBody, UserInfo userInfo, 
        		boolean provFlag, TransmitMessage message) throws Exception {
        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, FINAL_REQUEST_RECEIVED);
        	InboundUssdMessage inboundUssdMessage = messageListenerImpl.mlFinalReqReceived(messageBody, userInfo, false, provFlag);
        	if (null != inboundUssdMessage) {
        		message.setSourceAddress(userInfo.getDestAddress());
        		message.setDestinationAddress(userInfo.getMsisdn());
        		message.setMessageText(inboundUssdMessage.getClobString());
        		message.setTransactionId(userInfo.getTxId());
        		message.setReferenceNumber(userInfo.getUserMsgRef());
        		message.setServiceOp(SEVENTEEN);
        		message.setMlFlag(userInfo.isMlFlag());
        		message.setRandomFlag(userInfo.isRandomFlag());
        		message.setCellId(userInfo.getCellId());
        		message.setPoolId(userInfo.getPoolId());
        		message.setTownName(userInfo.getTownName());
        		message.setSelProdId(inboundUssdMessage.getSelProdId());
        		clearMenu(ML_OFFER_MENU, sessionId); 
        	}
        }

        private TransmitMessage callMPesaAndNotifyUser(Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {
            TransmitMessage message = null;
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, FINAL_REQUEST_RECEIVED);
            // Sending true for MPesa request
            InboundUssdMessage inboundUssdMessage = messageListenerImpl.mlFinalReqReceived(messageBody, userInfo, true, false);
            if (null != inboundUssdMessage) {
                message = new TransmitMessage();
                message.setSourceAddress(userInfo.getDestAddress());
                message.setDestinationAddress(userInfo.getMsisdn());
                message.setMessageText(inboundUssdMessage.getClobString());
                message.setTransactionId(userInfo.getTxId());
                message.setReferenceNumber(userInfo.getUserMsgRef());
                message.setServiceOp(SEVENTEEN);
                message.setMlFlag(userInfo.isMlFlag());
                message.setRandomFlag(userInfo.isRandomFlag());
                message.setCellId(userInfo.getCellId());
                message.setPoolId(userInfo.getPoolId());
                message.setTownName(userInfo.getTownName());
                message.setSelProdId(inboundUssdMessage.getSelProdId());
            }

            return message;
        }

        private TransmitMessage subMenuMLRequest(Integer sessionId, String messageBody, UserInfo userInfo, boolean isRandom) throws Exception {
            TransmitMessage message = null;
            InboundUssdMessage inboundUssdMessage = null;
            QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
            JSONObject queryBaljson = queryBaljsonMap.get(sessionId);
            if (isRandom) {
                inboundUssdMessage = messageListenerImpl.subMenuReqReceived(messageBody, userInfo);
            } else {
                inboundUssdMessage = messageListenerImpl.subMenuReqReceivedForTgtUser(messageBody, userInfo);
            }
            if (null != inboundUssdMessage) {
                if (inboundUssdMessage.getIncomingLabel() == PROD_IDS_NOT_FOUND) {
                	queryBaljsonMap.remove(queryBaljson.getInt(USER_MSG_REF));
                    return notifyUser(sessionId, userInfo, messageBody, STATUS_PROD_IDS_INSUF, userInfo.getLangCode(), true);
                    // sending default langCode french (2) and eligiblity (true)
                    // assumimg it's J4U eligible
                } else {
                	messageListenerImpl.getModifyTemplate(queryBaljson, userInfo.getLangCode(), inboundUssdMessage, false);
                    message = new TransmitMessage();
                    messageListenerImpl.getGenerateUssdResponse(message, userInfo, inboundUssdMessage, sessionId);
                    message.setUssdLogStatus(TWO);
                    message.setLogStatus(SUBMENU_RESPONSE_SENT);
                    // setting 2 for ussd log table status
                    message.setProdIds(inboundUssdMessage.getProdIds());
                    LOG.debug("inboundUssdMessage.getProdIds() - " + inboundUssdMessage.getProdIds());
                }
            }
            return message;
        }
        
        private TransmitMessage morningFailureMsgRequest(Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {

        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, MORNING_OFFER_FAILURE_MSG_REQUEST_RECIEVED);
        	TransmitMessage message = null;        	
        	InboundUssdMessage inboundUssdMessage = null;
        	inboundUssdMessage = messageListenerImpl.moFailureMsgReq(userInfo.getMsisdn(), userInfo.getLangCode());

        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
        		generateUssdFinalResponse(message, userInfo, inboundUssdMessage, sessionId);     		
        	}  	
        	return message;
        }
        
        public void generateUssdFinalResponse(TransmitMessage message, UserInfo userInfo, InboundUssdMessage inboundUssdMessage, Integer sessionId){
        	message.setSourceAddress(userInfo.getDestAddress());
        	message.setDestinationAddress(userInfo.getMsisdn());
        	message.setMessageText(inboundUssdMessage.getClobString());
        	message.setTransactionId(userInfo.getTxId());
        	message.setServiceOp(SEVENTEEN);
        	message.setReferenceNumber(sessionId);
        	message.setMlFlag(userInfo.isMlFlag());
        	message.setRandomFlag(userInfo.isRandomFlag());
        	message.setCellId(userInfo.getCellId());
        	message.setPoolId(userInfo.getPoolId());
        	message.setTownName(userInfo.getTownName());
        	message.setUssdLogStatus(TWO);
        	message.setLogStatus(STATUS_FINAL_RESPONSE_SENT);
        }

        private TransmitMessage activateMLMorningRequest(Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {

        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, MORNING_OFFER_ACTIVATION_REQUEST_RECIEVED);
        	TransmitMessage message = null;        	
        	InboundUssdMessage inboundUssdMessage = null;
        	inboundUssdMessage = messageListenerImpl.moFinalReqReceived(userInfo);
        	if (null != inboundUssdMessage) {
        		if (inboundUssdMessage.getIncomingLabel() == INVALID_SELECTION) {        			
        			return sendWrongInputErrorNotification(sessionId, userInfo, messageBody, STATUS_INVALID_REQUEST, ML_J4U_STATIC_MENU_WRONG_SELECTION);
        		} else {
        			message = new TransmitMessage();        		
        			generateUssdFinalResponse(message, userInfo, inboundUssdMessage, sessionId);        		
        		}        		
        	}
        	return message;
        }       

        private TransmitMessage ragMainMenuRequest(Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {
            TransmitMessage message = null;
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, RAG_MENU_REQUEST_RECEIVED);
            InboundUssdMessage inboundUssdMessage = null;
            inboundUssdMessage = messageListenerImpl.ragMainMenuReqReceived(userInfo);
            if (null != inboundUssdMessage) {
            	message = new TransmitMessage();
            	message.setDataCoding(ONE);
            	message.setMessageEncoding(ONE);
            	message.setSourceAddress(userInfo.getDestAddress());
            	message.setDestinationAddress(userInfo.getMsisdn());
            	message.setMessageText(inboundUssdMessage.getClobString());
            	message.setTransactionId(userInfo.getTxId());
            	if(SEVENTEEN == inboundUssdMessage.getIncomingLabel()) {
            		message.setServiceOp(SEVENTEEN);
            	}else {
            		message.setServiceOp(TWO);
            	}                
            	message.setReferenceNumber(sessionId);
            	message.setMlFlag(userInfo.isMlFlag());
            	message.setRandomFlag(userInfo.isRandomFlag());
            	message.setUssdLogStatus(TWO);
            	message.setLogStatus(RAG_MENU_RESPONSE_SENT);
            }

            return message;
        }

        private TransmitMessage ragSubMenuRequest(Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {
            
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, RAG_SUBMENU_REQUEST_RECEIVED);

            if (!messageBody.equalsIgnoreCase(USER_SEL_1) && !messageBody.equalsIgnoreCase(USER_SEL_2)) {
                clearMenu(RAG_SUB_MENU, sessionId);
                return sendWrongInputErrorNotification(sessionId, userInfo, messageBody, STATUS_INVALID_REQUEST, ML_RAG_SUB_MENU_WRONG_SELECTION);
            }

            TransmitMessage message = null;
            InboundUssdMessage inboundUssdMessage = messageListenerImpl.ragSubMenuReqReceived(messageBody, userInfo);
            if (null != inboundUssdMessage) {
                message = new TransmitMessage();
                message.setDataCoding(ONE);
                message.setMessageEncoding(ONE);
                message.setSourceAddress(userInfo.getDestAddress());
                message.setDestinationAddress(userInfo.getMsisdn());
                message.setMessageText(inboundUssdMessage.getClobString());
                message.setTransactionId(userInfo.getTxId());

                if (messageBody.equalsIgnoreCase(PropertiesLoader.getValue(USSD_ML_RAG_SUB_MENU_OPT_OUT_SELECTION))) {
                    message.setServiceOp(SEVENTEEN);                    
                } else {
                    message.setServiceOp(TWO);
                }

                message.setReferenceNumber(sessionId);
                message.setMlFlag(userInfo.isMlFlag());
                message.setRandomFlag(userInfo.isRandomFlag());
                message.setUssdLogStatus(TWO);
                message.setLogStatus(RAG_SUBMENU_RESPONSE_SENT);
            }

            return message;
        }

        private TransmitMessage ragOfferInfoRequest(Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, RAG_OFFERMENU_REQUEST_RECEIVED);

            if (!messageBody.equalsIgnoreCase(USER_SEL_1) && !messageBody.equalsIgnoreCase(USER_SEL_2)) {
                return sendWrongInputErrorNotification(sessionId, userInfo, messageBody, STATUS_INVALID_REQUEST, ML_RAG_OFFER_MENU_WRONG_SELECTION);
            }

            TransmitMessage message = null;
            InboundUssdMessage inboundUssdMessage = null;
            inboundUssdMessage = messageListenerImpl.ragOfferInfoReqReceived(messageBody, userInfo);
            if (null != inboundUssdMessage) {
                message = new TransmitMessage();
                message.setDataCoding(ONE);
                message.setMessageEncoding(ONE);
                message.setSourceAddress(userInfo.getDestAddress());
                message.setDestinationAddress(userInfo.getMsisdn());
                message.setMessageText(inboundUssdMessage.getClobString());
                message.setTransactionId(userInfo.getTxId());
                message.setServiceOp(TWO);
                message.setReferenceNumber(sessionId);
                message.setMlFlag(userInfo.isMlFlag());
                message.setRandomFlag(userInfo.isRandomFlag());
                message.setUssdLogStatus(TWO);
                message.setLogStatus(RAG_OFFERMENU_RESPONSE_SENT);
            }

            return message;
        }
        
        private TransmitMessage sagMainMenuRequest(Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {
            TransmitMessage message = null;
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, SAG_MENU_REQUEST_RECEIVED);
            InboundUssdMessage inboundUssdMessage = null;
            inboundUssdMessage = messageListenerImpl.sagMainMenuReqReceived(userInfo);
            if (null != inboundUssdMessage) {
                message = new TransmitMessage();
                message.setDataCoding(ONE);
                message.setMessageEncoding(ONE);
                message.setSourceAddress(userInfo.getDestAddress());
                message.setDestinationAddress(userInfo.getMsisdn());
                message.setMessageText(inboundUssdMessage.getClobString());
                message.setTransactionId(userInfo.getTxId());
                if(SEVENTEEN == inboundUssdMessage.getIncomingLabel()) {
            		message.setServiceOp(SEVENTEEN);
            	}else {
            		message.setServiceOp(TWO);
            	}                
                message.setReferenceNumber(sessionId);
                message.setMlFlag(userInfo.isMlFlag());
                message.setRandomFlag(userInfo.isRandomFlag());
                message.setUssdLogStatus(TWO);
                message.setLogStatus(SAG_MENU_RESPONSE_SENT);
            }

            return message;
        }

        private TransmitMessage sagSubMenuRequest(Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {
            TransmitMessage message = null;
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, SAG_SUBMENU_REQUEST_RECEIVED);
            InboundUssdMessage inboundUssdMessage = null;            

            inboundUssdMessage = messageListenerImpl.sagSubMenuReqReceived(messageBody, userInfo);
            if (null != inboundUssdMessage) {
                message = new TransmitMessage();
                message.setDataCoding(ONE);
                message.setMessageEncoding(ONE);
                message.setSourceAddress(userInfo.getDestAddress());
                message.setDestinationAddress(userInfo.getMsisdn());
                message.setMessageText(inboundUssdMessage.getClobString());
                message.setTransactionId(userInfo.getTxId());
                if (USER_SEL_2.equals(messageBody)) {
                    message.setServiceOp(SEVENTEEN);
                } else {
                    message.setServiceOp(TWO);
                }
                message.setReferenceNumber(sessionId);
                message.setMlFlag(userInfo.isMlFlag());
                message.setRandomFlag(userInfo.isRandomFlag());
                message.setUssdLogStatus(TWO);
                message.setLogStatus(SAG_SUBMENU_RESPONSE_SENT);
            }
            return message;
        }

        private TransmitMessage sagOfferInfoRequest(Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {
        	TransmitMessage message = null;
        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, SAG_OFFERMENU_REQUEST_RECEIVED);

        	InboundUssdMessage inboundUssdMessage = null;
        	inboundUssdMessage = messageListenerImpl.sagOfferInfoReqReceived(userInfo);
        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
        		message.setDataCoding(ONE);
        		message.setMessageEncoding(ONE);
        		message.setSourceAddress(userInfo.getDestAddress());
        		message.setDestinationAddress(userInfo.getMsisdn());
        		message.setMessageText(inboundUssdMessage.getClobString());
        		message.setTransactionId(userInfo.getTxId());
        		message.setServiceOp(TWO);
        		message.setReferenceNumber(sessionId);
        		message.setMlFlag(userInfo.isMlFlag());
        		message.setRandomFlag(userInfo.isRandomFlag());
        		message.setUssdLogStatus(TWO);
        		message.setLogStatus(SAG_OFFERMENU_RESPONSE_SENT);
        	}

        	return message;
        }        
        
        private TransmitMessage initialSocialRequestPart1(Integer sessionId, UserInfo userInfo, String messageBody) throws Exception {
        	TransmitMessage message = null;
        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, STATUS_REQUEST_RECEIVED);
        	String ussdtemplateId;
        	if (userInfo.isMorningOfferFlag()) {
        		ussdtemplateId = userInfo.isConsentFlag() ? USSD_ML_MAIN_MENU_MS_CONSENT_PART_1 : USSD_ML_MAIN_MENU_MS_PART_1;
        	} else {
        		ussdtemplateId = userInfo.isConsentFlag() ? USSD_ML_MAIN_MENU_SO_CONSENT_PART_1 : USSD_ML_MAIN_MENU_SO;
        	}
        	InboundUssdMessage inboundUssdMessage = messageListenerImpl.requestReceived(PropertiesLoader.getValue(ussdtemplateId), userInfo.getLangCode());
        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
        		QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
            	JSONObject queryBaljson = queryBaljsonMap.get(sessionId);
            	if (null != queryBaljson) {
            		messageListenerImpl.getModifyTemplate(queryBaljson, userInfo.getLangCode(), inboundUssdMessage, true);
				} else {
					inboundUssdMessage.setClobString(inboundUssdMessage.getClobString().replace(BALANCE_PATTERN, ""));
				}
        		messageListenerImpl.getGenerateUssdResponse(message, userInfo, inboundUssdMessage, sessionId);
        	}
        	return message;
        }
        
        
        private TransmitMessage initialSocialRequestPart2(Integer sessionId, UserInfo userInfo, String messageBody) throws Exception {
        	TransmitMessage message = null;
        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, STATUS_REQUEST_RECEIVED);
        	String ussdtemplateId;
        	if (userInfo.isMorningOfferFlag()) {
        		ussdtemplateId = userInfo.isConsentFlag() ? USSD_ML_MAIN_MENU_MS_CONSENT_PART_2 : USSD_ML_MAIN_MENU_MS_PART_2;
        	} else {
        		ussdtemplateId = userInfo.isConsentFlag() ? USSD_ML_MAIN_MENU_SO_CONSENT_PART_2 : null;
        	}
        	InboundUssdMessage inboundUssdMessage = messageListenerImpl.requestReceived(PropertiesLoader.getValue(ussdtemplateId), userInfo.getLangCode());
        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
        		messageListenerImpl.getGenerateUssdResponse(message, userInfo, inboundUssdMessage, sessionId);
        	}
        	return message;
        }
        
        private TransmitMessage initialMLRequestPart1(Integer sessionId, UserInfo userInfo, String messageBody) throws Exception {
        	TransmitMessage message = null;
        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, STATUS_REQUEST_RECEIVED);
        	String ussdtemplateId;
        	if (userInfo.isMorningOfferFlag()) {
        		ussdtemplateId = userInfo.isConsentFlag() ? USSD_ML_MAIN_MENU_MO_CONSENT_PART_1 : USSD_ML_MAIN_MENU_MO;
        	} else {
        		ussdtemplateId = userInfo.isConsentFlag() ? USSD_ML_MAIN_MENU_NO_MS_CONSENT : USSD_ML_MAIN_MENU_NO_MS;
        	}
        	InboundUssdMessage inboundUssdMessage = messageListenerImpl.requestReceived(PropertiesLoader.getValue(ussdtemplateId), userInfo.getLangCode());
        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
        		QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
            	JSONObject queryBaljson = queryBaljsonMap.get(sessionId);
            	if (null != queryBaljson) {
            		messageListenerImpl.getModifyTemplate(queryBaljson, userInfo.getLangCode(), inboundUssdMessage, true);
				} else {
					inboundUssdMessage.setClobString(inboundUssdMessage.getClobString().replace(BALANCE_PATTERN, ""));
				}
        		messageListenerImpl.getGenerateUssdResponse(message, userInfo, inboundUssdMessage, sessionId);
        	}
        	return message;
        }
        
        private TransmitMessage initialMLRequestPart2(Integer sessionId, UserInfo userInfo, String messageBody) throws Exception {
        	TransmitMessage message = null;
        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, STATUS_REQUEST_RECEIVED);
        	InboundUssdMessage inboundUssdMessage = null;
        	if(userInfo.isMorningOfferFlag() && userInfo.isConsentFlag()) {
        		inboundUssdMessage = messageListenerImpl.requestReceived(PropertiesLoader.getValue(USSD_ML_MAIN_MENU_MO_CONSENT_PART_2), userInfo.getLangCode());   		
        	}
        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
        		messageListenerImpl.getGenerateUssdResponse(message, userInfo, inboundUssdMessage, sessionId);
        	}
        	return message;
        }
        
        private TransmitMessage sendWrongInputErrorNotification(int sessionId, UserInfo userInfo, String messageBody, String status, String templateId) throws Exception {
            TransmitMessage message = null;
            messageListenerImpl.logInfo(sessionId, userInfo, messageBody, status);           
            InboundUssdMessage inboundUssdMessage = messageListenerImpl.userWrongInput(templateId, userInfo.getLangCode());
            if (null != inboundUssdMessage) {
                message = new TransmitMessage();
                message.setSourceAddress(userInfo.getDestAddress());
                message.setDestinationAddress(userInfo.getMsisdn());
                message.setMessageText(inboundUssdMessage.getClobString());
                message.setTransactionId(userInfo.getTxId());
                message.setServiceOp(SEVENTEEN);
                message.setReferenceNumber(sessionId);
                message.setMlFlag(userInfo.isMlFlag());
                message.setRandomFlag(userInfo.isRandomFlag());
            }
            return message;
        }
        
        private TransmitMessage createConsentMenu(String messageBody, Integer sessionId, UserInfo userInfo) throws Exception {
        	LOG.info("Inside createConsentMenu");
        	TransmitMessage message = null;
        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, CONSENT_MENU_REQUEST_RECEIVED);
        	InboundUssdMessage inboundUssdMessage = messageListenerImpl.getConsentMenu(PropertiesLoader.getValue(USSD_ML_CONSENT_MENU_TEMPLATE), userInfo.getLangCode());
        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
            	message.setDataCoding(ONE);
            	message.setMessageEncoding(ONE);
            	message.setSourceAddress(userInfo.getDestAddress());
            	message.setDestinationAddress(userInfo.getMsisdn());
            	message.setMessageText(inboundUssdMessage.getClobString());
            	message.setTransactionId(userInfo.getTxId());
            	message.setServiceOp(TWO);
            	message.setReferenceNumber(sessionId);
            	message.setMlFlag(userInfo.isMlFlag());
            	message.setRandomFlag(userInfo.isRandomFlag());
            	message.setLogStatus(USSD_CONSENT_MENU_TEMPLATE_SENT);
        	}
        	
        	return message;
        }
        
        private TransmitMessage consentOptOutMenu(String messageBody, Integer sessionId, UserInfo userInfo) throws Exception {
        	LOG.info("Inside consentOptOutMenu");
        	TransmitMessage message = null;
        	consentOptOutMenu.put(sessionId, userInfo);
        	
        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, CONSENT_OPT_OUT_REQUEST_RECEIVED);
        	InboundUssdMessage inboundUssdMessage = messageListenerImpl.getConsentOptOutMenu(PropertiesLoader.getValue(USSD_ML_CONSENT_OPT_OUT_MENU_TEMPLATE), userInfo.getLangCode());     	
        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
            	message.setDataCoding(ONE);
            	message.setMessageEncoding(ONE);
            	message.setSourceAddress(userInfo.getDestAddress());
            	message.setDestinationAddress(userInfo.getMsisdn());
            	message.setMessageText(inboundUssdMessage.getClobString());
            	message.setTransactionId(userInfo.getTxId());
            	message.setServiceOp(TWO);
            	message.setReferenceNumber(sessionId);
            	message.setMlFlag(userInfo.isMlFlag());
            	message.setRandomFlag(userInfo.isRandomFlag());
            	message.setLogStatus(USSD_CONSENT_OPT_OUT_TEMPLATE_SENT);
        	}	
        	return message;
        }
        
        private TransmitMessage consentOptOut(String messageBody, Integer sessionId, UserInfo userInfo) throws Exception {
        	LOG.info("Inside consentOptOut");
        	if (!messageBody.equalsIgnoreCase(USER_SEL_1) && !messageBody.equalsIgnoreCase(USER_SEL_2)) {                
        		return sendWrongInputErrorNotification(sessionId, userInfo, messageBody, STATUS_INVALID_REQUEST, ML_J4U_MENU_WRONG_SELECTION);                
        	}
        	InboundUssdMessage inboundUssdMessage=null;
        	TransmitMessage message = null;
        	if(USER_SEL_1.equals(messageBody)) {
        		messageListenerImpl.logInfo(sessionId, userInfo, messageBody, CONSENT_OPTOUT);
        		updaterDao.updateUserOptOutConsentStatusDB(userInfo.getMsisdn());
        		inboundUssdMessage = messageListenerImpl.getConsentOptOutFinalTemplate(PropertiesLoader.getValue(USSD_ML_CONSENT_OPTED_OUT_TEMPLATE), userInfo.getLangCode());        	        
        	}else {
        		messageListenerImpl.logInfo(sessionId, userInfo, messageBody, CONSENT_OPT_OUT_REJECTED);
        		inboundUssdMessage = messageListenerImpl.getConsentOptOutFinalTemplate(PropertiesLoader.getValue(USSD_ML_CONSENT_OPT_OUT_REJECTED_TEMPLATE), userInfo.getLangCode());    	        
        	}        	
        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
            	message.setDataCoding(ONE);
            	message.setMessageEncoding(ONE);
            	message.setSourceAddress(userInfo.getDestAddress());
            	message.setDestinationAddress(userInfo.getMsisdn());
            	message.setMessageText(inboundUssdMessage.getClobString());
            	message.setTransactionId(userInfo.getTxId());
            	message.setServiceOp(SEVENTEEN);
            	message.setReferenceNumber(sessionId);
            	message.setMlFlag(userInfo.isMlFlag());
            	message.setRandomFlag(userInfo.isRandomFlag());
            	message.setLogStatus(USSD_CONSENT_OPT_OUT_FINAL_TEMPLATE_SENT);
        	}
        	return message;
        }
        
        private TransmitMessage sendDenialNotification(int sessionId, UserInfo userInfo, String messageBody, String status) throws Exception {
        	LOG.info("Inside sendDenialNotification");
        	TransmitMessage message = null;        	
        	messageListenerImpl.logInfo(sessionId, userInfo, messageBody, status);
        	
        	InboundUssdMessage inboundUssdMessage = messageListenerImpl.getDenialMsg(PropertiesLoader.getValue(USSD_ML_CONSENT_DENIED_TEMPLATE), userInfo.getLangCode());
        	if (null != inboundUssdMessage) {
        		message = new TransmitMessage();
            	message.setSourceAddress(userInfo.getDestAddress());
            	message.setDestinationAddress(userInfo.getMsisdn());        	
            	message.setMessageText(inboundUssdMessage.getClobString());                      
            	message.setTransactionId(userInfo.getTxId());
            	message.setServiceOp(SEVENTEEN);
            	message.setReferenceNumber(sessionId);
            	message.setMlFlag(userInfo.isMlFlag());
            	message.setRandomFlag(userInfo.isRandomFlag());
        	}  	
        	return message;
        }
        
        private TransmitMessage onReceivedPSSR(int sessionId, UssdMessage request, UserInfo userInfo) {
        	TransmitMessage message = null;
        	String transactionId = new Utils().getTransactionID();
        	request.setTransactionId(transactionId);
        	messageListenerImpl.logInfo(request, STATUS_REQUEST_RECEIVED);
        	UssdSession session = new UssdSession(sessionId, UssdSession.MO);
        	session.setTransactionId(transactionId);
        	session.setMsisdn(request.getSourceAddress());
        	if (null != activeSessions) {
        		activeSessions.put(sessionId, session);
        		LOG.debug("Ussd session created in Received PSSR ==> " + sessionId);        		
        	}
        	UssdMessage response = messageListenerImpl.requestReceived(request, session, userInfo);
        	if (null != response) {
        		message = new TransmitMessage();
        		message.setDataCoding(ONE);
        		message.setMessageEncoding(ONE);
        		message.setSourceAddress(request.getDestinationAddress());
        		message.setDestinationAddress(request.getSourceAddress());
        		message.setMessageText(response.getMessageText());
        		message.setTransactionId(transactionId);
        		message.setProdIds(response.getProdIds());
        		switch (response.getMessageType()) {
	        		case UssdMessage.NOTIFY:
	        			message.setServiceOp(SEVENTEEN);
	        			break;
	        		case UssdMessage.REQUEST:
	        			message.setServiceOp(TWO);
	        			menuLevelOne.put(sessionId, userInfo);
	        			break;
	        		default:
		                LOG.warn("Unhandled UssdMessageType: " + response.getMessageType());
		                break;
        		}
        	}            
        	session.incrementSequenceNumber();            

        	if (null != response && response.getMessageType() == UssdMessage.NOTIFY) {
        		LOG.debug("Removed UssdMessage NOTIFY Request:" + sessionId);
        		if (null != activeSessions) {
        			activeSessions.remove(sessionId);
        		}
        	}
        	return message;
        }

        private TransmitMessage onReceivedUSSRAck(int sessionId, UssdMessage request) {
            TransmitMessage message = null;
            UssdSession session = null;
            
            if (null != request) {
                LOG.debug("Source address within onRecceived USSR ACk:::" + request.getSourceAddress());
            } else {
                LOG.debug("Source address within onRecceived USSR ACk ::: Request is empty");
            }
            if (null != activeSessions) {
                session = activeSessions.get(sessionId);
            }
            if (null == session) {
                LOG.warn("Could not find session id-" + sessionId + " in the actived sessions map");
                return null;
            }
            if (null != session.getTransactionId()) {
                message = setTransmitMessage(sessionId, request, session);
            } else {
                LOG.warn("Transaction ID was not found for session, returning null message");
            }
            return message;
        }

        private TransmitMessage setTransmitMessage(int sessionId, UssdMessage request, UssdSession session) {
            TransmitMessage message = null;
            String transactionId = session.getTransactionId();
            if (null != request) {
                request.setTransactionId(transactionId);
            }
            messageListenerImpl.logInfo(request, STATUS_USER_RESPONSE_RECEIVED);
            UssdMessage response = messageListenerImpl.responseReceived(request, session);
            if (null != response && null != request) {
            	message = createTransmitMessage(request, response, transactionId, sessionId);
            } else {
                LOG.warn("Response from messageListener.responseReceived is null");
            }
            session.incrementSequenceNumber();
            if (null != response && response.getMessageType() == UssdMessage.NOTIFY) {
                LOG.debug("Remove USSRAck NOTIFY Request:" + sessionId);
                activeSessions.remove(sessionId);
            }
            return message;
        }
        
        private TransmitMessage createTransmitMessage(UssdMessage request, UssdMessage response, String transactionId, int sessionId) {
            TransmitMessage message = new TransmitMessage();
            message.setSourceAddress(request.getDestinationAddress());
            message.setDestinationAddress(request.getSourceAddress());
            message.setMessageText(response.getMessageText());
            message.setTransactionId(transactionId);
            message.setSelProdId(response.getSelProdId());
            switch (response.getMessageType()) {
	            case UssdMessage.NOTIFY:
	                // As requested by DRC, we send 17 for both inbound and
	                // outbound to end the session.
	                message.setServiceOp(SEVENTEEN);
	                menuLevelOne.remove(sessionId);
	                break;
	            case UssdMessage.REQUEST:
	                message.setServiceOp(TWO);
	                break;
	            default:
	                LOG.warn("Unhandled UssdMessageType: " + response.getMessageType());
	                break;
            }
            return message;
        }

        @Override
        public void queryLink(Connection source, EnquireLink el) {
            LOG.debug("Enquire Link request received.");
            try {
                SMPPResponse response = (SMPPResponse) ussdConnection.newInstance(SMPPPacket.ENQUIRE_LINK_RESP);
                response.setCommandStatus(ESME_ROK);
                response.setSequenceNum(el.getSequenceNum());
                ussdConnection.sendResponse(response);
                LOG.debug("Enquire Link response sent.");
            } catch (Exception e) {
                LOG.error("Problem sending the enquire Link response.", e);
            }
        }

        @Override
        public void queryLinkResponse(Connection source, EnquireLinkResp elr) {
            LOG.debug("Enquire Link response received.");
            EnquiryLinkTime.setEnquiryResponseRecieved(true);
            EnquiryLinkTime.getEnquireLinkResponseRecievedTime().set(System.currentTimeMillis() - 100);
        }

        // Called when a bind response packet is received.
        @Override
        public void bindResponse(Connection source, BindResp br) {
            try {
                reentrantLock.lock();
                // on exiting this block, we're sure that
                // the main thread is now sitting in the wait
                // call, awaiting the unbind request.
                LOG.info("Bind response received.");

                if (br.getCommandStatus() == ZERO) {
                    LOG.info("Successfully bound. Awaiting messages..");
                } else {
                    LOG.info("Bind did not succeed! !Error! status = " + br.getCommandStatus());
                    try {
                        ussdConnection.closeLink();
                    } catch (IOException x) {
                        LOG.error("IOException closing link :: ", x);
                    }
                }
                isConnected.signal();
            } catch (Throwable e) {
                LOG.error("Problem receiving the bind response.", e);
            } finally {
                reentrantLock.unlock();
            }
        }

        // This method is called when the SMSC sends an unbind request to our
        // receiver. We must acknowledge it and terminate gracefully..
        @Override
        public void unbind(Connection source, Unbind ubd) {
            LOG.info("SMSC requested unbind. Acknowledging..");
            try {
                // SMSC requests unbind..
                UnbindResp ubr = new UnbindResp(ubd);
                ussdConnection.sendResponse(ubr);
            } catch (IOException x) {
                LOG.error("IOException while acking unbind: ", x);
            }
        }

        // This method is called when the SMSC responds to an unbind request we
        // sent
        // to it..it signals that we can shut down the network connection and
        // terminate our application..
        @Override
        public void unbindResponse(Connection source, UnbindResp ubr) {

            try {
                reentrantLock.lock();
                int st = ubr.getCommandStatus();

                if (st == ZERO) {
                    LOG.info("Successfully unbound.");
                } else {
                    LOG.info("Unbind response: !Error! status = " + st);
                }
                isConnected.signal();
            } catch (Throwable e) {
                LOG.error("Problem receiving the unbind response.", e);
            } finally {
                reentrantLock.unlock();
            }
        }

        // this method is called when the receiver thread is exiting normally.
        @Override
        public void receiverExit(Connection source, ReceiverExitEvent ev) {
            if (ev.getReason() == ReceiverExitEvent.BIND_TIMEOUT) {
                LOG.info("Bind timed out waiting for response.");
            }
            LOG.info("Receiver thread has exited.");
            connectionReset = true;
        }

        // this method is called when the receiver thread exits due to an
        // exception in the thread...
        @Override
        public void receiverExitException(Connection source, ReceiverExitEvent ev) {
            LOG.info("Receiver thread exited abnormally. The following exception was thrown:\n" + ev.getException().toString());
            connectionReset = true;
        }
    }

    class TimeoutHandler implements Runnable {
        public void run() {
            while (isClearActiveSession) {
                try {
                    Set<Integer> sessionIdSet = activeSessions.keySet();
                    Iterator<Integer> iter = sessionIdSet.iterator();
                    while (iter.hasNext()) {
                        Integer key = iter.next();
                        UssdSession session = activeSessions.get(key);
                        if (System.currentTimeMillis() - session.getCreatedTime() >= 60000) {
                            activeSessions.remove(key);
                            LOG.debug("Session removed for the key :: " + key);
                            messageListenerImpl.sessionTimedout(key, session);
                        }
                    }

                    TimeUnit.MILLISECONDS.sleep(TEN);
                } catch (InterruptedException e) {
                	LOG.error(ERROR_OCCURED, e);
                    Thread.currentThread().interrupt();
                }
            }

        }
    }

    class RetryConnection implements Runnable {

        @Override
        public void run() {
            LOG.debug("******* Into Run Method of RetryConnection *******");
            if (EnquiryLinkTime.getRetryCount().get() > retryCount) {
                LOG.warn("Exiting Retry Worker after Retrying " + retryCount + " times ");
                shutdown();
                try {
                    retryExcService.awaitTermination(5, TimeUnit.SECONDS);
                    throw new Exception("Exiting Retry Worker after Retrying " + retryCount + " times");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.error("Thread interrupted", e);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }

            } else if ((EnquiryLinkTime.isEnquiryRequestSent() && verifyToRestart()) || connectionReset) {
                LOG.debug("Retrying connection for " + EnquiryLinkTime.getRetryCount().get() + " time :: connectionReset => " + connectionReset);
                try {
                    unbind();
                } catch (UssdConnectionException e1) {
                    LOG.error(ERROR_OCCURED, e1);
                }
                try {
                    Thread.sleep(TEN_THOUSAND);
                    bind();
                    if (getConnectionStatus() == ACTIVE) {
                        EnquiryLinkTime.getRetryCount().set(ZERO);
                    } else {
                        EnquiryLinkTime.getRetryCount().incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.error("Thread interrupted during retry", e);
                } catch (Exception e) {
                    LOG.error("Error during Retry-->> : " + e.getMessage(), e);
                }
            }
        }
    }

}