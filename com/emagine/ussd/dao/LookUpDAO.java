package com.emagine.ussd.dao;

import static com.emagine.ussd.utils.USSDConstants.AA_ELIGIBLE;
import static com.emagine.ussd.utils.USSDConstants.AD_HOC;
import static com.emagine.ussd.utils.USSDConstants.A_VALUE;
import static com.emagine.ussd.utils.USSDConstants.BLACKLIST_MSISDN;
import static com.emagine.ussd.utils.USSDConstants.B_VALUE;
import static com.emagine.ussd.utils.USSDConstants.CELL_ID_KEY_NAME;
import static com.emagine.ussd.utils.USSDConstants.CONSENT_FLAG;
import static com.emagine.ussd.utils.USSDConstants.CROSS_SELL;
import static com.emagine.ussd.utils.USSDConstants.C_VALUE;
import static com.emagine.ussd.utils.USSDConstants.DATE_TIME;
import static com.emagine.ussd.utils.USSDConstants.DEFAULT_ERRMSG_PROC_NAME;
import static com.emagine.ussd.utils.USSDConstants.DEFAULT_MESSAGE;
import static com.emagine.ussd.utils.USSDConstants.DEFAULT_MSG_PROC_NAME;
import static com.emagine.ussd.utils.USSDConstants.ECMP_P_J4U_ML_LOCATION_OFFER;
import static com.emagine.ussd.utils.USSDConstants.ERROR_MESSAGE;
import static com.emagine.ussd.utils.USSDConstants.FLAG_N;
import static com.emagine.ussd.utils.USSDConstants.FLAG_Y;
import static com.emagine.ussd.utils.USSDConstants.J4U_ELIGIBILITY;
import static com.emagine.ussd.utils.USSDConstants.LANG_CODE;
import static com.emagine.ussd.utils.USSDConstants.LAST_RECHARGE_TIME;
import static com.emagine.ussd.utils.USSDConstants.LAST_SPEND_TIME;
import static com.emagine.ussd.utils.USSDConstants.LOCATION_RANDOM_FLAG;
import static com.emagine.ussd.utils.USSDConstants.MAX_PRODIDS_CNT_3;
import static com.emagine.ussd.utils.USSDConstants.ML_CUSTOMER_FLAG;
import static com.emagine.ussd.utils.USSDConstants.MORNING_OFFER_ELIGIBILITY;
import static com.emagine.ussd.utils.USSDConstants.MPESA_USER_FLAG;
import static com.emagine.ussd.utils.USSDConstants.MSISDN;
import static com.emagine.ussd.utils.USSDConstants.NBA_DATE_TIME;
import static com.emagine.ussd.utils.USSDConstants.NEXT_AVAILABLE_OFFER_DATE;
import static com.emagine.ussd.utils.USSDConstants.OFFER_ORDER_CSV;
import static com.emagine.ussd.utils.USSDConstants.OFFER_REFRESH_FLAG;
import static com.emagine.ussd.utils.USSDConstants.ONE_1;
import static com.emagine.ussd.utils.USSDConstants.PED_ELIGIBILITY;
import static com.emagine.ussd.utils.USSDConstants.PID_1;
import static com.emagine.ussd.utils.USSDConstants.PID_2;
import static com.emagine.ussd.utils.USSDConstants.PID_3;
import static com.emagine.ussd.utils.USSDConstants.PID_4;
import static com.emagine.ussd.utils.USSDConstants.PID_5;
import static com.emagine.ussd.utils.USSDConstants.POOL_ID;
import static com.emagine.ussd.utils.USSDConstants.PREF_PAY_METHOD;
import static com.emagine.ussd.utils.USSDConstants.PREF_PAY_METHOD_G;
import static com.emagine.ussd.utils.USSDConstants.PRODUCT_DESC;
import static com.emagine.ussd.utils.USSDConstants.PRODUCT_ID;
import static com.emagine.ussd.utils.USSDConstants.PRODUCT_IDS;
import static com.emagine.ussd.utils.USSDConstants.PRODUCT_PRICE;
import static com.emagine.ussd.utils.USSDConstants.PRODUCT_SUBTYPE;
import static com.emagine.ussd.utils.USSDConstants.PRODUCT_TYPE;
import static com.emagine.ussd.utils.USSDConstants.PRODUCT_VALIDITY;
import static com.emagine.ussd.utils.USSDConstants.PRODUCT_VALUE;
import static com.emagine.ussd.utils.USSDConstants.P_GET_OPENET_BLACKLIST_MSISDN;
import static com.emagine.ussd.utils.USSDConstants.RAG_GOAL_REACHED_FLAG;
import static com.emagine.ussd.utils.USSDConstants.RAG_MSISDN;
import static com.emagine.ussd.utils.USSDConstants.RAG_OPT_FLAG;
import static com.emagine.ussd.utils.USSDConstants.RANDOM_FLAG;
import static com.emagine.ussd.utils.USSDConstants.RECHARGE_TARGET;
import static com.emagine.ussd.utils.USSDConstants.REMAINING_EFFORT;
import static com.emagine.ussd.utils.USSDConstants.REWARD_CODE;
import static com.emagine.ussd.utils.USSDConstants.REWARD_INFO;
import static com.emagine.ussd.utils.USSDConstants.ROUTER_CS_MSISDNRANGE_QUERY;
import static com.emagine.ussd.utils.USSDConstants.ROUTER_CS_MSISDN_QUERY;
import static com.emagine.ussd.utils.USSDConstants.SAG_GOAL_REACHED_FLAG;
import static com.emagine.ussd.utils.USSDConstants.SAG_MSISDN;
import static com.emagine.ussd.utils.USSDConstants.SAG_OPT_FLAG;
import static com.emagine.ussd.utils.USSDConstants.SPEND_TARGET;
import static com.emagine.ussd.utils.USSDConstants.SUBID_CNTRYCD;
import static com.emagine.ussd.utils.USSDConstants.SUBID_LENGTH;
import static com.emagine.ussd.utils.USSDConstants.TEMPLATE;
import static com.emagine.ussd.utils.USSDConstants.TEMPLATE_ID;
import static com.emagine.ussd.utils.USSDConstants.TOWN_NAME;
import static com.emagine.ussd.utils.USSDConstants.TRX_ID;
import static com.emagine.ussd.utils.USSDConstants.TRX_PRODIDMAP_PROC_NAME;
import static com.emagine.ussd.utils.USSDConstants.UPSELL_1;
import static com.emagine.ussd.utils.USSDConstants.UPSELL_2;
import static com.emagine.ussd.utils.USSDConstants.USSD_BLACKLIST_ENABLED;
import static com.emagine.ussd.utils.USSDConstants.USSD_GET_NEW_CUSTOMER_PROFILE_PROC;
import static com.emagine.ussd.utils.USSDConstants.USSD_GET_USER_INFO_PROC;
import static com.emagine.ussd.utils.USSDConstants.USSD_GET_USER_INFO_WITH_BLIST_PROC;
import static com.emagine.ussd.utils.USSDConstants.USSD_LOCATION_PRODINFO_PROC_NAME;
import static com.emagine.ussd.utils.USSDConstants.USSD_ML_GET_MORNING_OFFER_PROC;
import static com.emagine.ussd.utils.USSDConstants.USSD_ML_SOCIAL_OFFER_BYEXP_PROC;
import static com.emagine.ussd.utils.USSDConstants.USSD_ML_SOCIAL_OFFER_BYRANK_PROC;
import static com.emagine.ussd.utils.USSDConstants.USSD_ML_SOCIAL_QUERY_OFFER_PROC;
import static com.emagine.ussd.utils.USSDConstants.USSD_NON_PROMO_PRODUCT_INFO;
import static com.emagine.ussd.utils.USSDConstants.USSD_OFFERLOOKUP_PROC_NAME;
import static com.emagine.ussd.utils.USSDConstants.USSD_PRODINFO_PRODTYPE_NAME;
import static com.emagine.ussd.utils.USSDConstants.USSD_P_GET_CONSENT_STATUS;
import static com.emagine.ussd.utils.USSDConstants.USSD_P_ML_AA_OFFER;
import static com.emagine.ussd.utils.USSDConstants.USSD_P_ML_SAG_INFO_SELECT;
import static com.emagine.ussd.utils.USSDConstants.USSD_P_ML_SAG_USER_RECORD_SELECT;
import static com.emagine.ussd.utils.USSDConstants.USSD_TEMPLATEFORID_PROC_NAME;
import static com.emagine.ussd.utils.USSDConstants.USSD_TXNPRODMAP_PROC_NAME;
import static com.emagine.ussd.utils.USSDConstants.WEEK_END_DATE;
import static com.emagine.ussd.utils.USSDConstants.WEEK_START_DATE;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.voltdb.VoltTable;
import org.voltdb.VoltTableRow;
import org.voltdb.VoltType;
import org.voltdb.client.Client;
import org.voltdb.client.ProcCallException;
import com.comviva.voltdb.factory.DAOFactory;
import com.emagine.ussd.config.PropertiesLoader;
import com.emagine.ussd.connection.UssdMessage;
import com.emagine.ussd.model.DefaultMessageDTO;
import com.emagine.ussd.model.MLOfferMsg;
import com.emagine.ussd.model.MessageDTO;
import com.emagine.ussd.model.OfferParams;
import com.emagine.ussd.model.ProductInfo;
import com.emagine.ussd.model.RankingFormulae;
import com.emagine.ussd.model.TemplateDTO;
import com.emagine.ussd.model.TownProdInfo;
import com.emagine.ussd.model.UserInfo;
import com.emagine.ussd.utils.ProductInfoCache;
import com.emagine.ussd.utils.USSDConstants;
import com.emagine.ussd.utils.Utils;

