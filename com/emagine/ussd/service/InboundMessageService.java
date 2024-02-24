package com.emagine.ussd.service;

import static com.emagine.ussd.utils.USSDConstants.*;
import static com.emagine.ussd.utils.Utils.getCurrentTimeStamp;

import java.io.FileInputStream;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.comviva.ped.service.IPEDProcessService;
import com.comviva.ped.service.PEDProcessService;
import com.emagine.ussd.config.PropertiesLoader;
import com.emagine.ussd.connection.TransmitMessage;
import com.emagine.ussd.connection.UssdMessage;
import com.emagine.ussd.connection.UssdSession;
import com.emagine.ussd.dao.LookUpDAO;
import com.emagine.ussd.dao.UpdaterDAO;
import com.emagine.ussd.model.DefaultMessageDTO;
import com.emagine.ussd.model.InboundUssdMessage;
import com.emagine.ussd.model.MLOfferMsg;
import com.emagine.ussd.model.MessageDTO;
import com.emagine.ussd.model.OfferParams;
import com.emagine.ussd.model.RankingFormulae;
import com.emagine.ussd.model.TemplateDTO;
import com.emagine.ussd.model.TownProdInfo;
import com.emagine.ussd.model.UserInfo;
import com.emagine.ussd.service.publisher.UssdEventPublisher;
import com.emagine.ussd.utils.DefaultMessageCache;
import com.emagine.ussd.utils.ProductInfoCache;
import com.emagine.ussd.utils.ProductPriceCache;
import com.emagine.ussd.utils.QueryBaljsonMap;
import com.emagine.ussd.utils.TemplateCache;
import com.emagine.ussd.utils.USSDConstants;
import com.emagine.ussd.utils.Utils;

public class InboundMessageService {

	private static final Logger LOGGER = Logger.getLogger(InboundMessageService.class);
	
	private static final String CREATING_ENTRY_IN_ECMP_T_USSD_TRX_PRODID_MAP_TABLE = "creating entry in ECMP_T_USSD_TRX_PRODID_MAP table";
	private static final String TEMPLATE_DTO = "templateDTO :: ";
	private static Properties properties = new Properties();
	private static Map<String, String> mlSubMenuProdTypeMap = getSubMenuProdTypeMap();
	private static Map<String, String> currencyTypeMap = getCurrencyType();
	private static Map<String, String> mlRefreshFlagMap;
	private static int isSocialEnabled;
	// Specify the number of decimal places to round
	private static int decimalPlaces;
	private static double multiplier;
	
	static {
		try {
			loadProperties(USSD_CONNECTION_PROPERTIES_FILE);
			mlRefreshFlagMap = loadMlRefreshFlagMap();
			isSocialEnabled = PropertiesLoader.getIntValue(USSD_SOCIAL_ENABLED);
			decimalPlaces = (PropertiesLoader.getIntValue(BAL_DECIMAL_PLACES) > 0) ? PropertiesLoader.getIntValue(BAL_DECIMAL_PLACES) : 2;
			multiplier = Math.pow(10, decimalPlaces);
		} catch (Exception e) {
			LOGGER.error("Error in loading the USSD Connection Properties File", e);
		}
	}
	private static final String TEMPLATE_NOT_FOUND = "Template details could not be found for id :: ";
	private static SecureRandom random = new SecureRandom();
	private LookUpDAO lookUpDAO;
	private UpdaterDAO updaterDAO;
	private Map<String, String> ragSubMenuInputMap = null;
	private Map<String, String> ragOfferInfoInputMap = null;
	private IPEDProcessService pedPrcosesService;
	private String csType = null;
	private static Predicate<String> checkSocialMenu = menu -> (ONE == isSocialEnabled && menu.equals(USER_SEL_6));
	private static Function<List<String>, List<String>> getProductPriceList = productidList -> {
		List<String> productPricesList = new ArrayList<>();
		try {
			ProductPriceCache priceCache = ProductPriceCache.instance();
			for (int index = 0; index < productidList.size(); index++) {
				productPricesList.add(index, priceCache.get(productidList.get(index)));
			}
		} catch (Exception ex) {
			LOGGER.error("Product price gettting failed - ", ex);
		}

        LOGGER.info("productPricesList = " + productPricesList);
        return productPricesList;
    };
    
    private static Function<List<String>, List<String>> getRandomProdIdList = prodIdsList -> {
    	int prodIdsListLen = prodIdsList.size();
    	LOGGER.debug("prodIdsListLength :-" + prodIdsListLen);
    	Set<String> randomIdsSet = new HashSet<>();
    	if (prodIdsListLen > FIVE) {
    		while (true) {
    			randomIdsSet.add(prodIdsList.get(random.nextInt(prodIdsListLen)));
    			if (randomIdsSet.size() == FIVE) {
    				break;
    			}
    		}
    	} else  {
    		randomIdsSet.addAll(prodIdsList);
    	}    	 
    	return new ArrayList<>(randomIdsSet);
    };

	public InboundMessageService() {
		try {			
			lookUpDAO = new LookUpDAO();
			updaterDAO = new UpdaterDAO();
			pedPrcosesService = new PEDProcessService();
		} catch (Exception ex) {
			LOGGER.error("Exception occured in creating DAOs:: " + ex.getMessage(), ex);
		}
	}