public class LookUpDAO {

	private static final Logger LOG = Logger.getLogger(LookUpDAO.class);
	private static final String PROC_NAME = "Procedure Name :: ";
	private static final String FIELD_PRODUCT_PRICE = "PRODUCT_PRICE";
	private static final String FIELD_EXPECTED_VALUE = "EXPECTED_VALUE";
	private static final String FIELD_PRODUCT_ID = "PRODUCT_ID";
	public static final String USSD_P_ML_RAG_USER_RECORD_SELECT = "USSD_P_ML_RAG_USER_RECORD_SELECT";

	private Client voltDbClient;

	private static final String CS_TYPE = "CS_TYPE";
	public static final String MSISDN_PREFIX_ZERO = "0";
	private static String countrycode = "243";
	private static int rangelen = 8;
	private static boolean isBlacklistEnabled;

	static {
		try {
			countrycode = PropertiesLoader.getValue(USSDConstants.ROUTER_COUNTRY_CODE);
			rangelen = PropertiesLoader.getIntValue(USSDConstants.ROUTER_MSISDN_RANGE_LENGTH);
			isBlacklistEnabled = PropertiesLoader.getValue(USSD_BLACKLIST_ENABLED).equalsIgnoreCase(ONE_1);

		} catch (Exception ex) {
			LOG.error("RouterDao Init Exception ", ex);
		}
	}

	public LookUpDAO() throws Exception {
		voltDbClient = DAOFactory.getClient();
	}

	public boolean lookupOffers(final MessageDTO msgDTO) throws Exception {
		String sql = PropertiesLoader.getValue(USSD_OFFERLOOKUP_PROC_NAME);
		VoltTable[] voltTable = voltDbClient.callProcedure(sql, msgDTO.getMsisdn()).getResults();
		if (voltTable[0].advanceRow()) {
			String prodString = voltTable[0].getString(PRODUCT_IDS);
			if (Utils.isNullOrEmpty(prodString)) {
				msgDTO.setProductIds(null);
			} else {
				JSONObject jsonProds = new JSONObject(prodString);
				msgDTO.addProduct(UPSELL_1, jsonProds.getString(UPSELL_1));
				msgDTO.addProduct(UPSELL_2, jsonProds.getString(UPSELL_2));
				msgDTO.addProduct(CROSS_SELL, jsonProds.getString(CROSS_SELL));
				msgDTO.setProductIds(prodString);
			}
			msgDTO.setLangCode((int) voltTable[0].get(LANG_CODE, VoltType.INTEGER));
			msgDTO.setTemplateId(voltTable[0].getString(TEMPLATE_ID));
			msgDTO.setNbaDateTime(voltTable[0].getTimestampAsSqlTimestamp(NBA_DATE_TIME));
			return true;
		}
		return false;
	}

	public void lookupTemplateForTemplateId(final String templateId, MessageDTO msgDTO) throws Exception {
		String sql = PropertiesLoader.getValue(USSD_TEMPLATEFORID_PROC_NAME);
		String template = null;
		String offerOrderCSV;
		VoltTable[] voltTable = voltDbClient.callProcedure(sql, templateId).getResults();
		if (voltTable[0].advanceRow()) {
			template = voltTable[0].getString(TEMPLATE);
			msgDTO.setTemplate(template);
			offerOrderCSV = voltTable[0].getString(OFFER_ORDER_CSV);
			msgDTO.setOfferSequence(null != offerOrderCSV ? offerOrderCSV.split(",") : null);
		}
	}

	public Map<String, TemplateDTO> getTemplatesMap() throws IOException, ProcCallException {
		Map<String, TemplateDTO> templatesMap = new HashMap<>();
		String sql = "SELECT * FROM ECMP_T_COMMS_TEMPLATE";
		VoltTable[] templatesTbl = voltDbClient.callProcedure(AD_HOC, sql).getResults();
		while (templatesTbl[0].advanceRow()) {
			String id = templatesTbl[0].getString(TEMPLATE_ID);
			int langCd = (int) templatesTbl[0].get(LANG_CODE, VoltType.INTEGER);
			String template = templatesTbl[0].getString(TEMPLATE);
			String offerOrderCSV = templatesTbl[0].getString(OFFER_ORDER_CSV);
			TemplateDTO templateDTO = new TemplateDTO();
			templateDTO.setLangCd(langCd);
			templateDTO.setOfferOrderCSV(offerOrderCSV);
			templateDTO.setTempalteId(id);
			templateDTO.setTemplate(template);
			templatesMap.put(id, templateDTO);
		}
		return templatesMap;
	}

	public Map<String, TemplateDTO> getMLTemplatesMap() throws IOException, ProcCallException  {
		Map<String, TemplateDTO> templatesMap = new HashMap<>();
		String sql = "select * from ECMP_T_COMMS_TEMPLATE where TEMPLATE_ID like 'ML%' ";
		VoltTable[] templatesTbl = voltDbClient.callProcedure(AD_HOC, sql).getResults();
		while (templatesTbl[0].advanceRow()) {
			String templateId = templatesTbl[0].getString(TEMPLATE_ID);
			int langCd = (int) templatesTbl[0].get(LANG_CODE, VoltType.INTEGER);
			TemplateDTO templateDTO = new TemplateDTO();
			templateDTO.setTempalteId(templateId);
			templateDTO.setLangCd(langCd);
			templateDTO.setOfferOrderCSV(templatesTbl[0].getString(OFFER_ORDER_CSV));
			templateDTO.setTemplate(templatesTbl[0].getString(TEMPLATE));
			templatesMap.put(templateId + "_" + langCd, templateDTO);
		}

		LOG.debug("ML Templates cache loaded :: templatesMap size = " + templatesMap.size());

		return templatesMap;
	}

	public Map<String, List<String>> getProdTypeMap() throws Exception {
		Map<String, List<String>> prodTypeMap = new HashMap<>();
		List<String> prodIdsList = null;
		String sql = PropertiesLoader.getValue(USSD_PRODINFO_PRODTYPE_NAME);
		VoltTable[] templatesTbl = voltDbClient.callProcedure(sql).getResults();
		while (templatesTbl[0].advanceRow()) {
			String productType = templatesTbl[0].getString("PRODUCT_TYPE");
			String productId = templatesTbl[0].getString(FIELD_PRODUCT_ID);

			if (prodTypeMap.containsKey(productType)) {
				prodTypeMap.get(productType).add(productId);
			} else {
				prodIdsList = new ArrayList<>();
				prodIdsList.add(productId);
				prodTypeMap.put(productType, prodIdsList);
			}
		}

		return prodTypeMap;
	}

	public Map<Integer, DefaultMessageDTO> getDefaultMessagesMap() throws IOException, ProcCallException {
		Map<Integer, DefaultMessageDTO> msgMap = new HashMap<>();
		String sql = "SELECT * FROM ECMP_T_DEFAULT_MESSAGE";

		VoltTable[] msgTbl = voltDbClient.callProcedure(AD_HOC, sql).getResults();
		while (msgTbl[0].advanceRow()) {
			int langCd = (int) msgTbl[0].get(LANG_CODE, VoltType.INTEGER);
			String defaultMsg = msgTbl[0].getString(DEFAULT_MESSAGE);
			String errorMsg = msgTbl[0].getString(ERROR_MESSAGE);
			DefaultMessageDTO msgDTO = new DefaultMessageDTO();
			msgDTO.setLandCd(langCd);
			msgDTO.setDefaultMsg(defaultMsg);
			msgDTO.setErrorMsg(errorMsg);
			msgMap.put(langCd, msgDTO);
		}
		return msgMap;
	}

	public String getDefaultMessageForLangCode(final int langCode) throws Exception {
		String sql = PropertiesLoader.getValue(DEFAULT_MSG_PROC_NAME);
		VoltTable[] voltTable = voltDbClient.callProcedure(sql, langCode).getResults();
		String message = null;
		if (voltTable[0].advanceRow()) {
			message = voltTable[0].getString(DEFAULT_MESSAGE);
		}

		return message;
	}

	public String getErrorMessageForLangCode(final int langCode) throws Exception {
		String sql = PropertiesLoader.getValue(DEFAULT_ERRMSG_PROC_NAME);
		VoltTable[] voltTable = voltDbClient.callProcedure(sql, langCode).getResults();
		String message = null;
		if (voltTable[0].advanceRow()) {
			message = voltTable[0].getString(ERROR_MESSAGE);
		}

		return message;
	}

	public String getProductForUserSelection(final UssdMessage ussdMessage, final int selection) throws Exception {
		String sql = PropertiesLoader.getValue(TRX_PRODIDMAP_PROC_NAME);
		VoltTable[] voltTable = voltDbClient.callProcedure(sql, ussdMessage.getSourceAddress(), ussdMessage.getSessionId()).getResults();
		String productId = null;
		if (voltTable[0].advanceRow()) {
			switch (selection) {
			case 1:
				productId = voltTable[0].getString(PID_1);
				break;
			case 2:
				productId = voltTable[0].getString(PID_2);
				break;
			case 3:
				productId = voltTable[0].getString(PID_3);
				break;
			default:
				LOG.debug("Invalid User selection!");
			}
		}
		return productId;
	}

	public String getProductForUserSelection(UserInfo userInfo, int selection) throws IOException, ProcCallException{
		String sql = "USSD_P_TRX_PRODID_MAP_SELECT";
		VoltTable[] voltTable = voltDbClient.callProcedure(sql, userInfo.getUserMsgRef(), userInfo.getTxId(), userInfo.getMsisdn()).getResults();
		String productId = null;
		if (voltTable[0].advanceRow()) {
			switch (selection) {
			case 1:
				productId = voltTable[0].getString(PID_1);
				break;
			case 2:
				productId = voltTable[0].getString(PID_2);
				break;
			case 3:
				productId = voltTable[0].getString(PID_3);
				break;
			case 4:
				productId = voltTable[0].getString(PID_4);
				break;
			case 5:
				productId = voltTable[0].getString(PID_5);
				break;
			default:
				LOG.debug("Invalid User selection!");
			}
		}
		return productId;
	}

	public Integer getProductCountForSelection(UserInfo userInfo) throws IOException, ProcCallException  {
		String sql = "USSD_P_TRX_PRODID_MAP_SELECT";
		VoltTable[] voltTable = voltDbClient.callProcedure(sql, userInfo.getUserMsgRef(), userInfo.getTxId(), userInfo.getMsisdn()).getResults();
		Optional<String> productId;
		Integer productCount = 0;
		if (voltTable[0].advanceRow()) {

			productId = Optional.ofNullable(voltTable[0].getString(PID_1));
			if (productId.isPresent()) {
				productCount += 1;
			}
			productId = Optional.ofNullable(voltTable[0].getString(PID_2));
			if (productId.isPresent()) {
				productCount += 1;
			}
			productId = Optional.ofNullable(voltTable[0].getString(PID_3));
			if (productId.isPresent()) {
				productCount += 1;
			}
			productId = Optional.ofNullable(voltTable[0].getString(PID_4));
			if (productId.isPresent()) {
				productCount += 1;
			}
			productId = Optional.ofNullable(voltTable[0].getString(PID_5));
			if (productId.isPresent()) {
				productCount += 1;
			}
		}
		return productCount;
	}

	public MessageDTO getTxnProdIdMap(final String txnId, final int ussdMessgeId) throws Exception {
		String sql = PropertiesLoader.getValue(USSD_TXNPRODMAP_PROC_NAME);
		VoltTable[] voltTable = voltDbClient.callProcedure(sql, txnId, ussdMessgeId).getResults();
		MessageDTO messageDTO = null;
		if (voltTable[0].advanceRow()) {
			messageDTO = new MessageDTO();
			messageDTO.setMsisdn(voltTable[0].getString(MSISDN));
			messageDTO.setUssdMsgId(ussdMessgeId);
			messageDTO.setDateTime(voltTable[0].getTimestampAsSqlTimestamp(DATE_TIME));
			messageDTO.setTxnId(voltTable[0].getString(TRX_ID));
		}
		return messageDTO;
	}