	private static void loadProperties(String fileName) {
		try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
			properties.load(fileInputStream);
		} catch (Exception e) {
			LOGGER.error("Error in loading the USSD Properties File", e);
		}

    }

    private static Map<String, String> loadMlRefreshFlagMap() throws Exception {
        mlRefreshFlagMap = new HashMap<>();
        List<String> offerRefreshFlagList = Arrays.asList(PropertiesLoader.getValue(USSD_OFFER_REFRESH_FLAG_MAP).split(","));
        for (String key : offerRefreshFlagList) {
            String[] mapAry = key.split(":");
            mlRefreshFlagMap.put(mapAry[ZERO], mapAry[ONE]);
        }

        return mlRefreshFlagMap;
    }

    private static Map<String, String> getSubMenuProdTypeMap() {
        List<String> prodTypeList;
        try {
            mlSubMenuProdTypeMap = new HashMap<>();
            prodTypeList = Arrays.asList(PropertiesLoader.getValue(USSD_SUB_MENU_PROD_TYPE).split(","));
            for (String key : prodTypeList) {
                String[] prodMapping = key.split(":");
                mlSubMenuProdTypeMap.put(prodMapping[ZERO], prodMapping[ONE]);
            }
        } catch (Exception e) {
            LOGGER.error("Error occured at getSubMenuProdTypeMap ", e);
        }

        return mlSubMenuProdTypeMap;
    }

    private static Map<String, String> getCurrencyType() {
        List<String> currencyTypeList;
        try {
            currencyTypeMap = new HashMap<>();
            currencyTypeList = Arrays.asList(PropertiesLoader.getValue(USSD_MPESA_CURRENCY_TYPE).split(","));
            for (String key : currencyTypeList) {
                String[] prodMapping = key.split(":");
                currencyTypeMap.put(prodMapping[ZERO], prodMapping[ONE]);
            }
        } catch (Exception e) {
            LOGGER.error("Error occured at. ", e);
        }

        return currencyTypeMap;
    }    

    public InboundUssdMessage getMenuForUser(UssdMessage ussdMessage, UserInfo userInfo) {
    	MessageDTO messageDTOForUser = new MessageDTO();
    	messageDTOForUser.setUssdMsgId(ussdMessage.getSessionId());
    	messageDTOForUser.setTxnId(ussdMessage.getTransactionId());
    	messageDTOForUser.setMsisdn(ussdMessage.getSourceAddress());
    	messageDTOForUser.setUssdShortCode(ussdMessage.getDestinationAddress());
    	messageDTOForUser.setMessageText(ussdMessage.getMessageText());
    	InboundUssdMessage inboundUSSDMsgForUser = new InboundUssdMessage();
    	try {
    		// Call the DAO Class for offer lookup
    		if (!lookUpDAO.lookupOffers(messageDTOForUser)) {
    			if(userInfo.isMorningOfferFlag()) {
    				LOGGER.info("User not found in ENBA_EX_T_MSISDN_OFFER_MSG table >> Non-Ml NewUser. MorningOfferFlag :: " +userInfo.isMorningOfferFlag());
    				lookUpTemplateDetails(PropertiesLoader.getValue(TEMP_FR_MORNING_OFFER_MENU), messageDTOForUser);
    				inboundUSSDMsgForUser.setIncomingLabel(UssdMessage.REQUEST);
    				messageDTOForUser.setStatus(STATUS_USSD_OFFER_FOUND);
    				userInfo.setOfferCount(ZERO);
    			}else {
    				LOGGER.info("User not found. Sending NOTIFY with default message. MSISDN:: " + ussdMessage.getSourceAddress());
    				// (New User) i.e. User not found. Fetch notify message for newUser-default message template id
    				lookUpTemplateDetails(PropertiesLoader.getValue(NEW_USER_DEFAULT_TEMPLATE), messageDTOForUser);
    				inboundUSSDMsgForUser.setIncomingLabel(UssdMessage.NOTIFY);
    				messageDTOForUser.setStatus(STATUS_USER_NOT_FOUND);
    			}    			
    			inboundUSSDMsgForUser.setClobString(messageDTOForUser.getTemplate());    			
    			logEventIntoDB(messageDTOForUser);
    			return inboundUSSDMsgForUser;
    		}else {
    			// User exists, but product_ids are not available
    			// select template based on language code and send notification
    			if (null == messageDTOForUser.getProductIds()) {
    				if(userInfo.isMorningOfferFlag()) {
    					lookUpTemplateDetails(PropertiesLoader.getValue(TEMP_FR_MORNING_OFFER_MENU), messageDTOForUser);
    					inboundUSSDMsgForUser.setIncomingLabel(UssdMessage.REQUEST);
    					messageDTOForUser.setStatus(STATUS_USSD_OFFER_FOUND);
    					userInfo.setOfferCount(ZERO);
    				}else {
    					LOGGER.info("User exists but not entitled for offers. Sending NOTIFY. MSISDN:: " + messageDTOForUser.getMsisdn() + " SESSION ID:: " + messageDTOForUser.getUssdMsgId());        			
    					lookUpTemplateDetails(messageDTOForUser.getTemplateId(), messageDTOForUser);
    					inboundUSSDMsgForUser.setIncomingLabel(UssdMessage.NOTIFY);
    					messageDTOForUser.setStatus(STATUS_OFFERS_NOT_FOUND);
    				}    			    			
    				inboundUSSDMsgForUser.setClobString(messageDTOForUser.getTemplate());    			
    				logEventIntoDB(messageDTOForUser);
    				return inboundUSSDMsgForUser;
    			}

    			// User exists, different product_ids are fetched and offers are made available    			
    			lookUpTemplateDetails(messageDTOForUser.getTemplateId(), messageDTOForUser);
    			String template = getMenuMessageForTemplate(messageDTOForUser);
    			TemplateCache templateCache = TemplateCache.instance();
    			
    			if(userInfo.isMorningOfferFlag()) {
    				LOGGER.info("User found in ENBA_EX_T_MSISDN_OFFER_MSG table. MorningOfferFlag :: " + userInfo.isMorningOfferFlag());
    				if(null != template) {
    					template = template.replace(JUSTE_POUR_TOI, "");
    				}
    				String moTemplate = null;    				
    				TemplateDTO templateDTO = templateCache.get(PropertiesLoader.getValue(TEMP_FR_MORNING_OFFER_MENU));
    				if (null != templateDTO) 
    					moTemplate= templateDTO.getTemplate();    				
	    				// Final MO template to display
	    				template =moTemplate + template;
    			}
    			
    			if(userInfo.isConsentFlag()) {
    				TemplateDTO templateDTO = templateCache.get(PropertiesLoader.getValue(USSD_NON_ML_CONSENT_OPT_OUT));
    				if (null != templateDTO) {     				
    					template = template +templateDTO.getTemplate();
    				}else {
    		            LOGGER.error("Template details could not be found for the id:: " + USSD_NON_ML_CONSENT_OPT_OUT);
    		        }
    				
    			}
    			inboundUSSDMsgForUser.setClobString(template);

    			//creating entry in ECMP_T_USSD_TRX_PRODID_MAP table
    			updaterDAO.insertTrxProdIdMap(messageDTOForUser);    			

    			// Returns Message in user preferred language back to USSDPlugin.    			
    			inboundUSSDMsgForUser.setIncomingLabel(UssdMessage.REQUEST);
    			inboundUSSDMsgForUser.setProdIds(new ArrayList<>(messageDTOForUser.getProducts().values()));    		
    			messageDTOForUser.setStatus(STATUS_USSD_OFFER_FOUND);
    			logEventIntoDB(messageDTOForUser);
    		}
    	} catch (Exception ex) {
    		LOGGER.error("Exception Occured:: " + ex.getMessage(), ex);
    		try {
    			messageDTOForUser.setStatus(STATUS_EXCEPTION);
    			logEventIntoDB(messageDTOForUser);
    			inboundUSSDMsgForUser = null;
    		} catch (Exception ex1) {
    			LOGGER.error("Exception Occured:: " + ex1.getMessage(), ex1);
    		}
    	}
    	return inboundUSSDMsgForUser;
    }


    public InboundUssdMessage getRagMainMenu(UserInfo userInfo) throws Exception {
        TemplateCache templateCache = TemplateCache.instance();
        InboundUssdMessage inboundUSSDMsgRag = null;

        userInfo = lookUpDAO.getRagUserInfo(userInfo);
        LOGGER.debug("userInfo.isRagEligibleFlag() => " + userInfo.isRagEligibleFlag());
        LOGGER.debug("userInfo.isRagOptInFlag() => " + userInfo.isRagOptInFlag());
        LOGGER.debug("userInfo.isRagGoalReachedFlag() => " + userInfo.isRagGoalReachedFlag());

        if (!userInfo.isRagEligibleFlag() && !userInfo.isRagOptInFlag() && !userInfo.isRagGoalReachedFlag()) {
            TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_RAG_INELIGIBLE_MENU) + "_" + userInfo.getLangCode());
            inboundUSSDMsgRag = getRagMenuContent(userInfo, templateDTO);
            inboundUSSDMsgRag.setIncomingLabel(SEVENTEEN);
        } else if (userInfo.isRagEligibleFlag() && !userInfo.isRagOptInFlag() && !userInfo.isRagGoalReachedFlag()) {
            TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_RAG_FIRST_OPT_IN_MENU) + "_" + userInfo.getLangCode());
            inboundUSSDMsgRag = getRagMenuContent(userInfo, templateDTO);
            userInfo.setRagOptInFlag(true);
            updaterDAO.updateOptInfo(userInfo);
            sendOptInSMSNotification(userInfo);
            inboundUSSDMsgRag.setIncomingLabel(TWO);
        } else if (userInfo.isRagEligibleFlag() && userInfo.isRagOptInFlag() && !userInfo.isRagGoalReachedFlag()) {
            TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_RAG_ALREADY_OPT_IN_MENU) + "_" + userInfo.getLangCode());
            inboundUSSDMsgRag = getRagMenuContent(userInfo, templateDTO);
            inboundUSSDMsgRag.setIncomingLabel(TWO);
        } else if (userInfo.isRagEligibleFlag() && userInfo.isRagOptInFlag() && userInfo.isRagGoalReachedFlag()) {
            TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_RAG_GOAL_REACHED_MENU) + "_" + userInfo.getLangCode());
            inboundUSSDMsgRag = getRagMenuContent(userInfo, templateDTO);
            inboundUSSDMsgRag.setIncomingLabel(SEVENTEEN);
        }
        return inboundUSSDMsgRag;
    }
    
    public InboundUssdMessage getSagMainMenu(UserInfo userInfo) throws Exception {
    	LOGGER.debug("Inside getSagMainMenu!");
        TemplateCache templateCache = TemplateCache.instance();
        InboundUssdMessage inboundUSSDMsgRag = null;

        userInfo = lookUpDAO.getSagUserInfo(userInfo);
        LOGGER.debug("userInfo.isSagEligibleFlag() => " + userInfo.isSagEligibleFlag());
        LOGGER.debug("userInfo.isSagOptInFlag() => " + userInfo.isSagOptInFlag());
        LOGGER.debug("userInfo.isSagGoalReachedFlag() => " + userInfo.isSagGoalReachedFlag());

        if (!userInfo.isSagEligibleFlag() && !userInfo.isSagOptInFlag() && !userInfo.isSagGoalReachedFlag()) {
            TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_SAG_INELIGIBLE_MENU) + "_" + userInfo.getLangCode());
            inboundUSSDMsgRag = getSagMenuContent(userInfo, templateDTO);
            inboundUSSDMsgRag.setIncomingLabel(SEVENTEEN);
        } else if (userInfo.isSagEligibleFlag() && !userInfo.isSagOptInFlag() && !userInfo.isSagGoalReachedFlag()) {
            TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_SAG_FIRST_OPT_IN_MENU) + "_" + userInfo.getLangCode());
            inboundUSSDMsgRag = getSagMenuContent(userInfo, templateDTO);
            userInfo.setSagOptInFlag(true);
            updaterDAO.updateSAGOptInfo(userInfo);
            sendSAGOptInSMSNotification(userInfo);
            inboundUSSDMsgRag.setIncomingLabel(TWO);
        } else if (userInfo.isSagEligibleFlag() && userInfo.isSagOptInFlag() && !userInfo.isSagGoalReachedFlag()) {
            TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_SAG_ALREADY_OPT_IN_MENU) + "_" + userInfo.getLangCode());
            inboundUSSDMsgRag = getSagMenuContent(userInfo, templateDTO);
            inboundUSSDMsgRag.setIncomingLabel(TWO);
        } else if (userInfo.isSagEligibleFlag() && userInfo.isSagOptInFlag() && userInfo.isSagGoalReachedFlag()) {
            TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_SAG_GOAL_REACHED_MENU) + "_" + userInfo.getLangCode());
            inboundUSSDMsgRag = getSagMenuContent(userInfo, templateDTO);
            inboundUSSDMsgRag.setIncomingLabel(SEVENTEEN);
        }

        return inboundUSSDMsgRag;
    }

    public InboundUssdMessage getRagSubMenu(String messageBody, UserInfo userInfo) throws Exception {
        InboundUssdMessage inboundUSSDMsgRagSub = new InboundUssdMessage();
        TemplateCache templateCache = TemplateCache.instance();

        userInfo = lookUpDAO.getRagUserInfo(userInfo);

        DefaultMessageCache defaultMessageCache = DefaultMessageCache.instance();
        DefaultMessageDTO defaultMsgDTO = defaultMessageCache.get(userInfo.getLangCode());

        try {
             ragSubMenuInputMap = getRagSubMenuInputMap();

            if (!ragSubMenuInputMap.containsKey(messageBody)) {
                throw new NumberFormatException("Incorrect user input from user");
            } else {

                if (messageBody.equalsIgnoreCase(PropertiesLoader.getValue(USSD_ML_RAG_SUB_MENU_OFFER_INFO_SELECTION))) {
                    TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_RAG_OFFER_INFO) + "_" + userInfo.getLangCode());
                    inboundUSSDMsgRagSub = getRagMenuContent(userInfo, templateDTO);

                } else if (messageBody.equalsIgnoreCase(PropertiesLoader.getValue(USSD_ML_RAG_SUB_MENU_OPT_OUT_SELECTION))) {
                    userInfo.setRagOptInFlag(false);
                    updaterDAO.updateOptInfo(userInfo);
                    String defaultMessage = defaultMsgDTO.getDefaultMsg();
                    inboundUSSDMsgRagSub.setClobString(defaultMessage);
                    sendOptOutSMSNotification(userInfo);
                }
            }
            return inboundUSSDMsgRagSub;
        } catch (NumberFormatException nfe) {
            // If user entered something other than what is expected for ML
            LOGGER.error("User input was invalid by user :: " + messageBody + " Sending error message back as NOTIFY to user", nfe);
            String errorMsg = defaultMsgDTO.getErrorMsg();
            inboundUSSDMsgRagSub.setClobString(errorMsg);
            return inboundUSSDMsgRagSub;
        }

    }
    
    public InboundUssdMessage getSagSubMenu(String messageBody, UserInfo userInfo) throws Exception {
    	InboundUssdMessage inboundUSSDMsgSagSub = new InboundUssdMessage();
    	TemplateCache templateCache = TemplateCache.instance();
    	DefaultMessageCache defaultMessageCache = DefaultMessageCache.instance();
    	DefaultMessageDTO defaultMsgDTO = defaultMessageCache.get(userInfo.getLangCode());

    	userInfo = lookUpDAO.getSagUserInfo(userInfo);

    	if (USER_SEL_1.equals(messageBody)) {
    		TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_SAG_OFFER_INFO) + "_" + userInfo.getLangCode());
    		inboundUSSDMsgSagSub = getSagMenuContent(userInfo, templateDTO);

    	} else if (USER_SEL_2.equals(messageBody)) {
    		userInfo.setSagOptInFlag(false);
    		updaterDAO.updateSAGOptInfo(userInfo);
    		String defaultMessage = defaultMsgDTO.getDefaultMsg();
    		inboundUSSDMsgSagSub.setClobString(defaultMessage);
    		sendSAGOptOutSMSNotification(userInfo);
    	}
    	return inboundUSSDMsgSagSub;
    }
  
    public InboundUssdMessage getOfferInfoMenu(String messageBody, UserInfo userInfo) throws Exception {
        InboundUssdMessage inboundUSSDMsgRagInfo = new InboundUssdMessage();

        userInfo = lookUpDAO.getRagUserInfo(userInfo);

        DefaultMessageCache defaultMessageCache = DefaultMessageCache.instance();
        DefaultMessageDTO defaultMsgDTO = defaultMessageCache.get(userInfo.getLangCode());

        try {
            Map<String, String> ragOfferInfoInputMap = getRagOfferInfoInputMap();

            if (!ragOfferInfoInputMap.containsKey(messageBody)) {
                throw new NumberFormatException("Incorrect user input");
            } else {
                if (messageBody.equalsIgnoreCase(PropertiesLoader.getValue(USSD_ML_RAG_OFFER_INFO_BACK_SELECTION))) {
                    inboundUSSDMsgRagInfo = getRagMainMenu(userInfo);
                }
            }
            return inboundUSSDMsgRagInfo;
        } catch (NumberFormatException nfe) {
            // If user entered something other than what is expected for ML
            LOGGER.info("User input was invalid by user :: " + messageBody + " Sending error message back as NOTIFY to usr");
            String errorMsg = defaultMsgDTO.getErrorMsg();
            inboundUSSDMsgRagInfo.setClobString(errorMsg);
            return inboundUSSDMsgRagInfo;
        }
    }

    public InboundUssdMessage getSAGOfferInfoMenu(UserInfo userInfo) throws Exception {
    	InboundUssdMessage inboundUSSDMsgSagInfo = null;    	
    	inboundUSSDMsgSagInfo = getSagMainMenu(userInfo);
    	return inboundUSSDMsgSagInfo;
    }

    
    private InboundUssdMessage getRagMenuContent(UserInfo userInfo, TemplateDTO templateDTO) throws Exception {
        InboundUssdMessage inboundUSSDMsgRagContent = new InboundUssdMessage();
        String[] ragParams = templateDTO.getOfferOrderCSV().split(",");
        String template = templateDTO.getTemplate();
        for (int i = 0; i < ragParams.length; i++) {
            String param = "@" + ragParams[i] + "@";
            if (null != userInfo.getRagInfo().get(ragParams[i])) {
                template = template.replace(param, userInfo.getRagInfo().get(ragParams[i]));
            }
        }
        inboundUSSDMsgRagContent.setClobString(template);
        return inboundUSSDMsgRagContent;
    }

    private InboundUssdMessage getSagMenuContent(UserInfo userInfo, TemplateDTO templateDTO) throws Exception {
        InboundUssdMessage inboundUSSDMsgSagContent = new InboundUssdMessage();
        String[] sagParams = templateDTO.getOfferOrderCSV().split(",");
        String template = templateDTO.getTemplate();
        for (int i = 0; i < sagParams.length; i++) {
            String param = "@" + sagParams[i] + "@";
            if (null != userInfo.getSagInfo().get(sagParams[i])) {
                template = template.replace(param, userInfo.getSagInfo().get(sagParams[i]));
            }
        }
        inboundUSSDMsgSagContent.setClobString(template);
        return inboundUSSDMsgSagContent;
    }
    
    private Map<String, String> getRagSubMenuInputMap() {
        List<String> inputList;
        try {
            ragSubMenuInputMap = new HashMap<>();
            inputList = Arrays.asList(PropertiesLoader.getValue(USSD_ML_RAG_SUB_MENU_OPTIONS).split(","));
            for (String key : inputList) {
                String[] prodMapping = key.split(":");
                ragSubMenuInputMap.put(prodMapping[ZERO], prodMapping[ONE]);
            }

        } catch (Exception e) {
            LOGGER.error("Error occured at . ", e);
        }

        return ragSubMenuInputMap;
    }

    private Map<String, String> getRagOfferInfoInputMap() {
        List<String> inputList;
        try {
            ragOfferInfoInputMap = new HashMap<>();
            inputList = Arrays.asList(PropertiesLoader.getValue(USSD_ML_RAG_OFFER_INFO_OPTIONS).split(","));
            for (String key : inputList) {
                String[] prodMapping = key.split(":");
                ragOfferInfoInputMap.put(prodMapping[ZERO], prodMapping[ONE]);
            }
            LOGGER.info(ragOfferInfoInputMap.keySet());
        } catch (Exception e) {
            LOGGER.error("Error occured at getRagOfferInfoInputMap ", e);
        }

        return ragOfferInfoInputMap;
    }   

    /***
     * Generates submenu for Random user
     * @param messageBody
     * @param userInfo
     * @return
     * @throws Exception
     */
        
    public InboundUssdMessage getSubMenuForMLUser(String messageBody, UserInfo userInfo) throws Exception {
    	InboundUssdMessage inboundUssdMessage ;
    	if(checkSocialMenu.test(messageBody)) {
    		inboundUssdMessage = getSubMenuForMLRandomUserSocial(userInfo);
    	} else {
    		inboundUssdMessage = getSubMenuForMLRandomUser(messageBody, userInfo);
    	}
    	return inboundUssdMessage ;
    }

    public InboundUssdMessage getSubMenuForMLRandomUser(String messageBody, UserInfo userInfo) throws Exception {
        InboundUssdMessage inboundUSSDMsgRandom = null;
        LOGGER.info("Checking location random offer=> LocationRandomFlag: " + userInfo.isLocationRandomFlag());        
        List<String> prodIdsList = null;
        ProductInfoCache productInfoCache = ProductInfoCache.instance() ;
        if (userInfo.isLocationRandomFlag() && null != userInfo.getPoolId()) {
        	LOGGER.debug("Fetching Offers from ECMP_T_LOCATION_PROD_INFO Table");
            prodIdsList = productInfoCache.getLocationProdIds (mlSubMenuProdTypeMap.get(messageBody), userInfo.getPoolId(), userInfo.getLangCode());
        }
  
        if (null == prodIdsList || prodIdsList.isEmpty() || prodIdsList.size() < THREE && !USER_SEL_4.equalsIgnoreCase(messageBody)) {
            LOGGER.debug("NO location random offers => ");
            LOGGER.debug("ML Random FLAG => " + userInfo.isRandomFlag());
            if (userInfo.isRandomFlag()) {
                LOGGER.info("Getting ML normal random offer  => non-PromotionalProductInfo from ECMP_T_PROD_INFO Table");
                prodIdsList = productInfoCache.getNonPromotionalOffer(mlSubMenuProdTypeMap.get(messageBody));
            }
        }
        Set<String> randomIdsSet = new HashSet<>();
        if (null != prodIdsList && !prodIdsList.isEmpty()) {
            int prodIdsListLen = prodIdsList.size();
            LOGGER.debug("prodIdsListLen :-" + prodIdsListLen);
            if (prodIdsListLen >= MAX_PRODIDS_CNT_3) {
            	while (true) {
					randomIdsSet.add(prodIdsList.get(random.nextInt(prodIdsListLen)));
                    if (randomIdsSet.size() == MAX_PRODIDS_CNT_3) {
                        break;
                    }
                }
            } else if (USER_SEL_4.equalsIgnoreCase(messageBody)) {
                randomIdsSet.addAll(prodIdsList);
            }
        } else {
            return getInvalidProductIdLabel(userInfo);
        }

        List<String> randomIdsList = new ArrayList<>(randomIdsSet);
        List<String> productPricesList = new ArrayList<>();
        LOGGER.info("randomIdsList = " + randomIdsList);
        productPricesList = getProductPriceList.apply(randomIdsList) ;
        LOGGER.info("productPricesList = " + productPricesList);

        TemplateDTO templateDTO = getSubMenuWithHourly(userInfo.getLangCode(), messageBody, randomIdsList.size());
        LOGGER.info("templateDTO = " + templateDTO);
        if (null != templateDTO) {
            inboundUSSDMsgRandom = generateSubMenu(userInfo.getLangCode(), randomIdsList, productInfoCache, templateDTO);
            insertInUssdTransProdIdMap(userInfo, randomIdsList,productPricesList);
            inboundUSSDMsgRandom.setProdIds(randomIdsList);
        } else {
            inboundUSSDMsgRandom = getInvalidProductIdLabel(userInfo);
        }

        return inboundUSSDMsgRandom;
    }

    public InboundUssdMessage getSubMenuForMLRandomUserSocial (UserInfo userInfo) throws Exception {
    	InboundUssdMessage inboundUSSDMsgRandom = null;
    	LOGGER.info("checking normal random offer=> RandomFlag: " + userInfo.isRandomFlag());
    	List<String> prodIdsList = null;    	

    	ProductInfoCache socialProductInfoCache = ProductInfoCache.instance();
    	prodIdsList = socialProductInfoCache.getSocialProductIdsList();
    	LOGGER.debug("Fetched Offers from ECMP_T_SOCIAL_PROD_INFO Table -> "+prodIdsList);

    	Set<String> randomIdsSet = new HashSet<>();
    	if (null != prodIdsList && !prodIdsList.isEmpty()) {
    		int prodIdsListLen = prodIdsList.size();
    		LOGGER.debug("prodIdsListLen :-" + prodIdsListLen);
    		if (prodIdsListLen >= MAX_PRODIDS_CNT_3) {
    			while (true) {
    				randomIdsSet.add(prodIdsList.get(random.nextInt(prodIdsListLen)));
    				if (randomIdsSet.size() == MAX_PRODIDS_CNT_3) {
    					break;
    				}
    			}
    		} 
    	} else {
    		return getInvalidProductIdLabel(userInfo);
    	}

    	List<String> randomIdsList = new ArrayList<>(randomIdsSet);
    	LOGGER.info("randomIdsList = " + randomIdsList);
    	List<String> productPricesList = getProductPriceList.apply(randomIdsList);
    	TemplateCache templateCache = TemplateCache.instance();
    	TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(ML_3_OFFER_SUBMENU_TEMPLATE) + "_" + userInfo.getLangCode());
    	LOGGER.info("templateDTO = " + templateDTO);
    	if (null != templateDTO) {
    		inboundUSSDMsgRandom = generateSubMenuSocial(userInfo.getLangCode(), randomIdsList, socialProductInfoCache, templateDTO);
    		LOGGER.debug(CREATING_ENTRY_IN_ECMP_T_USSD_TRX_PRODID_MAP_TABLE);
    		updaterDAO.insertTrxProdIdMap(userInfo, randomIdsList.get(ZERO) + "~" + productPricesList.get(ZERO), randomIdsList.get(ONE) + "~" + productPricesList.get(ONE), randomIdsList.get(TWO) + "~" + productPricesList.get(TWO));    		
    		inboundUSSDMsgRandom.setProdIds(randomIdsList);
    	} else {
    		inboundUSSDMsgRandom = getInvalidProductIdLabel(userInfo);
    	}

    	return inboundUSSDMsgRandom;

    }     
    
    public InboundUssdMessage getSubMenuForMorningUser(UserInfo userInfo) throws Exception {
    	InboundUssdMessage inboundUSSDMsg = null;
    	LOGGER.info("Checking Morning offer, isMorningOfferFlag => " + userInfo.isMorningOfferFlag());
    	LOGGER.debug("MSISDN=>" + userInfo.getMsisdn());

    	//Get the morningOffers which is not activated by the customer
    	List<String> prodIds = lookUpDAO.getMorningOfferWhiteList(userInfo.getMsisdn());

    	ProductInfoCache morningProductInfoCache = ProductInfoCache.instance();

    	if (null != prodIds && !prodIds.isEmpty()) {

    		TemplateDTO templateDTO = getSubMenuWithMorning(userInfo.getLangCode(), prodIds.size());
    		if (null != templateDTO) {
    			inboundUSSDMsg = generateSubMenuMorning(userInfo.getLangCode(), prodIds, morningProductInfoCache, templateDTO);
    			LOGGER.debug("ProductDesc=> \n" + inboundUSSDMsg.getClobString());
    		
    			List<String> productPricesList = getProductPriceList.apply(prodIds);
    			insertInUssdTransProdIdMap(userInfo, prodIds, productPricesList);
    			userInfo.setOfferCount(prodIds.size());
    			inboundUSSDMsg.setProdIds(prodIds);
    		} else {
    			inboundUSSDMsg = getInvalidProductIdLabel(userInfo);
    		}

    	} else {
    		return getInvalidProductIdLabel(userInfo);
    	}
    	return inboundUSSDMsg;   
    }  
    
    public InboundUssdMessage getSubMenuForTownUser(UserInfo userInfo) throws Exception {
    	InboundUssdMessage inboundUSSDMsg = null;
    	LOGGER.debug("getSubMenuForTownUser, MSISDN=>" + userInfo.getMsisdn());

    	ProductInfoCache townProductInfoCache = ProductInfoCache.instance(); 
    	TownProdInfo townInfo =townProductInfoCache.getCellIdTownName(userInfo.getCellId());

    	if (null != townInfo &&  null != townInfo.getTownName()) {
    		LOGGER.debug("Get the TownOffers which are under promotion for the Town: "+ townInfo.getTownName());    		
    		List<String> prodIdsList = townProductInfoCache.getTownProductIdsMapList(townInfo.getTownName());

    		if (null != prodIdsList && !prodIdsList.isEmpty()) {      			
    			List<String> productIdsList = getRandomProdIdList.apply(prodIdsList);

    			TemplateDTO templateDTO = getSubMenuTownOffers(userInfo.getLangCode(), productIdsList.size());
    			if (null != templateDTO) {
    				inboundUSSDMsg = generateTownSubMenu(userInfo.getLangCode(), productIdsList, townProductInfoCache, templateDTO);
    				LOGGER.debug("ProductDesc=> \n" + inboundUSSDMsg.getClobString());

    				List<String> productPricesList = getProductPriceList.apply(productIdsList);
    				insertInUssdTransProdIdMap(userInfo, productIdsList, productPricesList);
    				inboundUSSDMsg.setProdIds(productIdsList);
    				inboundUSSDMsg.setTownName(townInfo.getTownName());
    			}
    		}else {
    			inboundUSSDMsg = getInvalidProductIdLabel(userInfo);
    			inboundUSSDMsg.setTownName(townInfo.getTownName());
    		}
    		userInfo.setTownName(townInfo.getTownName());
    	} else {
    		LOGGER.info("Town not found. Town not under Promotion, MSISDN = " + userInfo.getMsisdn());
    		inboundUSSDMsg = getInvalidProductIdLabel(userInfo);  		
    	}
    	return inboundUSSDMsg;  
    }     
    
    private InboundUssdMessage getInvalidProductIdLabel(UserInfo userInfo) {
        InboundUssdMessage inboundUSSDMsgInvalidID = new InboundUssdMessage();
        LOGGER.debug("ProductIds not found. Sending NOTIFY with default message. MSISDN = " + userInfo.getMsisdn());
        inboundUSSDMsgInvalidID.setIncomingLabel(PROD_IDS_NOT_FOUND);
        return inboundUSSDMsgInvalidID;
    }

    private TemplateDTO getSubMenuTemplateForLocation(int langCode, JSONObject queryBaljson, int productListSize) throws Exception {
        TemplateCache templateCache = TemplateCache.instance();
        TemplateDTO templateDTO = null;
        String subMenuType = mlSubMenuProdTypeMap.get(USER_SEL_4).trim();
        if (ONE == productListSize && subMenuType.equals(queryBaljson.getString(PRODUCT_TYPE))) {
            templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(ML_1_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);

        } else if (TWO == productListSize && subMenuType.equals(queryBaljson.getString(PRODUCT_TYPE))) {
            templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(ML_2_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);

        } else {
            templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(ML_3_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);

        }

        LOGGER.debug(TEMPLATE_DTO + templateDTO);
        return templateDTO;
    }

    private TemplateDTO getSubMenuWithHourly(int langCode, String messageBody, int productListSize) throws Exception {
        TemplateCache templateCache = TemplateCache.instance();
        TemplateDTO templateDTOHourly = null;
        
        if (ONE == productListSize && USER_SEL_4.equals(messageBody)) {
            templateDTOHourly = templateCache.getMLMenu(PropertiesLoader.getValue(ML_1_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);

        } else if (TWO == productListSize && USER_SEL_4.equals(messageBody)) {
            templateDTOHourly = templateCache.getMLMenu(PropertiesLoader.getValue(ML_2_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);
        } else {
            templateDTOHourly = templateCache.getMLMenu(PropertiesLoader.getValue(ML_3_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);

        }

        LOGGER.debug(TEMPLATE_DTO + templateDTOHourly);
        return templateDTOHourly;
    }
    
    private TemplateDTO getSubMenuWithMorning(int langCode, int productListSize) throws Exception {
        TemplateCache templateCache = TemplateCache.instance();
        TemplateDTO templateDTOMorning = null;
        
        if (productListSize == MAX_PRODIDS_CNT_1) {
        	templateDTOMorning = templateCache.getMLMenu(PropertiesLoader.getValue(ML_1_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);

        } else if (MAX_PRODIDS_CNT_2 == productListSize) {
        	templateDTOMorning = templateCache.getMLMenu(PropertiesLoader.getValue(ML_2_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);
        } else {
        	templateDTOMorning = templateCache.getMLMenu(PropertiesLoader.getValue(ML_3_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);

        }
        LOGGER.debug(TEMPLATE_DTO + templateDTOMorning);
        return templateDTOMorning;
    }
    
    private TemplateDTO getSubMenuTownOffers(int langCode, int productListSize) throws Exception {
        TemplateCache templateCache = TemplateCache.instance();
        TemplateDTO templateDTOTown = null;
        
        if (MAX_PRODIDS_CNT_1 == productListSize) {
        	templateDTOTown = templateCache.getMLMenu(PropertiesLoader.getValue(ML_1_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);

        } else if (MAX_PRODIDS_CNT_2 == productListSize) {
        	templateDTOTown = templateCache.getMLMenu(PropertiesLoader.getValue(ML_2_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);
        	
        } else if (THREE == productListSize) {
        	templateDTOTown = templateCache.getMLMenu(PropertiesLoader.getValue(ML_3_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);

        } else if (FOUR == productListSize) {
        	templateDTOTown = templateCache.getMLMenu(PropertiesLoader.getValue(ML_4_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);
        } else {
        	templateDTOTown = templateCache.getMLMenu(PropertiesLoader.getValue(ML_5_OFFER_SUBMENU_TEMPLATE) + "_" + langCode);

        }
        LOGGER.debug(TEMPLATE_DTO + templateDTOTown);
        return templateDTOTown;
    }
   
    public TemplateDTO getLoanMenuTemplate(int langCode) throws Exception {
        TemplateCache templateCache = TemplateCache.instance();
        return templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_LOAN_MENU_TEMPLATE) + "_" + langCode);
    }

    public TemplateDTO getNotEnoughBalanceTemplate(int langCode) throws Exception {
        TemplateCache templateCache = TemplateCache.instance();
        return templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_NOT_ENOUGH_BAL_TEMPLATE) + "_" + langCode);
    }

    public TransmitMessage subMenuMLRequest(Integer sessionId, String messageBody, UserInfo userInfo, boolean isRandom) throws Exception {
        TransmitMessage subMenuMessage = null;
		QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
		JSONObject queryBaljson = queryBaljsonMap.get(sessionId);
        LOGGER.debug("messageBody =>" + messageBody + " isRandom => " + isRandom);
        InboundUssdMessage inboundUssdMessage = null;
        if (isRandom) {
            inboundUssdMessage = getSubMenuForMLUser(messageBody, userInfo);
        } else {
            inboundUssdMessage = getSubMenuForTgtMLUser(messageBody, userInfo);
        }
        if (null != inboundUssdMessage) {
            if (inboundUssdMessage.getIncomingLabel() == PROD_IDS_NOT_FOUND) {
            	queryBaljsonMap.remove(queryBaljson.getInt(USER_MSG_REF));
                return notifyUser(sessionId, userInfo, messageBody, STATUS_PROD_IDS_INSUF, userInfo.getLangCode(), true);
                // sending default langCode french (2) and eligiblity (true)
                // assumimg it's J4U eligible
            } else {
				modifyTemplate(queryBaljson, userInfo.getLangCode(), inboundUssdMessage, false);
                subMenuMessage = new TransmitMessage();
                generateUssdResponse(subMenuMessage, userInfo, inboundUssdMessage, userInfo.getUserMsgRef());
                // setting 2 for ussd log table status
                subMenuMessage.setUssdLogStatus(TWO);
                subMenuMessage.setLogStatus(SUBMENU_RESPONSE_SENT);
                subMenuMessage.setProdIds(inboundUssdMessage.getProdIds());
                LOGGER.debug("inboundUssdMessage.getProdIds() - " + inboundUssdMessage.getProdIds());
                //when target customer getting cache offers
				if(!isRandom)
                subMenuMessage.setProductIdList(inboundUssdMessage.getProductIds());
            }
        }
        return subMenuMessage;
    }

    private TransmitMessage notifyUser(int sessionId, UserInfo userInfo, String messageBody, String userStatus, int langCode, boolean isJFUEligible) throws Exception {
        String msisdn = userInfo.getMsisdn();
        LOGGER.info(msisdn + " :: " + userStatus);
        TransmitMessage messageNotifyUser = null;
        logInfo(sessionId, userInfo, messageBody, userStatus);
        InboundUssdMessage inboundUssdMessageNotifyUser = isJFUEligible ? getNotEnoughProdsMsg(msisdn, langCode) : getUserInEligibleTemplate(langCode);
        if (null != inboundUssdMessageNotifyUser) {
            messageNotifyUser = new TransmitMessage();
            messageNotifyUser.setSourceAddress(userInfo.getDestAddress());
            messageNotifyUser.setDestinationAddress(msisdn);
            messageNotifyUser.setMessageText(inboundUssdMessageNotifyUser.getClobString());
            messageNotifyUser.setTransactionId(userInfo.getTxId());
            messageNotifyUser.setServiceOp(SEVENTEEN);
            messageNotifyUser.setReferenceNumber(sessionId);
            messageNotifyUser.setMlFlag(userInfo.isMlFlag());
            messageNotifyUser.setRandomFlag(userInfo.isRandomFlag());
        }
        return messageNotifyUser;
    }

    public UserInfo getSubMenuForTgtMLUser(JSONObject queryBaljson) throws Exception {
    	InboundUssdMessage inboundUSSDMsgMenu = null;
    	UserInfo userInfoMenu = new UserInfo();
    	LOGGER.debug("getSubMenuForTgtMLUser => ");
    	String msisdn = queryBaljson.getString(MSISDN);
    	String prodType = queryBaljson.getString(PRODUCT_TYPE);
    	Long actBal = queryBaljson.getLong(ACCOUNT_BALANCE);
    	int langCode = queryBaljson.getInt(LANG_CODE);
    	int aaEligible = queryBaljson.has(AA_ELIGIBLE) ? queryBaljson.getInt(AA_ELIGIBLE) : ZERO;
    	int offerRefreshRequired = queryBaljson.has(REFRESH_RF_VALUE) ? queryBaljson.getInt(REFRESH_RF_VALUE) : ZERO;
    	boolean isAAEnabled = PropertiesLoader.getValue(USSD_AA_ENABLED).equalsIgnoreCase(TRUE);

    	setUserInfo(queryBaljson, userInfoMenu, msisdn, langCode);

    	List<String> prodIdsList = new ArrayList<>();
    	List<String> rfsList = new ArrayList<>();

    	AirtimeAdvanceService airtimeAdvanceService = new AirtimeAdvanceService();
    	LOGGER.debug("aaEligible - " + aaEligible);
    	LOGGER.debug("offerRefreshRequired - " + offerRefreshRequired);
    	RankingFormulae rf ;
    	List<OfferParams> offerParamList = new ArrayList<>();
    	Boolean bIsSocial = checkSocialMenu.test(userInfoMenu.getMessageBody()) ;
    	if ((offerRefreshRequired == ONE) && isAAEnabled) {
    		userInfoMenu.setAirtimeAdvBal(queryBaljson.has(AIRTIME_ADVANCE_BALANCE) ? queryBaljson.getLong(AIRTIME_ADVANCE_BALANCE) : ZERO);
    		userInfoMenu.setActBal(actBal);
    		userInfoMenu.setAaEligible(Integer.toString(aaEligible));

    		boolean tesCondition = false;
    		int size = MAX_PRODIDS_CNT_3 ;

    		if (Boolean.FALSE.equals(bIsSocial)) {
    			rf = airtimeAdvanceService.processRFCalculationForCellId(queryBaljson);
    			userInfoMenu.setPoolId(rf.getPoolId());
    			// call the location offer
    			offerParamList = rf.getOfferParams();
    			if (size > offerParamList.size()) {
    				size = offerParamList.size();
    			}
    			StringBuilder prodIdSb = new StringBuilder();
    			if (!offerParamList.isEmpty()) {
    				for (int i = 0; i < size; i++) {
    					OfferParams offParam = offerParamList.get(i);
    					LOGGER.debug("offParam getOfferId - " + i + "_" + offParam.getOfferId());
    					prodIdSb.append(offParam.getOfferId()).append("~").append(offParam.getOfferPrice()).append(",");
    					prodIdsList.add(offParam.getOfferId());
    					rfsList.add(Float.toString(offParam.getRfValue()));
    					userInfoMenu.getAaEligibleProdIdProdPriceMap().put(offParam.getOfferId(), offParam.getOfferPrice());
    				}
    			}
    			String subMenuType = mlSubMenuProdTypeMap.get(USER_SEL_4).trim();         //check for social?
    			if (null != rf.getPoolId() && !rf.getPoolId().isEmpty()) {
    				tesCondition = !offerParamList.isEmpty() && (prodIdsList.size() == THREE ||
    						(prodIdsList.size() < THREE && subMenuType.equals(queryBaljson.getString(PRODUCT_TYPE))));
    			}
    		}
    		LOGGER.debug("testCondition= >" + tesCondition);
    		if (tesCondition && Boolean.FALSE.equals(bIsSocial)) {
    			return locationOfferAllocationThreeOrLess(queryBaljson, prodIdsList, rfsList, userInfoMenu, offerParamList);

    		} else {  //normal ml offer
    			LOGGER.debug("location offer not present calling normal ml offer=>");
    			prodIdsList.clear() ;
    			rfsList.clear();
    			String offerRefFlag = queryBaljson.getString(OFFER_REFRESH_FLAG);
    			String selectedOfferFlag = mlRefreshFlagMap.get(queryBaljson.getString(USER_SELECTION));
    			LOGGER.debug("Offer Refresh flag :: > " + offerRefFlag);
    			LOGGER.debug("selected Offer Flag :: > " + selectedOfferFlag);

    			if (queryBaljson.getString(OFFER_REFRESH_FLAG).equalsIgnoreCase(VALUE_N) || queryBaljson.getString(OFFER_REFRESH_FLAG).indexOf(selectedOfferFlag) == -1) {
    				LOGGER.info("OFFER_REFRESH_FLAG = N OR index of OFFER_REFRESH_FLAG = -1 --> Condition is true  receiving  Cache offers =>");
    				TransmitMessage message = subMenuMLRequest(userInfoMenu.getUserMsgRef(), userInfoMenu.getMessageBody(), userInfoMenu, false);
    				   
    				userInfoMenu.setUpdateCCR(false);
    				userInfoMenu.setMlFlag(true);
					userInfoMenu.setServiceOp(message.getServiceOp());
					userInfoMenu.setProdIds(message.getProductIdList());
					userInfoMenu.setMessageBody(message.getMessageText());
    				return userInfoMenu;
    			} else {
    				rf = airtimeAdvanceService.processRFRequest(queryBaljson);
    				offerParamList = rf.getOfferParams();
    				size = MAX_PRODIDS_CNT_3;
    				if (size > offerParamList.size()) {
    					size = offerParamList.size();
    				}
    				for (int i = 0; i < size; i++) {
    					OfferParams offParam = offerParamList.get(i);
    					LOGGER.debug("offParam getOfferId - " + i + ") " + offParam.getOfferId());
    					prodIdsList.add(offParam.getOfferId());
    					rfsList.add(Float.toString(offParam.getRfValue()));
    					userInfoMenu.getAaEligibleProdIdProdPriceMap().put(offParam.getOfferId(), offParam.getOfferPrice());
    				}
    				userInfoMenu.setAirtimeAdvBal(queryBaljson.has(AIRTIME_ADVANCE_BALANCE) ? queryBaljson.getLong(AIRTIME_ADVANCE_BALANCE) : ZERO);
    				userInfoMenu.setActBal(actBal);
    				userInfoMenu.setAaEligible(Integer.toString(aaEligible));
    			}
    		}

    	} else {
    		if (Boolean.TRUE.equals(bIsSocial)) {
    			prodIdsList = lookUpDAO.getSocialOffersByBalanceAndRank(msisdn, actBal);
    			prodIdsList = (prodIdsList.size() == MAX_PRODIDS_CNT_3) ? prodIdsList : lookUpDAO.getSocialOffersByExpectedValue (msisdn, prodIdsList);
    		} else {
    			prodIdsList = lookUpDAO.getOffersByBalanceAndRank(msisdn, prodType, actBal);
    			prodIdsList = (prodIdsList.size() == MAX_PRODIDS_CNT_3) ? prodIdsList : lookUpDAO.getOffersByExpectedValue(msisdn, prodType, prodIdsList);
    		}
    		userInfoMenu.setAirtimeAdvBal(queryBaljson.has(AIRTIME_ADVANCE_BALANCE) ? queryBaljson.getLong(AIRTIME_ADVANCE_BALANCE) : ZERO);
    		userInfoMenu.setActBal(actBal);
    		userInfoMenu.setAaEligible(Integer.toString(aaEligible));
    	}

    	if (!prodIdsList.isEmpty()) {
    		// provison offer
    		userInfoMenu = offerAllocationThreeOrLess(queryBaljson, prodIdsList, rfsList, userInfoMenu);
    	} else {
    		inboundUSSDMsgMenu = getNotEnoughProdsMsg(msisdn, langCode);
    		if (null != inboundUSSDMsgMenu) {
    			userInfoMenu.setServiceOp(SEVENTEEN);
    			userInfoMenu.setMessageBody(inboundUSSDMsgMenu.getClobString());
    			userInfoMenu.setMlFlag(true);
    		}
    	}

    	return userInfoMenu;
    }
    

    private void createEntryInTrxProdIdMap(UserInfo userInfo, List<String> prodIdsList) throws Exception {
    	LOGGER.debug(CREATING_ENTRY_IN_ECMP_T_USSD_TRX_PRODID_MAP_TABLE);

    	int prodIdsListSize = prodIdsList.size();

    	if (prodIdsListSize == MAX_PRODIDS_CNT_3) {
    		updaterDAO.insertTrxProdIdMap(userInfo, prodIdsList.get(ZERO), prodIdsList.get(ONE), prodIdsList.get(TWO));
    		userInfo.setOfferCount(MAX_PRODIDS_CNT_3);
    	} else if (prodIdsListSize == MAX_PRODIDS_CNT_2) {
    		updaterDAO.insertTrxProdIdMap(userInfo, prodIdsList.get(ZERO), prodIdsList.get(ONE), null);
    		userInfo.setOfferCount(MAX_PRODIDS_CNT_2);
    	} else if (prodIdsListSize == MAX_PRODIDS_CNT_1) {
    		updaterDAO.insertTrxProdIdMap(userInfo, prodIdsList.get(ZERO), null, null);
    		userInfo.setOfferCount(MAX_PRODIDS_CNT_2);
    	}
    }


    public UserInfo offerAllocationThreeOrLess(JSONObject queryBaljson, List<String> prodIdsList, List<String> rfsList, UserInfo userInfo) throws Exception {
        InboundUssdMessage inboundUSSDMsgOffer = null;
        LOGGER.debug("offerAllocationThreeOrLess => ");
        String msisdn = queryBaljson.getString(MSISDN);
        String shortMsg = queryBaljson.getString(USER_SELECTION);
        int langCode = queryBaljson.getInt(LANG_CODE);
		QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
		JSONObject queryBal = queryBaljsonMap.get(queryBaljson.getInt(USER_MSG_REF));
        String selectedOfferFlag = mlRefreshFlagMap.get(queryBaljson.getString(USER_SELECTION));
        
        if (!"H".equals(selectedOfferFlag) && prodIdsList.size() < THREE) {
	        setUserInfo(queryBaljson, userInfo, msisdn, langCode);
	        inboundUSSDMsgOffer = getNotEnoughProdsMsg(msisdn, langCode);
            if (null != inboundUSSDMsgOffer) {
            	userInfo.setServiceOp(SEVENTEEN);
            	userInfo.setMessageBody(inboundUSSDMsgOffer.getClobString());
            	userInfo.setMlFlag(true);
            	queryBaljsonMap.remove(queryBaljson.getInt(USER_MSG_REF));
            }
        } else {
	        setUserInfo(queryBaljson, userInfo, msisdn, langCode);
	        TemplateDTO templateDTO = getSubMenuTemplateForLocation(langCode, queryBaljson, prodIdsList.size());
	        ProductInfoCache productInfoCache = ProductInfoCache.instance();

	        if (null != templateDTO) {
	        	if(checkSocialMenu.test(shortMsg)) {	        		
					inboundUSSDMsgOffer = generateSubMenuSocial(langCode, prodIdsList, productInfoCache, templateDTO);
	        	} else {
	        		 inboundUSSDMsgOffer = generateSubMenu(langCode, prodIdsList, productInfoCache, templateDTO);
	        	}	        		
	           
	            userInfo.setMessageBody(inboundUSSDMsgOffer.getClobString());
	            userInfo.setMlFlag(true);
	            userInfo.setUpdateCCR(true);
	
	            String subMenuType = mlSubMenuProdTypeMap.get(shortMsg);
	
	            // creating entry in ECMP_T_USSD_TRX_PRODID_MAP table
	            createEntryInTrxProdIdMap(userInfo, prodIdsList);
	            
	            StringBuilder prodIdSb = new StringBuilder();
	            for (String prodId : prodIdsList) {
	                if ((null != userInfo.getAaEligibleProdIdProdPriceMap()) && (userInfo.getAaEligibleProdIdProdPriceMap().containsKey(prodId))) {
	                    long productPrice = userInfo.getAaEligibleProdIdProdPriceMap().get(prodId);
	                    prodIdSb.append(prodId).append("~").append(productPrice).append(",");
	                    LOGGER.debug("prodIdSb 1 - " + prodIdSb);
	                } else {
	                    prodIdSb.append(prodId).append(",");
	                    LOGGER.debug("prodIdSb 2 - " + prodIdSb);
	                }
	            }
	
	            if (!rfsList.isEmpty()) {
	                StringBuilder prodRfSb = new StringBuilder();
	                for (String prodRf : rfsList) {
	                    prodRfSb = prodRfSb.append(prodRf + ",");
	                    LOGGER.debug("prodRfSb 2 - " + prodRfSb);
	                }
	
	                // creating entry in ENBA_T_J4U_ML_OFFER_MSG table
	                LOGGER.debug(" creating entry in ENBA_T_J4U_ML_OFFER_MSG table=>");
	                updaterDAO.upsertMLOfferMsg(userInfo, subMenuType, prodIdSb.deleteCharAt(prodIdSb.length() - 1).toString(), prodRfSb.deleteCharAt(prodRfSb.length() - 1).toString());
	            } else {
	                LOGGER.debug(" creating entry in ENBA_T_J4U_ML_OFFER_MSG table=>");
	                updaterDAO.upsertMLOfferMsg(userInfo, subMenuType, prodIdSb.deleteCharAt(prodIdSb.length() - 1).toString(), null);
	            }
	
	            LOGGER.debug("Selected ProdIds for the msisdn = " + msisdn + " :: " + prodIdsList);
	            if (!rfsList.isEmpty()) {
	                List<String> prodIdRfList = new ArrayList<>();
	                prodIdRfList.addAll(prodIdsList);
	                prodIdRfList.addAll(rfsList);
	                userInfo.setProdIds(prodIdRfList);
	            } else {
	                userInfo.setProdIds(prodIdsList);
	            }
				modifyTemplate(queryBal, langCode, inboundUSSDMsgOffer, false);
				userInfo.setMessageBody(inboundUSSDMsgOffer.getClobString());
	        }
        }
        return userInfo;
    }

    public UserInfo locationOfferAllocationThreeOrLess(JSONObject queryBaljson, List<String> prodIdsList, List<String> rfsList, UserInfo userInfo, List<OfferParams> offerParamList) throws Exception {
        InboundUssdMessage inboundUSSDMsgLocation = null;
        LOGGER.debug("locationOfferAllocationThreeOrLess => ");
        String msisdn = queryBaljson.getString(MSISDN);
        int langCode = queryBaljson.getInt(LANG_CODE);
        setUserInfo(queryBaljson, userInfo, msisdn, langCode);
        TemplateDTO templateDTO = getSubMenuTemplateForLocation(langCode, queryBaljson, offerParamList.size());

        if (null != templateDTO) {
            inboundUSSDMsgLocation = generateSubMenuForLocation(offerParamList, templateDTO);
			QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
			JSONObject queryBal = queryBaljsonMap.get(queryBaljson.getInt(USER_MSG_REF));
			modifyTemplate(queryBal, langCode, inboundUSSDMsgLocation, false);
            LOGGER.debug("Message=>" + inboundUSSDMsgLocation.getClobString());
            userInfo.setMessageBody(inboundUSSDMsgLocation.getClobString());
            userInfo.setMlFlag(true);
            userInfo.setUpdateCCR(false);
            createEntryInTrxProdIdMap(userInfo, prodIdsList);

            if (!rfsList.isEmpty()) {
                List<String> prodIdRfList = new ArrayList<>();
                prodIdRfList.addAll(prodIdsList);
                prodIdRfList.addAll(rfsList);
                userInfo.setProdIds(prodIdRfList);
            } else {
                userInfo.setProdIds(prodIdsList);
            }
        }
        return userInfo;
    }

    private void setUserInfo(JSONObject queryBaljson, UserInfo userInfo, String msisdn, int langCode) {
        userInfo.setMsisdn(msisdn);
        userInfo.setJFUEligible(true);
        userInfo.setDestAddress(queryBaljson.getString(DEST_ADDRESS));
        userInfo.setTxId(queryBaljson.getString(TRANSACTION_ID));
        userInfo.setUserMsgRef(queryBaljson.getInt(USER_MSG_REF));
        userInfo.setOfferRefreshFlag(queryBaljson.getString(OFFER_REFRESH_FLAG));
        userInfo.setSelProdType(queryBaljson.getString(USER_SELECTION));
        userInfo.setLangCode(Integer.toString(langCode));
		userInfo.setServiceOp(TWO);
		userInfo.setMessageBody(queryBaljson.getString(USER_SELECTION));
		userInfo.setCellId(queryBaljson.getString(CELL_ID_KEY_NAME));
		userInfo.setPoolId(queryBaljson.getString(POOL_ID));
		userInfo.setlStartTime(queryBaljson.getLong(REQUEST_STARTTIME));
    }

    private InboundUssdMessage generateSubMenuForLocation(List<OfferParams> offerParamList, TemplateDTO templateDTO) throws Exception {
        InboundUssdMessage inboundUSSDMsgSubMenu = new InboundUssdMessage();
        String[] prodIdsOrder = templateDTO.getOfferOrderCSV().split(",");
        String template = templateDTO.getTemplate();
        for (int i = 0; i < prodIdsOrder.length; i++) {
            String offerPattern = "@" + prodIdsOrder[i] + "@";
            String replacement = offerParamList.get(i).getProductDescription();
            template = template.replace(offerPattern, replacement);
        }
        inboundUSSDMsgSubMenu.setClobString(template);
        LOGGER.debug("inboundUSSDMsg" + inboundUSSDMsgSubMenu);
        return inboundUSSDMsgSubMenu;

    }

    private InboundUssdMessage generateSubMenu(int langCode, List<String> prodIdsList, ProductInfoCache productInfoCache, TemplateDTO templateDTO) throws Exception {
        InboundUssdMessage inboundUSSDMsg = new InboundUssdMessage();
        String[] prodIdsOrder = templateDTO.getOfferOrderCSV().split(",");
        String template = templateDTO.getTemplate();
        for (int i = 0; i < prodIdsOrder.length; i++) {
            String offerPattern = "@" + prodIdsOrder[i] + "@";
            String replacement = productInfoCache.get(prodIdsList.get(i) + "_" + langCode).getProductDesc();
            template = template.replace(offerPattern, replacement);
        }
        inboundUSSDMsg.setClobString(template);
        return inboundUSSDMsg;
    }
    
    private InboundUssdMessage generateSubMenuSocial(int langCode, List<String> prodIdsList, ProductInfoCache socialProductInfoCache, TemplateDTO templateDTO) throws Exception {
        InboundUssdMessage inboundUSSDMsg = new InboundUssdMessage();
        String[] prodIdsOrder = templateDTO.getOfferOrderCSV().split(",");
        String template = templateDTO.getTemplate();
        for (int i = 0; i < prodIdsOrder.length; i++) {
            String offerPattern = "@" + prodIdsOrder[i] + "@";
            String replacement = socialProductInfoCache.getSocial(prodIdsList.get(i) + "_" + langCode).getProductDesc();
            template = template.replace(offerPattern, replacement);
        }
        inboundUSSDMsg.setClobString(template);
        return inboundUSSDMsg;
    }
    
    private InboundUssdMessage generateSubMenuMorning(int langCode, List<String> prodIdsList, ProductInfoCache morningProductInfoCache, TemplateDTO templateDTO) throws Exception {
        InboundUssdMessage inboundUSSDMsg = new InboundUssdMessage();
        String[] prodIdsOrder = templateDTO.getOfferOrderCSV().split(",");
        String template = templateDTO.getTemplate();
        for (int i = 0; i < prodIdsOrder.length; i++) {
            String offerPattern = "@" + prodIdsOrder[i] + "@";
            String replacement = morningProductInfoCache.getMorning(prodIdsList.get(i) + "_" + langCode).getProductDesc();
            template = template.replace(offerPattern, replacement);
        }
        inboundUSSDMsg.setClobString(template);
        return inboundUSSDMsg;
    }
    
    private InboundUssdMessage generateTownSubMenu(int langCode, List<String> prodIdsList, ProductInfoCache townProductInfoCache, TemplateDTO templateDTO) throws Exception {
        InboundUssdMessage inboundUSSDMsg = new InboundUssdMessage();
        String[] prodIdsOrder = templateDTO.getOfferOrderCSV().split(",");
        String template = templateDTO.getTemplate();
        for (int i = 0; i < prodIdsOrder.length; i++) {
            String offerPattern = "@" + prodIdsOrder[i] + "@";
            String replacement = townProductInfoCache.getTownProdInfo(prodIdsList.get(i) + "_" + langCode).getProductDesc();
            template = template.replace(offerPattern, replacement);
        }
        inboundUSSDMsg.setClobString(template);
        return inboundUSSDMsg;
    }
    
    public InboundUssdMessage generateLoanMenu(String loanAmount, TemplateDTO templateDTO) throws Exception {
        InboundUssdMessage inboundUSSDMsg = new InboundUssdMessage();
        String template = templateDTO.getTemplate();
        String loanAmountStr = templateDTO.getOfferOrderCSV();
        String loanAmountPattern = "@" + loanAmountStr + "@";
        template = template.replace(loanAmountPattern, loanAmount);
        inboundUSSDMsg.setClobString(template);
        return inboundUSSDMsg;
    }

    public InboundUssdMessage generateNotEnoughBalMessage(TemplateDTO templateDTO) throws Exception {
        InboundUssdMessage inboundUSSDMsg = new InboundUssdMessage();
        String template = templateDTO.getTemplate();
        inboundUSSDMsg.setClobString(template);
        return inboundUSSDMsg;
    }

    /****
     * Generates submenu from Cache for ML target users.
     * @param messageBody
     * @param userInfo
     * @return
     * @throws Exception
     */   
    public InboundUssdMessage getSubMenuForTgtMLUser(String messageBody, UserInfo userInfo) throws Exception {
        InboundUssdMessage inboundUSSDMsg = null;
        LOGGER.debug("MSISDN=>" + userInfo.getMsisdn());
        MLOfferMsg mlOfferMsg  = 
            lookUpDAO.getSubMenuForTgtMLUser(mlSubMenuProdTypeMap.get(messageBody), userInfo.getMsisdn());
        if (null != mlOfferMsg) {
            LOGGER.debug(" mlOfferMsg = > " + mlOfferMsg.getMenuContent());
            inboundUSSDMsg = new InboundUssdMessage();
            inboundUSSDMsg.setClobString(mlOfferMsg.getMenuContent());

            // creating entry in ECMP_T_USSD_TRX_PRODID_MAP table
            String[] prodDetailsAry = mlOfferMsg.getProdIds();
            String[] prodRFValuesAry = mlOfferMsg.getRfValues();
            LOGGER.debug(CREATING_ENTRY_IN_ECMP_T_USSD_TRX_PRODID_MAP_TABLE);
            if (prodDetailsAry.length == MAX_PRODIDS_CNT_3) {
                updaterDAO.insertTrxProdIdMap(userInfo, prodDetailsAry[ZERO], prodDetailsAry[ONE], prodDetailsAry[TWO]);                
            } else if (prodDetailsAry.length == MAX_PRODIDS_CNT_2) {
                updaterDAO.insertTrxProdIdMap(userInfo, prodDetailsAry[ZERO], prodDetailsAry[ONE], null);                
            } else if (prodDetailsAry.length == MAX_PRODIDS_CNT_1) {
                updaterDAO.insertTrxProdIdMap(userInfo, prodDetailsAry[ZERO], null, null);                
            }
            String[] prodIdsAry = new String[prodDetailsAry.length];
            for (int i = 0; i < prodDetailsAry.length; i++) {
                if (prodDetailsAry[i].split("~").length == TWO) {
                    prodIdsAry[i] = prodDetailsAry[i].split("~")[ZERO];
                } else {
                    prodIdsAry[i] = prodDetailsAry[i];
                }
            }
            LOGGER.info("prodRFValuesAry - " + prodRFValuesAry);
            if (null != prodRFValuesAry) {
                String[] prodIdRFsAry = new String[prodIdsAry.length + prodRFValuesAry.length];
                System.arraycopy(prodIdsAry, ZERO, prodIdRFsAry, ZERO, prodIdsAry.length);
                System.arraycopy(prodRFValuesAry, ZERO, prodIdRFsAry, prodIdsAry.length, prodRFValuesAry.length);
                inboundUSSDMsg.setProdIds(Arrays.asList(prodIdRFsAry));
                inboundUSSDMsg.setProductIds(Arrays.asList(prodIdRFsAry));
                LOGGER.info("prodIdRFsAry - " + Arrays.toString(prodIdRFsAry));
            } else {
                inboundUSSDMsg.setProdIds(Arrays.asList(prodIdsAry));
                inboundUSSDMsg.setProductIds(Arrays.asList(prodIdsAry));
                LOGGER.info("prodIdsAry - " + Arrays.toString(prodIdsAry));
            }

        } else {
            inboundUSSDMsg = getInvalidProductIdLabel(userInfo);
        }

        return inboundUSSDMsg;
    } 
    
    public InboundUssdMessage getUserInEligibleTemplate(int langCode) throws Exception {
        return getMLMenuTemplate(PropertiesLoader.getValue(USER_IN_ELIGIBLE_TEMPLATE), langCode);
    }

    public InboundUssdMessage getNotEnoughProdsTemplateNew(int langCode) throws Exception {
        return getMLMenuTemplate(PropertiesLoader.getValue(USSD_ML_NO_AVAILABLE_PRODS_TEMPLATE), langCode);
    }
    public InboundUssdMessage getConsentMenuTemplate(int langCode) throws Exception {
        return getMLMenuTemplate(PropertiesLoader.getValue(USSD_ML_CONSENT_MENU_TEMPLATE), langCode);
    }
    public InboundUssdMessage getNotEnoughProdsMsg(String msisdn, int langCode) throws Exception {
        InboundUssdMessage inboundUSSDMsgPrice = null;
        LOGGER.info("Not Enough Prods for the user. Sending NOTIFY with default message. MSISDN :: " + msisdn);
        TemplateCache templateCache = TemplateCache.instance();
        TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_NOT_ENOUGH_PRODS_TEMPLATE) + "_" + langCode);
        if (null != templateDTO) {
            inboundUSSDMsgPrice = new InboundUssdMessage();
            inboundUSSDMsgPrice.setClobString(templateDTO.getTemplate());
            inboundUSSDMsgPrice.setIncomingLabel(PROD_IDS_NOT_FOUND);
        } else {
            LOGGER.error(TEMPLATE_NOT_FOUND + PropertiesLoader.getValue(USSD_ML_NOT_ENOUGH_PRODS_TEMPLATE)+ "_" + langCode);
        }

        return inboundUSSDMsgPrice;
    }
    
    public InboundUssdMessage getMorningOfferFailureMsg(String msisdn, int langCode) throws Exception {
        InboundUssdMessage inboundUSSDMsgTemplate = null;
        LOGGER.info("MorningOfferEligibility failed/ ProdIds not found. Sending - Try again tomorrow msg :: " + msisdn);
        TemplateCache templateCache = TemplateCache.instance();       
        TemplateDTO templateDTO = templateCache.getMLMenu(PropertiesLoader.getValue(USSD_ML_MO_FAILURE_MSG_TEMPLATE)+ "_" + langCode);
        if (null != templateDTO) {
            inboundUSSDMsgTemplate = new InboundUssdMessage();            
            inboundUSSDMsgTemplate.setClobString(templateDTO.getTemplate());
        } else {
            LOGGER.error(TEMPLATE_NOT_FOUND + PropertiesLoader.getValue(USSD_ML_MO_FAILURE_MSG_TEMPLATE)+ "_" + langCode);
        }
        return inboundUSSDMsgTemplate;
    }
    
    private InboundUssdMessage getNoOffersAvailableFailureMsg(String msisdn, String templateId, int langCode) throws Exception {
        InboundUssdMessage inboundUSSDMsgTemplate = null;
        LOGGER.info("ProdIds not found. Sending - Try again tomorrow msg :: " + msisdn);
        TemplateCache templateCache = TemplateCache.instance();       
        TemplateDTO templateDTO = templateCache.getMLMenu(templateId + "_" + langCode);
        if (null != templateDTO) {
            inboundUSSDMsgTemplate = new InboundUssdMessage();            
            inboundUSSDMsgTemplate.setClobString(templateDTO.getTemplate());
        } else {
            LOGGER.error(TEMPLATE_NOT_FOUND + templateId + "_" + langCode);
        }
        return inboundUSSDMsgTemplate;
    }

    public InboundUssdMessage getErrorMenu(int langCode) throws Exception {
        DefaultMessageCache defaultMessageCache = DefaultMessageCache.instance();
        InboundUssdMessage inboundUSSDMsg = new InboundUssdMessage();
        inboundUSSDMsg.setClobString(defaultMessageCache.get(langCode).getErrorMsg());
        return inboundUSSDMsg;
    }

    private void lookUpTemplateDetails(String templateId, MessageDTO messageDTO) throws Exception {
        TemplateCache templateCache = TemplateCache.instance();
        TemplateDTO templateDTO = templateCache.get(templateId);
        if (null != templateDTO) {
        	messageDTO.setTemplate(templateDTO.getTemplate());
        	String offerOrderCSV = templateDTO.getOfferOrderCSV();
        	if(null != offerOrderCSV) {
        		messageDTO.setOfferSequence(offerOrderCSV.split(","));
        	}
            
        } else {
            LOGGER.error("Template details could not be found for id:: " + templateId);
        }
    }

    private String getMenuMessageForTemplate(MessageDTO messageDTO) throws Exception {
        String langCdSuffix = "_" + messageDTO.getLangCode();
        ProductInfoCache cache = ProductInfoCache.instance();
        String template = messageDTO.getTemplate();
        int length = messageDTO.getOfferSequence().length;

        if (null != template && null != messageDTO.getOfferSequence()) {
            for (int i = 0; i < length; i++) {
                String offerPattern = "@" + messageDTO.getOfferSequence()[i] + "@";
                LOGGER.debug("offerPattern => " + offerPattern);
                LOGGER.debug("offerPattern with language Id => " + messageDTO.getOfferSequence()[i] + langCdSuffix);
                LOGGER.debug("Product Id with language Id => " + messageDTO.getProducts().get(messageDTO.getOfferSequence()[i]) + langCdSuffix);
                String replacement = cache.get(messageDTO.getProducts().get(messageDTO.getOfferSequence()[i]) + langCdSuffix).getProductDesc();
                LOGGER.debug("replacement => " + replacement);

                template = template.replace(offerPattern, replacement);
            }
        } else {
            LOGGER.error("Template details / Template offerSequence not found for the id :: " + messageDTO.getTemplateId());
        }
        LOGGER.debug("template =>" + template);
        return template;
    }    

    public InboundUssdMessage respondToUserRequest(UssdMessage ussdMessage, UssdSession ussdSession) {
    	MessageDTO messageDTO = new MessageDTO();
    	messageDTO.setUssdMsgId(ussdMessage.getSessionId());
    	messageDTO.setTxnId(ussdMessage.getTransactionId());
    	messageDTO.setMsisdn(ussdMessage.getSourceAddress());
    	messageDTO.setUssdShortCode(ussdMessage.getDestinationAddress());
    	messageDTO.setMessageText(ussdMessage.getMessageText());
    	InboundUssdMessage inboundUSSDMsg = new InboundUssdMessage();
    	DefaultMessageCache defaultMessageCache = DefaultMessageCache.instance();
    	inboundUSSDMsg.setIncomingLabel(UssdMessage.NOTIFY);
    	try {        	
    		lookUpDAO.lookupOffers(messageDTO);  
    		int userSelection;           
    		DefaultMessageDTO defaultMsgDTO = defaultMessageCache.get(messageDTO.getLangCode());
    		try {
    			userSelection = Integer.valueOf(ussdMessage.getMessageText());   
    			if (userSelection < PropertiesLoader.getIntValue(MENU_MIN) || userSelection > PropertiesLoader.getIntValue(MENU_MAX)) {
    				throw new NumberFormatException("Incorrect user input");
    			}
    		} catch (NumberFormatException nfe) {
    			// If user entered something other than what is expected for J4U
    			LOGGER.error("User input was invalid :: " + ussdMessage.getMessageText() + " Sending error message back as NOTIFY", nfe);
    			String errorMsg = defaultMsgDTO.getErrorMsg();
    			inboundUSSDMsg.setClobString(errorMsg);
    			return inboundUSSDMsg;
    		}
    		String productId = lookUpDAO.getProductForUserSelection(ussdMessage, userSelection);
    		LOGGER.info("ProductId value: ");
    		messageDTO.setSelectedProdcutId(productId);

    		// publish message to Rewards topic
    		JSONObject message = createMessageForRewardsPlugin(messageDTO);            

    		if (PropertiesLoader.getIntValue(USSD_ENABLE_MULTI_CS) == ONE) {
    			csType = lookUpDAO.getCSType(ussdSession.getMsisdn());
    			if (csType.equalsIgnoreCase(USSDConstants.CS_TYPE_OP)) {
    				publishToTopic(PropertiesLoader.getValue(OP_REWARDSPUB_TOPIC_NAME), message.toString());
    				LOGGER.info("Published message on OP_rewards (openet) topic: "+PropertiesLoader.getValue(OP_REWARDSPUB_TOPIC_NAME)+ ", JSONObject: " + message);
    			} else {
    				publishToTopic(PropertiesLoader.getValue(REWARDSPUB_TOPIC_NAME), message.toString());
    				LOGGER.info("Published message on rewards topic: " +PropertiesLoader.getValue(REWARDSPUB_TOPIC_NAME)+ ". JSONObject: "+ message);
    			}
    		} else {
    			publishToTopic(PropertiesLoader.getValue(REWARDSPUB_TOPIC_NAME), message.toString());
    			LOGGER.info("Published message on rewards topic: " +PropertiesLoader.getValue(REWARDSPUB_TOPIC_NAME)+ ", JSONObject: "+ message);
    		}
    		// Choose the default message for closing conversation
    		String defaultMessage = defaultMsgDTO.getDefaultMsg();
    		inboundUSSDMsg.setClobString(defaultMessage);
    		inboundUSSDMsg.setSelProdId(productId);
    	} catch (Exception ex) {
    		LOGGER.error("Exception occured at:: " + ex.getMessage(), ex);
    	}
    	return inboundUSSDMsg;
    }

    public InboundUssdMessage finalReqReceivedForMLUser(String messageBody, UserInfo userInfo, boolean isMPesaReq, boolean provFlag) throws Exception {
        int userSelection;
        InboundUssdMessage inboundUSSDMsg = new InboundUssdMessage();
        DefaultMessageCache defaultMessageCache = DefaultMessageCache.instance();
        DefaultMessageDTO defaultMsgDTO = defaultMessageCache.get(userInfo.getLangCode());
        try {
    		userSelection = userInfo.getSelectionProdOption();
        } catch (NumberFormatException nfe) {
            // If user entered something other than what is expected for ML
            LOGGER.info("User input was invalid :: " + messageBody + " Sending error message back as NOTIFY");
            String errorMsg = defaultMsgDTO.getErrorMsg();
            inboundUSSDMsg.setClobString(errorMsg);
            return inboundUSSDMsg;
        }
        // userInfo.g

        String productId = lookUpDAO.getProductForUserSelection(userInfo, userSelection);
        LOGGER.debug("finalReqReceivedForMLUser => userSelection - " + userSelection);
        LOGGER.debug("provFlag - " + provFlag);
        LOGGER.debug("isMPesaReq - " + isMPesaReq);
        LOGGER.debug("productId - " + productId);
        LOGGER.debug("productIdArray - " + userInfo.getProdIds());

    	String prodPrice = null;
    	if (productId.split("~").length == TWO) {
    		prodPrice = productId.split("~")[ONE];
    		productId = productId.split("~")[ZERO];
    	}

    	if (null == prodPrice) {
    		ProductPriceCache priceCache = ProductPriceCache.instance() ;
    		prodPrice = priceCache.get(productId);
    	}

        if (null != productId) {
        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 
        	LocalDateTime startDate = LocalDateTime.now();
        	String startTime = startDate.format(formatter);
            if (isMPesaReq) {
                LOGGER.debug("getLangCode :: " + userInfo.getLangCode());
                String key = productId + "_" + ONE_1;
                if (userInfo.getLangCode() == TWO) {
                    key = productId + "_" + TWO_2;
                }

                LOGGER.debug("added product description using key :: " + key);
                String productDescription = ProductInfoCache.instance().get(key).getProductDesc();
                LOGGER.debug("added product description using key :: " + key + " DESC :: " + productDescription);

    			if (null != prodPrice) {
    				// publish message to MPesa topic
    				LOGGER.debug("isMPesaReq prdPrice - " + prodPrice);
    				JSONObject message = createReqForRewardsOrMPesa(userInfo, productId, messageBody, prodPrice, isMPesaReq, provFlag, productDescription, startTime);
    				publishToTopic(PropertiesLoader.getValue(MPESA_TOPIC_NAME), message.toString());
    				LOGGER.info("Published message for Ml user on MPesa topic :: " + 
    						PropertiesLoader.getValue(MPESA_TOPIC_NAME) +" :: " + message);    				
    			} else {
    				LOGGER.error("Product price not found for the productId = " + productId + " :: msisdn = " + userInfo.getMsisdn());
    				return getNotEnoughProdsMsg(userInfo.getMsisdn(), userInfo.getLangCode());
    			}
    		} else {
    			// publish message to Rewards topic
    			LOGGER.debug(" NOT MPesaReq, prodPrice - " + prodPrice);
    			JSONObject message = createReqForRewardsOrMPesa(userInfo, productId, messageBody, prodPrice, isMPesaReq, provFlag, null, startTime);

    			if (PropertiesLoader.getIntValue(USSD_ENABLE_MULTI_CS) == ONE) {
    				csType = lookUpDAO.getCSType(userInfo.getMsisdn());
    				if (csType.equalsIgnoreCase(USSDConstants.CS_TYPE_OP)) {
    					publishToTopic(PropertiesLoader.getValue(OP_REWARDSPUB_TOPIC_NAME), message.toString());
    					LOGGER.info("Published message on Openet OP_rewards topic for Ml user :: " + message);
    				} else {
    					publishToTopic(PropertiesLoader.getValue(REWARDSPUB_TOPIC_NAME), message.toString());
    					LOGGER.info("Published message on rewards topic for Ml user :: " + message);
    				}
    			} else {
    				publishToTopic(PropertiesLoader.getValue(REWARDSPUB_TOPIC_NAME), message.toString());    		
    				LOGGER.info("Published message for Ml user on rewards topic :: " + 
    						PropertiesLoader.getValue(REWARDSPUB_TOPIC_NAME) +" :: " + message);  
    			}
    		}

            // Choose the default message for closing conversation
            String defaultMessage = isMPesaReq ? getMLMenuTemplate(PropertiesLoader.getValue(USSD_MPESA_CONF_MENU), userInfo.getLangCode()).getClobString() : defaultMsgDTO.getDefaultMsg();
            inboundUSSDMsg.setClobString(defaultMessage);
            inboundUSSDMsg.setSelProdId(productId);
            return inboundUSSDMsg;
        } else {
            return getNotEnoughProdsMsg(userInfo.getMsisdn(), userInfo.getLangCode());
        }

    }
    
    public InboundUssdMessage finalReqReceivedForMOUser(UserInfo userInfo) throws Exception {

    	InboundUssdMessage inboundUSSDMsgTemplate = new InboundUssdMessage();
    	LOGGER.debug("Final Request recieved for activating the offer :: " + userInfo.getMsisdn());

    	DefaultMessageCache defaultMessageCache = DefaultMessageCache.instance();
    	DefaultMessageDTO defaultMsgDTO = defaultMessageCache.get(userInfo.getLangCode());

    	if (null != defaultMsgDTO) {    		
    		// Choose the default message(SUCCESSmsg) for closing conversation
    		String defaultMessage = defaultMsgDTO.getDefaultMsg();
    		inboundUSSDMsgTemplate.setClobString(defaultMessage);
    	} else {
    		inboundUSSDMsgTemplate.setClobString(CONFIRMATION_MSG_ERROR);
    		LOGGER.error("Default Confirmation Msg could not be loaded from table ECMP_T_DEFAULT_MESSAGE! ");
    	}

    	int userSelection = userInfo.getSelectionProdOption();
    	String productId = lookUpDAO.getProductForUserSelection(userInfo, userSelection);
    	if(null != productId) {
    		LOGGER.debug("productId - " + productId);
    		LOGGER.debug("productId.split(~).length = " + productId.split("~").length);    	
    		if (productId.split("~").length == TWO) {
    			LOGGER.debug("Its combination of both productId & productPrice.");    		
    			productId = productId.split("~")[ZERO];
    		}

    		LOGGER.debug("productId - " + productId);
    		LOGGER.debug("userSelection - " + userSelection);

			// publish message to Rewards topic
			JSONObject message = createReqForMorningOffer(userInfo, productId);

			if (PropertiesLoader.getIntValue(USSD_ENABLE_MULTI_CS) == ONE) {
				csType = lookUpDAO.getCSType(userInfo.getMsisdn());
				if (csType.equalsIgnoreCase(USSDConstants.CS_TYPE_OP)) {
					LOGGER.info("Published JSON message on Openet OP_rewards topic for Ml MorningOffer user :: " + message);
					publishToTopic(PropertiesLoader.getValue(OP_REWARDSPUB_TOPIC_NAME), message.toString());
				} else {
					LOGGER.info("Published JSON message on rewards topic for Ml MorningOffer user :: " + message);
					publishToTopic(PropertiesLoader.getValue(REWARDSPUB_TOPIC_NAME), message.toString());
				}
			} else {
				LOGGER.info("Published JSON message on rewards topic for Ml MorningOffer user :: " + message);
				publishToTopic(PropertiesLoader.getValue(REWARDSPUB_TOPIC_NAME), message.toString());
			}
    	} else {
    		inboundUSSDMsgTemplate.setIncomingLabel(INVALID_SELECTION);
    	}
    	return inboundUSSDMsgTemplate;
    }

    private void sendOptOutSMSNotification(UserInfo userInfoOptOut) throws Exception {
    	JSONObject jsonObjectOptOut = new JSONObject();
    	jsonObjectOptOut.put(TRANSACTION_ID, userInfoOptOut.getTxId());
    	jsonObjectOptOut.put(MSISDN, userInfoOptOut.getMsisdn());
    	jsonObjectOptOut.put(LANG_CODE, String.valueOf(userInfoOptOut.getLangCode()));
    	jsonObjectOptOut.put(MSG_CODE, PropertiesLoader.getValue(SMS_OPT_OUT_TEMPLATE));

    	LOGGER.info("sendOptOutSMSNotification => " + jsonObjectOptOut.get(TRANSACTION_ID));
    	publishToTopic(PropertiesLoader.getValue(SMS_TOPIC_NAME), jsonObjectOptOut.toString());
    }

    private void sendOptInSMSNotification(UserInfo userInfoOptIn) throws Exception {
    	JSONObject jsonObjectOptIn = new JSONObject();
    	jsonObjectOptIn.put(TRANSACTION_ID, userInfoOptIn.getTxId());
    	jsonObjectOptIn.put(MSISDN, userInfoOptIn.getMsisdn());
    	jsonObjectOptIn.put(LANG_CODE, String.valueOf(userInfoOptIn.getLangCode()));
    	jsonObjectOptIn.put(MSG_CODE, PropertiesLoader.getValue(SMS_OPT_IN_TEMPLATE));

    	jsonObjectOptIn.put(PRODUCT_VALUE, userInfoOptIn.getRagInfo().get(PRODUCT_VALUE));
    	jsonObjectOptIn.put(RECHARGE_TARGET, userInfoOptIn.getRagInfo().get(RECHARGE_TARGET));
    	jsonObjectOptIn.put(WEEK_END_DATE, userInfoOptIn.getRagInfo().get(WEEK_END_DATE));

    	jsonObjectOptIn.put(PRODUCT_VALIDITY, userInfoOptIn.getRagInfo().get(PRODUCT_VALIDITY));
    	jsonObjectOptIn.put(REWARD_CODE, userInfoOptIn.getRagInfo().get(REWARD_CODE));
    	jsonObjectOptIn.put(REWARD_INFO, userInfoOptIn.getRagInfo().get(REWARD_INFO));

    	jsonObjectOptIn.put(NEXT_AVAILABLE_OFFER_DATE, userInfoOptIn.getRagInfo().get(NEXT_AVAILABLE_OFFER_DATE));
    	jsonObjectOptIn.put(REMAINING_EFFORT, userInfoOptIn.getRagInfo().get(REMAINING_EFFORT));
    	jsonObjectOptIn.put(LAST_RECHARGE_TIME, userInfoOptIn.getRagInfo().get(LAST_RECHARGE_TIME));
    	LOGGER.info("End of sendOptInSMSNotification: "+ jsonObjectOptIn.get(TRANSACTION_ID));
    	publishToTopic(PropertiesLoader.getValue(SMS_TOPIC_NAME), jsonObjectOptIn.toString());
    }
    
    private void sendSAGOptInSMSNotification(UserInfo userInfoOptIn) throws Exception {
        JSONObject jsonObjectOptIn = new JSONObject();
        jsonObjectOptIn.put(TRANSACTION_ID, userInfoOptIn.getTxId());
        jsonObjectOptIn.put(MSISDN, userInfoOptIn.getMsisdn());
        jsonObjectOptIn.put(LANG_CODE, String.valueOf(userInfoOptIn.getLangCode()));
        jsonObjectOptIn.put(MSG_CODE, PropertiesLoader.getValue(USSD_SAG_SMS_OPT_IN_TEMPLATE));

        jsonObjectOptIn.put(PRODUCT_VALUE, userInfoOptIn.getSagInfo().get(PRODUCT_VALUE));
        jsonObjectOptIn.put(SPEND_TARGET, userInfoOptIn.getSagInfo().get(SPEND_TARGET));
        jsonObjectOptIn.put(WEEK_END_DATE, userInfoOptIn.getSagInfo().get(WEEK_END_DATE));
        jsonObjectOptIn.put(PRODUCT_VALIDITY, userInfoOptIn.getSagInfo().get(PRODUCT_VALIDITY));
        jsonObjectOptIn.put(REWARD_CODE, userInfoOptIn.getSagInfo().get(REWARD_CODE));
        jsonObjectOptIn.put(REWARD_INFO, userInfoOptIn.getSagInfo().get(REWARD_INFO));
        jsonObjectOptIn.put(NEXT_AVAILABLE_OFFER_DATE, userInfoOptIn.getSagInfo().get(NEXT_AVAILABLE_OFFER_DATE));
        jsonObjectOptIn.put(REMAINING_EFFORT, userInfoOptIn.getSagInfo().get(REMAINING_EFFORT));
        jsonObjectOptIn.put(LAST_SPEND_TIME, userInfoOptIn.getSagInfo().get(LAST_SPEND_TIME));
        
        LOGGER.info("sendSAGOptInSMSNotification || TransactionId: " + jsonObjectOptIn.get(TRANSACTION_ID));
        publishToTopic(PropertiesLoader.getValue(SMS_TOPIC_NAME), jsonObjectOptIn.toString());
    }
    
    private void sendSAGOptOutSMSNotification(UserInfo userInfoOptOut) throws Exception {
        JSONObject jsonObjectOptOut = new JSONObject();
        jsonObjectOptOut.put(TRANSACTION_ID, userInfoOptOut.getTxId());
        jsonObjectOptOut.put(MSISDN, userInfoOptOut.getMsisdn());
        jsonObjectOptOut.put(LANG_CODE, String.valueOf(userInfoOptOut.getLangCode()));
        jsonObjectOptOut.put(MSG_CODE, PropertiesLoader.getValue(USSD_SAG_SMS_OPT_OUT_TEMPLATE));

        LOGGER.info("sendSAGOptOutSMSNotification => " + jsonObjectOptOut.get(TRANSACTION_ID));
        publishToTopic(PropertiesLoader.getValue(SMS_TOPIC_NAME), jsonObjectOptOut.toString());
    }


    /**
     * Publishes a message to REWARDSCONSUMER/SMSCONSUMER topic
     *
     * @param message
     * @throws Exception
     */
    private void publishToTopic(String topicName, String message) {
        UssdEventPublisher eventPublisher = new UssdEventPublisher();
        eventPublisher.addEvent(topicName, message);
    }

    public void timeout(int sessionId, UssdSession ussdSession) {
        try {
            // create an entry in USSD Log table
            logEventIntoDB(ussdSession.getTransactionId(), sessionId, ussdSession.getMsisdn(), null, null, STATUS_USSD_TIMEOUT, getCurrentTimeStamp(), null, null, null, null, null, ussdSession.getCellId(), ussdSession.getPoolId(), null);
            // remove entry from ECMP_T_USSD_TRX_PRODID_MAP if it exist
        } catch (Exception ex) {
            LOGGER.error("Exception occured during timeout:: " + ex.getMessage(), ex);
        }
    }

    public void removeSession(int sessionId, UssdSession ussdSession) {
        try {
            // remove entry from ECMP_T_USSD_TRX_PRODID_MAP if it exist.
            // based on TRX_ID and Session_ID
            if (null != lookUpDAO.getTxnProdIdMap(ussdSession.getTransactionId(), sessionId)) {
                updaterDAO.deleteRecordForSession(ussdSession.getTransactionId(), sessionId);
            }
            LOGGER.info("Session removed for session ID:: " + sessionId);
        } catch (Exception ex) {
            LOGGER.error("Exception occured at:: " + ex.getMessage(), ex);
        }
    }

    public void logEventIntoDB(MessageDTO msgDTO) throws Exception {
        logEventIntoDB(msgDTO.getTxnId(), msgDTO.getUssdMsgId(), msgDTO.getMsisdn(), msgDTO.getProductIds(), msgDTO.getSelectedProdcutId(), msgDTO.getStatus(), getCurrentTimeStamp(), msgDTO.getUssdShortCode(), msgDTO.getMessageText(), null, null, null, msgDTO.getCellId(), msgDTO.getPoolId(), msgDTO.getTownName());
    }

    public void logEventIntoDB(String txnId, Integer ussdMsgId, String msisdn, String pids, String selectedPid, String status, Date dateTime, String ussdShortCd, String msgText, String custBal, String mlCustFlag, String randomFlag, String cellId, String poolId, String townName)
                    throws Exception {
        Object[] args = new Object[] { txnId, ussdMsgId, msisdn, pids, selectedPid, status, dateTime, ussdShortCd, Utils.convertToDBString(msgText), custBal, mlCustFlag, randomFlag, cellId, poolId, townName};
        updaterDAO.insertLog(args);
    }

    private JSONObject createMessageForRewardsPlugin(MessageDTO messageDTOForReward) throws JSONException {
        JSONObject messageForReward = new JSONObject();
        messageForReward.put(MSISDN, messageDTOForReward.getMsisdn());
        messageForReward.put(TRANSACTION_ID, messageDTOForReward.getTxnId());
        messageForReward.put(PRODUCT_ID, messageDTOForReward.getSelectedProdcutId());
        messageForReward.put(PROG_SHORT_CD, messageDTOForReward.getUssdShortCode());
        messageForReward.put(LANG_CODE, messageDTOForReward.getLangCode());
        return messageForReward;
    }

    private JSONObject createReqForRewardsOrMPesa(UserInfo userInfo, String productId, String messageBody, String prdPrice, boolean isMPesaReq, boolean provFlag, String productDescription, String dateTime)
                    throws Exception {
        LOGGER.debug("createReqForRewardsOrMPesa - " + prdPrice);
        JSONObject messageMpesa = new JSONObject();
        messageMpesa.put(MSISDN, userInfo.getMsisdn());
        messageMpesa.put(TRANSACTION_ID, userInfo.getTxId());
        messageMpesa.put(PRODUCT_ID, productId);
        messageMpesa.put(PROG_SHORT_CD, userInfo.getDestAddress());
        messageMpesa.put(LANG_CODE, userInfo.getLangCode());
        messageMpesa.put(ML_FLAG, FLAG_Y);
        messageMpesa.put(SEL_PROD_TYPE, userInfo.getSelProdType());
        messageMpesa.put(PRODUCT_PRICE, prdPrice);
        messageMpesa.put(DATE_TIME, dateTime);
        if (isMPesaReq) {
            messageMpesa.put(PRODUCT_DESC, productDescription);
            messageMpesa.put(CURRENCY_TYPE, currencyTypeMap.get(messageBody));
            messageMpesa.put(RANDOM_FLAG, userInfo.isRandomFlag() ? FLAG_Y : FLAG_N);
        }
        if (provFlag) {
            messageMpesa.put(AIRTIME_ADVANCE_BALANCE, userInfo.getAirtimeAdvBal());
            messageMpesa.put(ACCOUNT_BALANCE, userInfo.getActBal());
            messageMpesa.put(AA_PROV_AMOUNT, userInfo.getProvLoanAmt());
        }
        return messageMpesa;
    }
    
    private JSONObject createReqForMorningOffer(UserInfo userInfo, String productId)
    		throws Exception {
    	LOGGER.debug("Create JSON Req ForMorningOffer - " );
    	String dateTime = Utils.getDateAsString(new Date(), "yyyy-MM-dd hh:mm:ss");
    	JSONObject morningJson = new JSONObject();
    	morningJson.put(MSISDN, userInfo.getMsisdn());
    	morningJson.put(TRANSACTION_ID, userInfo.getTxId());
    	morningJson.put(PRODUCT_ID, productId);
    	morningJson.put(PROG_SHORT_CD, userInfo.getDestAddress());
    	morningJson.put(LANG_CODE, userInfo.getLangCode());
    	morningJson.put(ML_FLAG, userInfo.isMlFlag() ? FLAG_Y : FLAG_N);
    	morningJson.put(SEL_PROD_TYPE, userInfo.getSelProdType());    	
    	morningJson.put(DATE_TIME, dateTime);    	
    	morningJson.put(RANDOM_FLAG, userInfo.isRandomFlag() ? FLAG_Y : FLAG_N);
    	return morningJson;
    }
       

    /*
     * Log will be called during creating request for user.
     */
    public void logInfo(UssdMessage ussdMessage, String status) {
        try {
            // create Entry in Log Tables
            logEventIntoDB(ussdMessage.getTransactionId(), ussdMessage.getSessionId(), ussdMessage.getSourceAddress(), null, null, status, getCurrentTimeStamp(), ussdMessage.getDestinationAddress(), ussdMessage.getMessageText(), null, null, null, ussdMessage.getCellId(), ussdMessage.getPoolId(), ussdMessage.getTownName());
        } catch (Exception ex) {
            LOGGER.error("Exception occured :: " + ex.getMessage(), ex);
        }
    }

    public void logInfo(Integer sessionId, UserInfo userInfo, String messageBody, String status) throws Exception {
        Date date = new Date();
        LOGGER.info("LOAD MONITOR - MSISDN|SESSIONID|TXID|STATUS|TIME|CELLID|POOLID|RESPTIME - " + userInfo.getMsisdn() + "|" + userInfo.getUserMsgRef() + "|" + userInfo.getTxId() + "|" + status + "|" + date.getTime() + "|" + userInfo.getCellId() + "|" + userInfo.getPoolId() + "|0");
        logEventIntoDB(userInfo.getTxId(), sessionId, userInfo.getMsisdn(), null, null, status, getCurrentTimeStamp(), userInfo.getDestAddress(), messageBody, userInfo.getCustBalance(), userInfo.isMlFlag() ? FLAG_Y : FLAG_N, userInfo.isRandomFlag() ? FLAG_Y : FLAG_N, userInfo.getCellId(), userInfo.getPoolId(), userInfo.getTownName());
    }

    /*
     * LogInfo will be called during responding back to user.(SubmitSM)
     */
    public void logInfo(int sessionId, TransmitMessage transmitMessage) {
        try {
            // create Entry in Log Tables
            String status;
            String mlFlag;
            String randomFlag;
            if (transmitMessage.getServiceOp() == SEVENTEEN) {
            	status = (transmitMessage.getUssdLogStatus() == ONE) ? STATUS_FINAL_RESPONSE_SENT : transmitMessage.getLogStatus();              
            } else {
                status = (transmitMessage.getUssdLogStatus() == ONE) ? STATUS_RESPONSE_SENT : transmitMessage.getLogStatus();
            }

            Date date = new Date();
            LOGGER.info("LOAD MONITOR - MSISDN|SESSIONID|TXID|STATUS|TIME|CELLID|POOLID|RESPTIME - " + transmitMessage.getDestinationAddress() + "|" +
                            sessionId + "|" + transmitMessage.getTransactionId() + "|" + status + "|" + date.getTime() + "|" +
                            transmitMessage.getCellId() + "|" + transmitMessage.getPoolId() + "|" + transmitMessage.getlTimeTaken());

            LOGGER.debug("Updating status for the msisdn = " + transmitMessage.getDestinationAddress() + " :: " + status);

            mlFlag = transmitMessage.getMlFlag() ? FLAG_Y : FLAG_N;
            randomFlag = transmitMessage.getRandomFlag() ? FLAG_Y : FLAG_N;

            logEventIntoDB(transmitMessage.getTransactionId(), sessionId, transmitMessage.getDestinationAddress(), transmitMessage.getProdIds(), transmitMessage.getSelProdId(), status, getCurrentTimeStamp(), transmitMessage.getSourceAddress(), transmitMessage.getMessageText(), null, mlFlag, randomFlag, transmitMessage.getCellId(), transmitMessage.getPoolId(), transmitMessage.getTownName());
        } catch (Exception ex) {
            LOGGER.error("Exception occured :: " + ex.getMessage(), ex);
        }
    }

    public void removeAllSessionData() {
        try {
            updaterDAO.deleteAllSessionData();
        } catch (Exception e) {
            LOGGER.error("Exception occured:: " + e.getMessage(), e);
        }
    }

    public InboundUssdMessage getMLMenuTemplate(String templateId, int langCode) throws Exception {
    	InboundUssdMessage inboundUSSDMsg = null;

    	TemplateCache templateCache = TemplateCache.instance();
    	TemplateDTO templateDTO = templateCache.getMLMenu(templateId + "_" + langCode);
    	if (null != templateDTO) {
    		inboundUSSDMsg = new InboundUssdMessage();
    		inboundUSSDMsg.setClobString(templateDTO.getTemplate());
    		LOGGER.debug("Template details found for the TemplateId:: " + templateId);
    	} else {
    		LOGGER.error("Template details could not be found for id:: " + templateId + "_" + langCode);
    	}
    	return inboundUSSDMsg;
    }

    public InboundUssdMessage getPedAvailablePlays(UserInfo userInfo) throws Exception {

        int count = pedPrcosesService.getAvailablePlays(userInfo.getMsisdn());
        InboundUssdMessage inboundUssdMessage;
        String pedAvailableMenuTemplate;
        if (count > ZERO) {
            pedAvailableMenuTemplate = PropertiesLoader.getValue(USSD_ML_PED_AVAILABLE_PLAYS_MENU);
            LOGGER.debug("pedAvailableMenuTemplate=>" + pedAvailableMenuTemplate + "|| count=> " + count);

            inboundUssdMessage = getMLMenuTemplate(pedAvailableMenuTemplate, userInfo.getLangCode());
            String replacePattern = "@playcount@";
            String replacedCountTemaplate = inboundUssdMessage.getClobString().replace(replacePattern, "" + count);

            Calendar calendar = Calendar.getInstance();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date weekStartingDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
            String weeksStartDate = dateFormat.format(weekStartingDate);
            int DaysCount = pedPrcosesService.getPlayExpiryDays();
            if (DaysCount > ZERO) {
                calendar.add(Calendar.DATE, DaysCount);
                weeksStartDate = dateFormat.format(calendar.getTime());
            }

            replacedCountTemaplate = replacedCountTemaplate.replace("@date@", weeksStartDate);
            inboundUssdMessage.setClobString(replacedCountTemaplate);
            inboundUssdMessage.setSelProdId(pedAvailableMenuTemplate);
        } else {
            pedAvailableMenuTemplate = PropertiesLoader.getValue(USSD_ML_PED_NO_PLAY_MENU);
            LOGGER.debug("No Play templated Id :: " + pedAvailableMenuTemplate);
            inboundUssdMessage = getMLMenuTemplate(pedAvailableMenuTemplate, userInfo.getLangCode());
            inboundUssdMessage.setSelProdId(pedAvailableMenuTemplate);
        }
        return inboundUssdMessage;
    }

    public InboundUssdMessage getPedPrizeHistory(UserInfo userInfo) throws Exception {
        
        String pedHistoryMenuTemplate;
        final String[] replacedCountTemaplate = { null };
        AtomicInteger index = new AtomicInteger();
        index.addAndGet(ONE);

        List<String> prizeHistoryList = pedPrcosesService.getPrizeHistory(userInfo.getMsisdn());

        InboundUssdMessage inboundUssdMessage;

        if (null != prizeHistoryList && prizeHistoryList.size() > ZERO) {

            pedHistoryMenuTemplate = PropertiesLoader.getValue(USSD_ML_PED_HISTORY_MENU);
            LOGGER.debug("history templated Id :: " + pedHistoryMenuTemplate);
            inboundUssdMessage = getMLMenuTemplate(pedHistoryMenuTemplate, userInfo.getLangCode());
            StringBuilder tmepl = new StringBuilder(inboundUssdMessage.getClobString());
            prizeHistoryList.forEach(prize -> replacedCountTemaplate[ZERO] = tmepl.append("\n" + index.getAndIncrement() + ". " + prize).toString());
            inboundUssdMessage.setClobString(replacedCountTemaplate[ZERO]);
            inboundUssdMessage.setSelProdId(pedHistoryMenuTemplate);
        } else {
            pedHistoryMenuTemplate = PropertiesLoader.getValue(USSD_ML_PED_NO_HISTORY_MENU);
            LOGGER.debug("history templated Id :: " + pedHistoryMenuTemplate);
            inboundUssdMessage = getMLMenuTemplate(pedHistoryMenuTemplate, userInfo.getLangCode());
            inboundUssdMessage.setSelProdId(pedHistoryMenuTemplate);
        }

        return inboundUssdMessage;
    }

    public InboundUssdMessage getPrizeForPlay(UserInfo userInfo) throws Exception {
        LOGGER.debug("getPrizeForPlay=>");
        String prizeDetail = pedPrcosesService.processPlay(userInfo.getMsisdn(), userInfo.getLangCode(), userInfo.getTxId());
        String pedPlayTemaplate = "";
        InboundUssdMessage inboundUssdMessage = null;
        if ("NO_PRIZE".equalsIgnoreCase(prizeDetail)) {
            pedPlayTemaplate = PropertiesLoader.getValue(USSD_ML_PED_NO_PRIZE_MENU);
            LOGGER.debug("templated Id is :: " + pedPlayTemaplate);
            inboundUssdMessage = getMLMenuTemplate(pedPlayTemaplate, userInfo.getLangCode());
        } else if ("NO_PLAY".equalsIgnoreCase(prizeDetail)) {
            pedPlayTemaplate = PropertiesLoader.getValue(USSD_ML_PED_NO_PLAY_MENU);
            LOGGER.debug("templated Id :: " + pedPlayTemaplate);
            inboundUssdMessage = getMLMenuTemplate(pedPlayTemaplate, userInfo.getLangCode());
        } else {
            pedPlayTemaplate = PropertiesLoader.getValue(USSD_ML_PED_OFFER_MENU);
            LOGGER.debug("templated Id :: " + pedPlayTemaplate);
            inboundUssdMessage = getMLMenuTemplate(pedPlayTemaplate, userInfo.getLangCode());
            String replacePattern = "@prize@";
            String replacedCountTemaplate = inboundUssdMessage.getClobString().replace(replacePattern, prizeDetail);
            inboundUssdMessage.setClobString(replacedCountTemaplate);

        }
        inboundUssdMessage.setSelProdId(pedPlayTemaplate);
        return inboundUssdMessage;
    }
    
	public TransmitMessage getMainMenuForMLUser(JSONObject queryBaljson, UserInfo userInfo) throws Exception {
		InboundUssdMessage inboundUssdMessage = null;
		TransmitMessage message = null;
		if (ONE == isSocialEnabled) {
			// sending j4u main menu 0.Morning/nothing, 1.voice, 2.data
			// 3.integrated 4. hourly data, 5.Social 6.MyRewards
			inboundUssdMessage = initialSocialRequestPart1(userInfo);
		} else {
			// sending j4u main menu 0.Morning/nothing, 1.voice, 2.data
			// 3.integrated 4. hourly data, 5.MyRewards
			inboundUssdMessage = initialMLRequestPart1(userInfo);
		}
		if (null != inboundUssdMessage) {
			modifyTemplate(queryBaljson, userInfo.getLangCode(), inboundUssdMessage, true);
			message = new TransmitMessage();
			generateUssdResponse(message, userInfo, inboundUssdMessage, userInfo.getUserMsgRef());
			message.setLstarttime(userInfo.getlStartTime());
		}
		return message;
	}

	private InboundUssdMessage initialSocialRequestPart1(UserInfo userInfo) throws Exception {
		String ussdtemplateId;
    	if (userInfo.isMorningOfferFlag()) {
    		ussdtemplateId = userInfo.isConsentFlag() ? USSD_ML_MAIN_MENU_MS_CONSENT_PART_1 : USSD_ML_MAIN_MENU_MS_PART_1;
    	} else {
    		ussdtemplateId = userInfo.isConsentFlag() ? USSD_ML_MAIN_MENU_SO_CONSENT_PART_1 : USSD_ML_MAIN_MENU_SO;
    	}
    	return getMLMenuTemplate(PropertiesLoader.getValue(ussdtemplateId), userInfo.getLangCode());
	}

	private InboundUssdMessage initialMLRequestPart1(UserInfo userInfo) throws Exception {
		String ussdtemplateId;
    	if (userInfo.isMorningOfferFlag()) {
    		ussdtemplateId = userInfo.isConsentFlag() ? USSD_ML_MAIN_MENU_MO_CONSENT_PART_1 : USSD_ML_MAIN_MENU_MO;
    	} else {
    		ussdtemplateId = userInfo.isConsentFlag() ? USSD_ML_MAIN_MENU_NO_MS_CONSENT : USSD_ML_MAIN_MENU_NO_MS;
    	}
    	return getMLMenuTemplate(PropertiesLoader.getValue(ussdtemplateId), userInfo.getLangCode());
	}

	public TransmitMessage subMenuMLMorningRequest(JSONObject queryBaljson, Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {
		TransmitMessage message = null;
		InboundUssdMessage inboundUssdMessage = null;
		inboundUssdMessage = getSubMenuForMorningUser(userInfo);

		if (null != inboundUssdMessage) {
			if (inboundUssdMessage.getIncomingLabel() == PROD_IDS_NOT_FOUND) {
				QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
				queryBaljsonMap.remove(sessionId);
				return noOffersAvailableMsgRequest(sessionId, messageBody, userInfo, PropertiesLoader.getValue(USSD_ML_NO_OFFERS_AVAILABLE_FAILURE_MSG_TEMPLATE));
			} else {
				modifyTemplate(queryBaljson, userInfo.getLangCode(), inboundUssdMessage, false);
				message = new TransmitMessage();
				generateUssdResponse(message, userInfo, inboundUssdMessage, userInfo.getUserMsgRef());
				// setting 2 for ussd log table status
				message.setUssdLogStatus(TWO);
				message.setLogStatus(SUBMENU_RESPONSE_SENT);
				message.setProdIds(inboundUssdMessage.getProdIds());
				LOGGER.debug("inboundUssdMessage.getProdIds() - " + inboundUssdMessage.getProdIds());
			}
		}

		return message;
	}

	public TransmitMessage noOffersAvailableMsgRequest(Integer sessionId, String messageBody,
			UserInfo userInfo,  String templateId) throws Exception {

		logInfo(sessionId, userInfo, messageBody, NO_OFFERS_AVAILABLE_FAILURE_MSG_REQUEST_RECIEVED);
		TransmitMessage message = null;
		InboundUssdMessage inboundUssdMessage = null;
		inboundUssdMessage = getNoOffersAvailableFailureMsg(userInfo.getMsisdn(), templateId, userInfo.getLangCode());

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
			message.setCellId(userInfo.getCellId());
			message.setPoolId(userInfo.getPoolId());
			message.setUssdLogStatus(TWO);
			message.setLogStatus(STATUS_FINAL_RESPONSE_SENT);
		}
		return message;
	}

	public TransmitMessage processMenuForJ4UnewCustomer(JSONObject queryBaljson, UserInfo newUserInfo)
			throws Exception {
		TransmitMessage message = null;
		InboundUssdMessage inboundUssdMessage = null;
		String msisdn = newUserInfo.getMsisdn();
		LOGGER.info("processing Menu for NewUser, msisdn :: " + msisdn);
		String ussdtemplateId = newUserInfo.isMorningOfferFlag() ? USSD_ML_MAIN_MENU_MO : USSD_ML_MAIN_MENU_NO_MS;
		inboundUssdMessage = getMLMenuTemplate(PropertiesLoader.getValue(ussdtemplateId), newUserInfo.getLangCode());

		if (null != inboundUssdMessage) {
			modifyTemplate(queryBaljson, newUserInfo.getLangCode(), inboundUssdMessage, true);
			message = new TransmitMessage();
			generateUssdResponse(message, newUserInfo, inboundUssdMessage, newUserInfo.getUserMsgRef());
			// setting 2 for ussd log table status
			message.setUssdLogStatus(TWO);
			message.setLogStatus(NEWCUSTOMER_MENU_SENT);

		}
		return message;

	}
	
    public void modifyTemplate(JSONObject queryBaljson, int langCode, InboundUssdMessage inboundUssdMessage,
			boolean isMainMenu) throws Exception {
		String balanceDetail = "";
		String replacedTemplate = null;
		if (queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(OCS_QUERY_BAL_SUCCESS)) { 
			double actualBalance = (double) queryBaljson.getLong(ACCOUNT_BALANCE)
					/ PropertiesLoader.getIntValue(MICRO_DOLLAR);
			String balanceTemplate = PropertiesLoader.getValue(BALANCE_TEMPLATE + "_" + langCode);
			double dalance = Math.floor(actualBalance * multiplier) / multiplier;
			balanceDetail = balanceTemplate.replace(BALANCE_PATTERN, Double.toString(dalance));
		}
		if (isMainMenu) {
			replacedTemplate = inboundUssdMessage.getClobString().replace(BALANCE_PATTERN, balanceDetail);
		} else {
			if (queryBaljson.getString(OCS_QUERY_BAL_STATUS).equals(OCS_QUERY_BAL_SUCCESS)) {
				replacedTemplate = balanceDetail + "\n" + inboundUssdMessage.getClobString();
			} else {
				replacedTemplate = inboundUssdMessage.getClobString();
			}
			QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
			queryBaljsonMap.remove(queryBaljson.getInt(USER_MSG_REF));
		}
		inboundUssdMessage.setClobString(replacedTemplate);
	}
    
    public void generateUssdResponse(TransmitMessage message, UserInfo userInfo, InboundUssdMessage inboundUssdMessage, Integer sessionId){
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
    }
    
    
    private void insertInUssdTransProdIdMap(UserInfo userInfo, List<String> productIdsList, List<String> productPricesList) throws Exception {
    	// creating entry in ECMP_T_USSD_TRX_PRODID_MAP table
    	LOGGER.debug(CREATING_ENTRY_IN_ECMP_T_USSD_TRX_PRODID_MAP_TABLE);
    	if (productIdsList.size() == FIVE) {
    		updaterDAO.insertTrxProdIdMap5(userInfo, productIdsList.get(ZERO) + "~" + productPricesList.get(ZERO), 
    				productIdsList.get(ONE) + "~" + productPricesList.get(ONE), 
    				productIdsList.get(TWO) + "~" + productPricesList.get(TWO),
    				productIdsList.get(THREE) + "~" + productPricesList.get(THREE),
    				productIdsList.get(FOUR) + "~" + productPricesList.get(FOUR)); 
    		userInfo.setOfferCount(FIVE);
    	} else if (productIdsList.size() == FOUR) {
    		updaterDAO.insertTrxProdIdMap5(userInfo, productIdsList.get(ZERO) + "~" + productPricesList.get(ZERO), 
    				productIdsList.get(ONE) + "~" + productPricesList.get(ONE), 
    				productIdsList.get(TWO) + "~" + productPricesList.get(TWO),
    				productIdsList.get(THREE) + "~" + productPricesList.get(THREE),null);
    		userInfo.setOfferCount(FOUR);
    	} else if (productIdsList.size() == MAX_PRODIDS_CNT_3) {
    		updaterDAO.insertTrxProdIdMap5(userInfo, productIdsList.get(ZERO) + "~" + productPricesList.get(ZERO), 
    				productIdsList.get(ONE) + "~" + productPricesList.get(ONE), 
    				productIdsList.get(TWO) + "~" + productPricesList.get(TWO),null,null );
    		userInfo.setOfferCount(MAX_PRODIDS_CNT_3);
    	} else if (productIdsList.size() == MAX_PRODIDS_CNT_2) {
    		updaterDAO.insertTrxProdIdMap5(userInfo, productIdsList.get(ZERO) + "~" + productPricesList.get(ZERO), 
    				productIdsList.get(ONE) + "~" + productPricesList.get(ONE),null,null,null);
    		userInfo.setOfferCount(MAX_PRODIDS_CNT_2);
    	} else if (productIdsList.size() == MAX_PRODIDS_CNT_1) {
    		updaterDAO.insertTrxProdIdMap5(userInfo, productIdsList.get(ZERO) + "~" + productPricesList.get(ZERO), 
    				null,null,null,null);
    		userInfo.setOfferCount(MAX_PRODIDS_CNT_2);
    	}
    	LOGGER.debug("Insertion in UssdTransProductIdMap done!");
    }
    
    public TransmitMessage subMenuMLTownRequest(JSONObject queryBaljson, Integer sessionId, String messageBody, UserInfo userInfo) throws Exception {
    	TransmitMessage message = null;
    	
    	InboundUssdMessage inboundUssdMessage = null;
    	inboundUssdMessage = getSubMenuForTownUser(userInfo);

    	if (null != inboundUssdMessage) {
    		if (inboundUssdMessage.getIncomingLabel() == PROD_IDS_NOT_FOUND) {
    			QueryBaljsonMap queryBaljsonMap = QueryBaljsonMap.instance();
    			queryBaljsonMap.remove(sessionId);
    			return noOffersAvailableMsgRequest(sessionId, messageBody, userInfo, PropertiesLoader.getValue(USSD_ML_TOWN_FAILURE_MSG_TEMPLATE));
    		} else {
    			modifyTemplate(queryBaljson, userInfo.getLangCode(), inboundUssdMessage, false);
    			message = new TransmitMessage();
    			generateUssdResponse(message, userInfo, inboundUssdMessage, sessionId);
    			message.setTownName(inboundUssdMessage.getTownName());
    			message.setUssdLogStatus(TWO);
    			message.setLogStatus(SUBMENU_RESPONSE_SENT);
    			// setting 2 for ussd log table status
    			message.setProdIds(inboundUssdMessage.getProdIds());
    			LOGGER.debug("inboundUssdMessage.getProdIds() - " + inboundUssdMessage.getProdIds());
    		}
    	}
    	return message;
    }
}