	public Map<String, ProductInfo> getProductInfoMap(String procName) throws IOException, ProcCallException {
		Map<String, ProductInfo> productInfoMap = new HashMap<>();

		VoltTable[] productInfoTbl = voltDbClient.callProcedure(procName).getResults();
		while (productInfoTbl[0].advanceRow()) {
			ProductInfo productInfo = new ProductInfo();
			String pid = productInfoTbl[0].getString(PRODUCT_ID);
			int langCd = (int) productInfoTbl[0].get(LANG_CODE, VoltType.INTEGER);
			productInfo.setProductID(pid);
			productInfo.setLangCode(langCd);
			productInfo.setProductDesc(productInfoTbl[0].getString(PRODUCT_DESC));
			productInfo.setbValue(productInfoTbl[0].getString(B_VALUE));
			productInfo.setcValue(productInfoTbl[0].getString(C_VALUE));
			productInfo.setProductType(productInfoTbl[0].getString(PRODUCT_TYPE));
			productInfo.setProductSubType(productInfoTbl[0].getString(PRODUCT_SUBTYPE));
			productInfoMap.put(pid + "_" + langCd, productInfo);
		}
		return productInfoMap;
	}

	public Map<String, ProductInfo> getMorningOfferProductInfoMap(String procName) throws IOException, ProcCallException {
		Map<String, ProductInfo> productInfoMap = new HashMap<>();

		VoltTable[] productInfoTbl = voltDbClient.callProcedure(procName).getResults();
		while (productInfoTbl[0].advanceRow()) {
			ProductInfo productInfo = new ProductInfo();
			String pid = productInfoTbl[0].getString(PRODUCT_ID);
			int langCd = (int) productInfoTbl[0].get(LANG_CODE, VoltType.INTEGER);
			productInfo.setProductID(pid);
			productInfo.setLangCode(langCd);
			productInfo.setProductDesc(productInfoTbl[0].getString(PRODUCT_DESC));
			productInfoMap.put(pid + "_" + langCd, productInfo);
		}
		return productInfoMap;
	}

	public Map<String, ProductInfo> getLocationProductInfoMap() throws Exception {
		Map<String, ProductInfo> productInfoMap = new HashMap<>();

		VoltTable[] productInfoTbl = voltDbClient.callProcedure(PropertiesLoader.getValue(USSD_LOCATION_PRODINFO_PROC_NAME)).getResults();
		while (productInfoTbl[0].advanceRow()) {
			ProductInfo productInfo = new ProductInfo();
			String pid = productInfoTbl[0].getString(PRODUCT_ID);
			int langCd = (int) productInfoTbl[0].get(LANG_CODE, VoltType.INTEGER);
			productInfo.setProductID(pid);
			productInfo.setLangCode(langCd);
			productInfo.setProductDesc(productInfoTbl[0].getString(PRODUCT_DESC));
			productInfo.setbValue(productInfoTbl[0].getString(B_VALUE));
			productInfo.setcValue(productInfoTbl[0].getString(C_VALUE));
			productInfo.setPoolID(productInfoTbl[0].getString(POOL_ID));
			productInfo.setProductType(productInfoTbl[0].getString(PRODUCT_TYPE));
			productInfo.setProductSubType(productInfoTbl[0].getString(PRODUCT_SUBTYPE));
			productInfoMap.put(pid + "_" + langCd, productInfo);
		}
		return productInfoMap;
	}

	/**
	 * Retrieves user information based on the provided MSISDN.
	 *
	 * @param msisdn The MSISDN of the user.
	 * @return A {@link UserInfo} object containing user information.
	 * @throws Exception If an error occurs during the retrieval process.
	 */
	public UserInfo getUserInfo(String msisdn) throws Exception {
		// Determine the SQL query based on whether blacklist functionality is enabled.
	    String sql = isBlacklistEnabled ?
	            PropertiesLoader.getValue(USSD_GET_USER_INFO_WITH_BLIST_PROC) :
	            PropertiesLoader.getValue(USSD_GET_USER_INFO_PROC);

		String value;

		// Adjust MSISDN format if needed.
		if (msisdn.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
			msisdn = msisdn.replaceFirst(PropertiesLoader.getValue(SUBID_CNTRYCD), "");
		}
		LOG.debug("getUserInfo");
		
		// Call the stored procedure to retrieve user information.
		VoltTable[] voltTables = voltDbClient.callProcedure(sql, msisdn).getResults();
		UserInfo userInfo = null;
		
		// Process the retrieved data.
		while (voltTables[0].advanceRow()) {
			userInfo = new UserInfo();
			userInfo.setMsisdn(msisdn);
			
			// Set AA Eligibility.
	        value = voltTables[0].getString(AA_ELIGIBLE);
	        userInfo.setAaEligible((value == null || value.isEmpty()) ? "0" : value);

	        // Set A Value.
	        value = voltTables[0].getString(A_VALUE);
	        userInfo.setaValue((value == null || value.isEmpty()) ? 0 : Float.parseFloat(value));

	        // Set Preferred Payment Method.
	        value = voltTables[0].getString(PREF_PAY_METHOD);
	        userInfo.setPrefPayMethod((value == null || value.isEmpty()) ? PREF_PAY_METHOD_G : value);

	        // Set ML Flag.
	        userInfo.setMlFlag(FLAG_Y.equalsIgnoreCase(voltTables[0].getString(ML_CUSTOMER_FLAG)));

	        // Set Random Flag.
	        userInfo.setRandomFlag(FLAG_Y.equalsIgnoreCase(voltTables[0].getString(RANDOM_FLAG)));

	        // Set JFU Eligibility.
	        userInfo.setJFUEligible(FLAG_Y.equalsIgnoreCase(voltTables[0].getString(J4U_ELIGIBILITY)));

	        // Set Language Code.
	        userInfo.setLangCode(voltTables[0].getString(LANG_CODE));

	        // Set Offer Refresh Flag.
	        userInfo.setOfferRefreshFlag(voltTables[0].getString(OFFER_REFRESH_FLAG));

	        // Set RAG User.
	        userInfo.setRagUser(null != voltTables[0].getString(RAG_MSISDN));

	        // Set SAG User.
	        userInfo.setSagUser(null != voltTables[0].getString(SAG_MSISDN));

	        // Set Mpesa User.
	        userInfo.setMpesaUser(FLAG_Y.equalsIgnoreCase(voltTables[0].getString(MPESA_USER_FLAG)));

	        // Set PED Eligibility.
	        userInfo.setPedEligibility(FLAG_Y.equalsIgnoreCase(voltTables[0].getString(PED_ELIGIBILITY)));

	        // Set Location Random Flag.
	        userInfo.setLocationRandomFlag(FLAG_Y.equalsIgnoreCase(voltTables[0].getString(LOCATION_RANDOM_FLAG)));

	        // Set Morning Offer Flag.
	        userInfo.setMorningOfferFlag(FLAG_Y.equalsIgnoreCase(voltTables[0].getString(MORNING_OFFER_ELIGIBILITY)));

	        // Set J4U New User.
	        userInfo.setJ4uNewUser(false);

	        // Set Offer Count.
	        userInfo.setOfferCount(MAX_PRODIDS_CNT_3);

	        // Set Blacklisted User (if blacklist functionality is enabled).
	        if (isBlacklistEnabled) {
	            userInfo.setBlacklistedUser(null != voltTables[0].getString(BLACKLIST_MSISDN));
	        }

			LOG.debug("getUserInfo method ends");

		}
		return userInfo;
	}

	public boolean getConsentInfo(String msisdn) throws Exception {
		LOG.info("getConsentInfo - START");

		boolean result = false;
		String procName = PropertiesLoader.getValue(USSD_P_GET_CONSENT_STATUS);

		if (msisdn.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
			msisdn = msisdn.replaceFirst(PropertiesLoader.getValue(SUBID_CNTRYCD), "");
		}
		VoltTable[] voltTable = voltDbClient.callProcedure(procName, msisdn).getResults();
		if (voltTable[0].advanceRow() && FLAG_Y.equalsIgnoreCase(voltTable[0].getString(CONSENT_FLAG))) {
			result = true;
		}
		LOG.info("Consent Flag - " + result);
		return result;
	}

	public MLOfferMsg getSubMenuForTgtMLUser(String messageBody, String msisdn) throws IOException, ProcCallException {
		String procName = "USSD_P_ML_OFFER_MSG_SELECT";
		MLOfferMsg mlOfferMsg = null;
		VoltTable[] voltTables = voltDbClient.callProcedure(procName, msisdn, messageBody).getResults();
		while (voltTables[0].advanceRow()) {
			mlOfferMsg = new MLOfferMsg();
			mlOfferMsg.setProdIds(voltTables[0].getString("PRODUCT_IDS"));
			mlOfferMsg.setMenuContent(voltTables[0].getString("MENU_CONTENT"));
			mlOfferMsg.setRfValues(voltTables[0].getString("RF_VALUE"));
		}

		return mlOfferMsg;
	}

	public List<String> getMorningOfferWhiteList(String msisdn) throws Exception {
		LOG.info("getMorningOfferWhiteList - START");
		List<String> prodIdsList = new ArrayList<>();
		String procName = PropertiesLoader.getValue(USSD_ML_GET_MORNING_OFFER_PROC);
		VoltTable[] voltTables = voltDbClient.callProcedure(procName, msisdn).getResults();
		while (voltTables[0].advanceRow()) {
			prodIdsList.add(voltTables[0].getString(PRODUCT_ID));
		}
		LOG.info("getMorningOfferWhiteList - END - Total Record -" + prodIdsList.size());
		return prodIdsList;
	}

	public List<String> getOffersByBalanceAndRank(String msisdn, String prodType, double accBal) throws IOException, ProcCallException  {

		StringBuilder procName = new StringBuilder("USSD_P_ML_OFFER_SELECT");
		LOG.debug(PROC_NAME + procName);
		List<String> prodIdsList = new ArrayList<>();
		VoltTable[] voltTables = voltDbClient.callProcedure(procName.toString(), msisdn, prodType, accBal).getResults();
		while (voltTables[0].advanceRow()) {
			prodIdsList.add(voltTables[0].getString(FIELD_PRODUCT_ID));
		}

		return prodIdsList;

	}

	public List<String> getSocialOffersByBalanceAndRank(String msisdn, double accBal) throws Exception {

		String procName = PropertiesLoader.getValue(USSD_ML_SOCIAL_OFFER_BYRANK_PROC).trim();
		LOG.debug(PROC_NAME + procName);
		List<String> prodIdsList = new ArrayList<>();
		VoltTable[] voltTables = voltDbClient.callProcedure(procName, msisdn, accBal).getResults();
		while (voltTables[0].advanceRow()) {
			prodIdsList.add(voltTables[0].getString(FIELD_PRODUCT_ID));
		}

		return prodIdsList;

	}

	public List<String> getOffersByExpectedValue(String msisdn, String prodType, List<String> prodIdsList) throws IOException, ProcCallException {

		StringBuilder procName = new StringBuilder("USSD_P_ML_OFFER_SELECT_BY_EXPVAL");
		LOG.debug(PROC_NAME + procName);
		String[] prodIdsArr = prodIdsList.toArray(new String[0]);
		int limit = MAX_PRODIDS_CNT_3 - prodIdsList.size();
		VoltTable[] voltTables = voltDbClient.callProcedure(procName.toString(), msisdn, prodType, prodIdsArr, limit).getResults();
		while (voltTables[0].advanceRow()) {
			prodIdsList.add(voltTables[0].getString(FIELD_PRODUCT_ID));
		}

		return prodIdsList;
	}

	public List<String> getSocialOffersByExpectedValue(String msisdn, List<String> prodIdsList) throws Exception {

		String procName = PropertiesLoader.getValue(USSD_ML_SOCIAL_OFFER_BYEXP_PROC);
		LOG.debug(PROC_NAME + procName);
		String[] prodIdsArr = prodIdsList.toArray(new String[0]);
		int limit = MAX_PRODIDS_CNT_3 - prodIdsList.size();
		VoltTable[] voltTables = voltDbClient.callProcedure(procName, msisdn, prodIdsArr, limit).getResults();
		while (voltTables[0].advanceRow()) {
			prodIdsList.add(voltTables[0].getString(FIELD_PRODUCT_ID));
		}

		return prodIdsList;
	}

	public UserInfo getRagUserInfo(UserInfo userInfo) throws Exception {
		String procedure = "USSD_P_ML_RAG_INFO_SELECT";
		String msisdn = userInfo.getMsisdn();
		HashMap<String, String> ragInfo = null;
		if (msisdn.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
			msisdn = msisdn.replaceFirst(PropertiesLoader.getValue(SUBID_CNTRYCD), "");
		}
		VoltTable[] voltTables = voltDbClient.callProcedure(procedure, msisdn, userInfo.getLangCode()).getResults();

		if (voltTables[0].getRowCount() > 0) {
			while (voltTables[0].advanceRow()) {
				userInfo.setRagEligibleFlag(!"".equals(voltTables[0].getString(MSISDN)) || !voltTables[0].getString(MSISDN).isEmpty());
				userInfo.setRagNeverOptInFlag(FLAG_N.equalsIgnoreCase(voltTables[0].getString(RAG_OPT_FLAG)));
				userInfo.setRagOptInFlag(FLAG_Y.equalsIgnoreCase(voltTables[0].getString(RAG_OPT_FLAG)));
				userInfo.setRagGoalReachedFlag(FLAG_Y.equalsIgnoreCase(voltTables[0].getString(RAG_GOAL_REACHED_FLAG)));
				ragInfo = new HashMap<>();
				ragInfo.put(REWARD_CODE, voltTables[0].getString(REWARD_CODE));
				ragInfo.put(REWARD_INFO, voltTables[0].getString(REWARD_INFO));
				ragInfo.put(PRODUCT_VALIDITY, voltTables[0].getString(PRODUCT_VALIDITY));
				ragInfo.put(PRODUCT_VALUE, voltTables[0].getString(PRODUCT_VALUE));
				ragInfo.put(LAST_RECHARGE_TIME, voltTables[0].getString(LAST_RECHARGE_TIME));
				ragInfo.put(WEEK_END_DATE, voltTables[0].getString(WEEK_END_DATE));
				ragInfo.put(NEXT_AVAILABLE_OFFER_DATE, voltTables[0].getString(NEXT_AVAILABLE_OFFER_DATE));
				ragInfo.put(REMAINING_EFFORT, String.valueOf((int) voltTables[0].get(REMAINING_EFFORT, VoltType.INTEGER)));
				ragInfo.put(RECHARGE_TARGET, String.valueOf((int) voltTables[0].get(RECHARGE_TARGET, VoltType.INTEGER)));
				userInfo.setRagInfo(ragInfo);
			}
		} else {
			userInfo.setRagEligibleFlag(false);
			userInfo.setRagOptInFlag(false);
			userInfo.setRagNeverOptInFlag(true);
			userInfo.setRagGoalReachedFlag(false);
		}

		return userInfo;

	}

	public UserInfo getSagUserInfo(UserInfo userInfo) throws Exception {
		String procedure = PropertiesLoader.getValue(USSD_P_ML_SAG_INFO_SELECT);
		String msisdn = userInfo.getMsisdn();
		HashMap<String, String> sagInfo = null;
		if (msisdn.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
			msisdn = Utils.getMsisdnWithoutCcode(msisdn);
		}
		VoltTable[] voltTables = voltDbClient.callProcedure(procedure, msisdn, userInfo.getLangCode()).getResults();

		if (voltTables[0].getRowCount() > 0) {
			LOG.debug("SAG MSISDN/Product Info found in ERED_T_SAG_EFFORT_REWARD_WL, and ECMP_T_SAG_PROD_INFO => ");
			while (voltTables[0].advanceRow()) {
				userInfo.setSagEligibleFlag(!"".equals(voltTables[0].getString(MSISDN)) || !voltTables[0].getString(MSISDN).isEmpty());
				userInfo.setSagNeverOptInFlag(FLAG_N.equalsIgnoreCase(voltTables[0].getString(SAG_OPT_FLAG)));
				userInfo.setSagOptInFlag(FLAG_Y.equalsIgnoreCase(voltTables[0].getString(SAG_OPT_FLAG)));
				userInfo.setSagGoalReachedFlag(FLAG_Y.equalsIgnoreCase(voltTables[0].getString(SAG_GOAL_REACHED_FLAG)));
				sagInfo = new HashMap<>();
				sagInfo.put(REWARD_CODE, voltTables[0].getString(REWARD_CODE));
				sagInfo.put(REWARD_INFO, voltTables[0].getString(REWARD_INFO));
				sagInfo.put(PRODUCT_VALIDITY, voltTables[0].getString(PRODUCT_VALIDITY));
				sagInfo.put(PRODUCT_VALUE, voltTables[0].getString(PRODUCT_VALUE));
				sagInfo.put(LAST_SPEND_TIME, voltTables[0].getString(LAST_SPEND_TIME));
				sagInfo.put(WEEK_END_DATE, voltTables[0].getString(WEEK_END_DATE));
				sagInfo.put(NEXT_AVAILABLE_OFFER_DATE, voltTables[0].getString(NEXT_AVAILABLE_OFFER_DATE));
				sagInfo.put(REMAINING_EFFORT, String.valueOf((int) voltTables[0].get(REMAINING_EFFORT, VoltType.INTEGER)));
				sagInfo.put(SPEND_TARGET, String.valueOf((int) voltTables[0].get(SPEND_TARGET, VoltType.INTEGER)));
				userInfo.setSagInfo(sagInfo);
			}
		} else {
			LOG.info(
					"MSISDN || PRODID => Not found in ERED_T_SAG_EFFORT_REWARD_WL || ERED_T_SAG_EFFORT_OPT_INFO || ECMP_T_SAG_PROD_INFO!");
			userInfo.setSagEligibleFlag(false);
			userInfo.setSagOptInFlag(false);
			userInfo.setSagNeverOptInFlag(true);
			userInfo.setSagGoalReachedFlag(false);
		}

		return userInfo;

	}

	public RankingFormulae getRFParams(String msisdn, String prodType, int langCode, RankingFormulae rankingFormulae)
			throws Exception {
		LOG.debug("getRFParams for by calling procedure USSD_P_ML_AA_OFFER- ");
		StringBuilder procName = new StringBuilder(PropertiesLoader.getValue(USSD_P_ML_AA_OFFER).trim());
		LOG.debug("MSISDN - " + msisdn + "----" + prodType + ", procedure Name=> " + procName.toString());
		VoltTable[] voltTables = voltDbClient.callProcedure(procName.toString(), prodType, msisdn).getResults();
		List<OfferParams> offerParamsList = new ArrayList<>();
		List<String> prodIdsList = new ArrayList<>();
		OfferParams offerParams;
		LOG.debug("voltTables entries - " + voltTables[0].getRowCount());
		while (voltTables[0].advanceRow()) {
			offerParams = new OfferParams();
			offerParams.setOfferPrice(Long.parseLong(voltTables[0].getString(FIELD_PRODUCT_PRICE)));
			offerParams.setExpectedValue(Float.parseFloat(voltTables[0].getString(FIELD_EXPECTED_VALUE)));
			offerParams.setOfferId(voltTables[0].getString(FIELD_PRODUCT_ID));
			prodIdsList.add(voltTables[0].getString(FIELD_PRODUCT_ID));

			offerParamsList.add(offerParams);
		}
		rankingFormulae.setOfferParams(getAAOfferValues(prodType, offerParamsList, prodIdsList, langCode));
		LOG.debug("getRFParams - END");
		return rankingFormulae;
	}

	public RankingFormulae getSocialRFParams(String msisdn, String prodType, int langCode,
			RankingFormulae rankingFormulae) throws Exception {
		LOG.debug("getSocialRFParams for by calling procedure USSD_ML_SOCIAL_QUERY_OFFER_PROC- ");
		String procName = PropertiesLoader.getValue(USSD_ML_SOCIAL_QUERY_OFFER_PROC).trim();
		LOG.debug("MSISDN :: " + msisdn + "----" + prodType + ", Procedure Name :: " + procName);
		VoltTable[] voltTables = voltDbClient.callProcedure(procName, msisdn).getResults();
		List<OfferParams> offerParamsList = new ArrayList<>();
		List<String> prodIdsList = new ArrayList<>();
		OfferParams offerParams;
		LOG.debug("voltTables entries - " + voltTables[0].getRowCount());
		while (voltTables[0].advanceRow()) {
			offerParams = new OfferParams();
			offerParams.setOfferPrice(Long.parseLong(voltTables[0].getString(FIELD_PRODUCT_PRICE)));
			offerParams.setExpectedValue(Float.parseFloat(voltTables[0].getString(FIELD_EXPECTED_VALUE)));
			offerParams.setOfferId(voltTables[0].getString(FIELD_PRODUCT_ID));
			prodIdsList.add(voltTables[0].getString(FIELD_PRODUCT_ID));

			offerParamsList.add(offerParams);
		}
		rankingFormulae.setOfferParams(getSocialOfferValues(prodType, offerParamsList, prodIdsList, langCode));
		LOG.debug("getSocialRFParams - END");
		return rankingFormulae;
	}

	public Map<String, String> getPoolIdForCellId() throws Exception {
		LOG.info("getPoolIdForCellId - START");
		Map<String, String> cellPoolMap = new HashMap<>();
		VoltTable[] voltTable;
		voltTable = voltDbClient.callProcedure(PropertiesLoader.getValue(USSDConstants.LOCATION_MODULE_GET_POOL_ID).trim()).getResults();
		while (voltTable[0].advanceRow()) {
			cellPoolMap.put(voltTable[0].getString(CELL_ID_KEY_NAME), voltTable[0].getString(USSDConstants.POOL_ID));
		}
		LOG.info("getPoolIdForCellId - END - Total Records -" + cellPoolMap.size());
		return cellPoolMap;
	}

	public RankingFormulae getRFParamsForLocation(String msisdn, String prodType, int langCode,
			RankingFormulae rankingFormulae, String poolId) throws Exception {
		LOG.debug("getRFParams for Location - From ENBA_T_J4U_ML_LOCATION_OFFER Table ");
		StringBuilder procName = new StringBuilder(PropertiesLoader.getValue(ECMP_P_J4U_ML_LOCATION_OFFER).trim());
		LOG.debug("MSISDN :: " + msisdn + "----" + prodType + ", Procedure Name :: " + procName.toString());
		VoltTable[] voltTables = voltDbClient.callProcedure(procName.toString(), prodType, msisdn).getResults();
		List<OfferParams> offerParamsList = new ArrayList<>();
		List<String> prodIdsList = new ArrayList<>();
		OfferParams offerParams;
		LOG.debug("voltTables - " + voltTables[0].getRowCount());
		while (voltTables[0].advanceRow()) {
			offerParams = new OfferParams();
			offerParams.setOfferPrice(Long.parseLong(voltTables[0].getString(FIELD_PRODUCT_PRICE)));
			offerParams.setExpectedValue(Float.parseFloat(voltTables[0].getString(FIELD_EXPECTED_VALUE)));
			offerParams.setOfferId(voltTables[0].getString(FIELD_PRODUCT_ID));
			prodIdsList.add(voltTables[0].getString(FIELD_PRODUCT_ID));

			offerParamsList.add(offerParams);
		}

		rankingFormulae.setPoolId(poolId);
		rankingFormulae.setOfferParams(getAAOfferValuesForLocation(offerParamsList, langCode, poolId));
		LOG.debug("getRFParams For Location- END");
		return rankingFormulae;
	}

	private List<OfferParams> getAAOfferValues(String prodType, List<OfferParams> offerParamsList,
			List<String> prodIdsList, int langCode) {

		LOG.debug("getAAOfferValues - prodType=>" + prodType + " langCode=>" + langCode);
		LOG.debug("prodIdsArr=>" + prodIdsList);
		LOG.debug("ml offerParamsList => " + offerParamsList);
		List<OfferParams> offerParamsList1 = new ArrayList<>();
		ProductInfoCache productInfoCache = ProductInfoCache.instance();
		for (int offerCount = 0; offerCount < offerParamsList.size(); offerCount++) {
			OfferParams offerParams = offerParamsList.get(offerCount);
			ProductInfo productInfo = productInfoCache.getML(offerParams.getOfferId() + "_" + langCode);
			if (null != productInfo) {
				offerParams.setbValue(calculateFloatValue(productInfo.getbValue()));
				offerParams.setcValue(calculateFloatValue(productInfo.getcValue()));
				offerParamsList1.add(offerParams);
			}
		}
		LOG.debug("getAAOfferValues - END=>  ml offer=>" + offerParamsList1.size());
		return offerParamsList1;
	}

	public List<OfferParams> getSocialOfferValues(String prodType, List<OfferParams> offerParamsList, List<String> prodIdsList, int langCode) {

		LOG.debug("getSocialOfferValues - prodType=>" + prodType + " langCode=>" + langCode);
		LOG.debug("prodIdsArr=>" + prodIdsList);
		LOG.debug("ml offerParamsList => " + offerParamsList);
		List<OfferParams> offerParamsList1 = new ArrayList<>();
		ProductInfoCache productInfoCache = ProductInfoCache.instance();
		for (int offerCount = 0; offerCount < offerParamsList.size(); offerCount++) {
			OfferParams offerParams = offerParamsList.get(offerCount);
			ProductInfo productInfo = productInfoCache.getSocial(offerParams.getOfferId() + "_" + langCode);
			if (null != productInfo) {
				offerParams.setbValue(calculateFloatValue(productInfo.getbValue()));
				offerParams.setcValue(calculateFloatValue(productInfo.getcValue()));
				offerParamsList1.add(offerParams);
			}
		}
		LOG.debug("getSocialOfferValues - END=>  ml offer=>" + offerParamsList1.size());
		return offerParamsList1;
	}

	public List<OfferParams> getAAOfferValuesForLocation(List<OfferParams> offerParamsList, int langCode, String poolId) {

		LOG.debug("getLocationProductInfo - from ECMP_T_LOCATION_PROD_INFO table");
		List<OfferParams> offerParamsArrayList = new ArrayList<>();
		ProductInfoCache productInfoCache = ProductInfoCache.instance();
		for (int offerCount = 0; offerCount < offerParamsList.size(); offerCount++) {
			OfferParams offerParams = offerParamsList.get(offerCount);
			ProductInfo productInfo = productInfoCache.getLocation(offerParams.getOfferId() + "_" + langCode);
			if (null != productInfo && poolId.equalsIgnoreCase(productInfo.getPoolID())) {
				offerParams.setbValue(calculateFloatValue(productInfo.getbValue()));
				offerParams.setcValue(calculateFloatValue(productInfo.getcValue()));
				offerParams.setProductDescription(productInfo.getProductDesc());
				offerParamsArrayList.add(offerParams);
			}
		}
		LOG.debug("offerParamsArrayList for location=>" + offerParamsArrayList);
		return offerParamsArrayList;
	}

	private static float calculateFloatValue(String value) {
		if (value == null || value.equals("")) {
			return 1;
		} else {
			return Float.parseFloat(value);
		}
	}

	public Map<String, String> getProductPriceMap() throws Exception {
		LOG.debug("getProductPriceMap - START");
		String procName = PropertiesLoader.getValue(USSDConstants.USSD_PRODUCT_PRICE_LIST_PROC);
		Map<String, String> productPriceMap = new HashMap<>();
		VoltTable[] voltTables = voltDbClient.callProcedure(procName).getResults();
		LOG.debug("voltTables - " + voltTables[0].getRowCount());
		while (voltTables[0].advanceRow()) {
			productPriceMap.put(voltTables[0].getString(PRODUCT_ID), voltTables[0].getString(PRODUCT_PRICE));
		}
		LOG.debug("getProductPriceMap - END");
		return productPriceMap;
	}

	public List<String> getRagDefaultValues() throws IOException, ProcCallException  {
		LOG.debug("getRagDefaultValues - START");
		List<String> ragDefValues = new ArrayList<>();
		VoltTable[] voltTables = voltDbClient.callProcedure(USSD_P_ML_RAG_USER_RECORD_SELECT).getResults();
		if (voltTables[0].getRowCount() > 0) {
			while (voltTables[0].advanceRow()) {
				ragDefValues.add(voltTables[0].getString(WEEK_START_DATE));
				ragDefValues.add(voltTables[0].getString(WEEK_END_DATE));
				ragDefValues.add(voltTables[0].getString(NEXT_AVAILABLE_OFFER_DATE));
			}
		}
		LOG.debug("getRagDefaultValues - END");
		return ragDefValues;
	}

	public List<String> getSagDefaultValues() throws Exception {
		LOG.debug("getSagDefaultValues - START");
		String procName = PropertiesLoader.getValue(USSD_P_ML_SAG_USER_RECORD_SELECT);
		List<String> sagDefValues = new ArrayList<>();
		VoltTable[] voltTables = voltDbClient.callProcedure(procName).getResults();
		if (voltTables[0].getRowCount() > 0) {
			while (voltTables[0].advanceRow()) {
				sagDefValues.add(voltTables[0].getString(WEEK_START_DATE));
				sagDefValues.add(voltTables[0].getString(WEEK_END_DATE));
				sagDefValues.add(voltTables[0].getString(NEXT_AVAILABLE_OFFER_DATE));
			}
		}
		LOG.debug("getSagDefaultValues - END");
		return sagDefValues;
	}

	public Map<String, List<String>> getProductNonPromotionalInfo() throws Exception {
		Map<String, List<String>> prodTypeMap = new HashMap<>();
		List<String> prodIdsList = null;
		String sql = PropertiesLoader.getValue(USSD_NON_PROMO_PRODUCT_INFO);
		VoltTable[] templatesTbl = voltDbClient.callProcedure(sql).getResults();
		while (templatesTbl[0].advanceRow()) {
			String productType = templatesTbl[0].getString("PRODUCT_TYPE");
			String productId = templatesTbl[0].getString(FIELD_PRODUCT_ID);

			if (prodTypeMap.containsKey(productType)) {
				prodTypeMap.get(productType).add(productId);
			} else {
				prodIdsList = new ArrayList<>();
				prodIdsList.add(productId);
				prodTypeMap.put(productType, prodIdsList);
			}
		}

		return prodTypeMap;
	}

	public String getCSType(String msisdn) {
		String csType = USSDConstants.CS_TYPE_HW;
		String finalmsisdn = validateMSISDN(msisdn);
		String msisdnrange = finalmsisdn.substring(0, rangelen);
		try {
			csType = queryCSTypeForRange(msisdnrange);
			if (csType.equalsIgnoreCase(USSDConstants.CS_TYPE_IM)) {
				csType = queryCSTypeForMSISDN(finalmsisdn);
			}
		} catch (Exception e) {
			LOG.error("RouterDao::getCSType Exception ", e);
		}

		return csType;
	}

	private String queryCSTypeForRange(String msisdnrange) throws Exception {
		String csType = USSDConstants.CS_TYPE_HW;
		LOG.debug("queryCSTypeForRange BEGIN - MSISDNRANGE :: " + msisdnrange);

		String procedure = PropertiesLoader.getValue(ROUTER_CS_MSISDNRANGE_QUERY);
		VoltTable[] voltTable = voltDbClient.callProcedure(procedure, msisdnrange).getResults();

		if (voltTable[0].getRowCount() != 0) {
			VoltTableRow vRow = voltTable[0].fetchRow(0);
			csType = vRow.getString(CS_TYPE);
		}
		LOG.info("queryCSTypeForRange - MSISDNRANGE|CSTYPE - " + msisdnrange + "| " + csType);
		return csType;
	}

	private String queryCSTypeForMSISDN(String msisdn) throws Exception {
		String csType = USSDConstants.CS_TYPE_HW;
		LOG.debug("queryCSTypeForMSISDN BEGIN - MSISDN :: " + msisdn);

		String procedure = PropertiesLoader.getValue(ROUTER_CS_MSISDN_QUERY);

		VoltTable[] voltTable = voltDbClient.callProcedure(procedure, msisdn).getResults();
		if (voltTable[0].getRowCount() != 0) {
			VoltTableRow vRow = voltTable[0].fetchRow(0);
			csType = vRow.getString(CS_TYPE);
		}
		LOG.info("queryCSTypeForMSISDN - MSISDN|CSTYPE - " + msisdn + "|" + csType);
		return csType;
	}

	private String validateMSISDN(String msisdn) {
		String finalmsisdn = msisdn;
		if (!msisdn.startsWith(countrycode)) {
			StringBuilder msisdnbuilder = new StringBuilder();
			if (msisdn.startsWith(MSISDN_PREFIX_ZERO)) {
				msisdnbuilder.append(countrycode);
				msisdnbuilder.append(msisdn.substring(1));
			} else {
				msisdnbuilder.append(countrycode);
				msisdnbuilder.append(msisdn);
			}
			finalmsisdn = msisdnbuilder.toString();
		}

		return finalmsisdn;
	}

	public boolean getBlacklistedMsisdn(String msisdn) throws Exception {

		boolean blacklistedFlag = false;
		if (msisdn.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
			msisdn = Utils.getMsisdnWithoutCcode(msisdn);
		}
		String procedure = PropertiesLoader.getValue(P_GET_OPENET_BLACKLIST_MSISDN);
		VoltTable[] voltTables = voltDbClient.callProcedure(procedure, msisdn).getResults();
		if (voltTables[0].getRowCount() > 0 && voltTables[0].advanceRow()) {
			blacklistedFlag = !"".equals(voltTables[0].getString(MSISDN)) || !voltTables[0].getString(MSISDN).isEmpty();
		}
		return blacklistedFlag;
	}

	public Map<String, ProductInfo> getTownOfferProductInfoMap(String procName) throws IOException, ProcCallException {
		Map<String, ProductInfo> townProductInfoMap = new HashMap<>();

		VoltTable[] productInfoTbl = voltDbClient.callProcedure(procName).getResults();
		while (productInfoTbl[0].advanceRow()) {
			ProductInfo productInfo = new ProductInfo();
			String pid = productInfoTbl[0].getString(PRODUCT_ID);
			int langCd = (int) productInfoTbl[0].get(LANG_CODE, VoltType.INTEGER);
			productInfo.setProductID(pid);
			productInfo.setLangCode(langCd);
			productInfo.setProductDesc(productInfoTbl[0].getString(PRODUCT_DESC));
			townProductInfoMap.put(pid + "_" + langCd, productInfo);
		}
		return townProductInfoMap;
	}

	public Map<String, TownProdInfo> getCellIdTownNameDetails(String procName) throws IOException, ProcCallException  {
		Map<String, TownProdInfo> townCellIdTownNameMap = new HashMap<>();

		VoltTable[] productInfoTbl = voltDbClient.callProcedure(procName).getResults();
		while (productInfoTbl[0].advanceRow()) {
			TownProdInfo townProdInfo = new TownProdInfo();
			String cellId = productInfoTbl[0].getString(CELL_ID_KEY_NAME);
			townProdInfo.setCellId(cellId);
			townProdInfo.setTownName(productInfoTbl[0].getString(TOWN_NAME));
			townCellIdTownNameMap.put(cellId, townProdInfo);
		}
		return townCellIdTownNameMap;
	}

	public Map<String, List<String>> getTownProdIdDetails(String procName) throws IOException, ProcCallException {
		Map<String, List<String>> townProdInfoMap = new HashMap<>();

		VoltTable[] productInfoTbl = voltDbClient.callProcedure(procName).getResults();

		while (productInfoTbl[0].advanceRow()) {
			String prodId = productInfoTbl[0].getString(PRODUCT_ID);
			String townName = productInfoTbl[0].getString(TOWN_NAME);

			if (townProdInfoMap.containsKey(townName)) {
				townProdInfoMap.get(townName).add(prodId);
			} else {
				List<String> productIdsList = new ArrayList<>();
				productIdsList.add(prodId);
				townProdInfoMap.put(townName, productIdsList);
			}
		}
		return townProdInfoMap;
	}

	public int getNewCustomerLangCode(String msisdn) throws Exception {
		int langCd = 0;
		String procedure = PropertiesLoader.getValue(USSD_GET_NEW_CUSTOMER_PROFILE_PROC);
		VoltTable[] voltTables = voltDbClient.callProcedure(procedure, msisdn).getResults();
		if (voltTables[0].getRowCount() > 0 && voltTables[0].advanceRow()) {
			langCd = (int) voltTables[0].get(LANG_CODE, VoltType.INTEGER);
		}
		return langCd;
	}
}