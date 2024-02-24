package com.emagine.ussd.dao;

import static com.emagine.ussd.utils.USSDConstants.FLAG_N;
import static com.emagine.ussd.utils.USSDConstants.FLAG_O;
import static com.emagine.ussd.utils.USSDConstants.FLAG_Y;
import static com.emagine.ussd.utils.USSDConstants.INSERT_RAG_USER_CAT_PROC_NAME;
import static com.emagine.ussd.utils.USSDConstants.INSERT_SAG_USER_CAT_PROC_NAME;
import static com.emagine.ussd.utils.USSDConstants.MSISDN;
import static com.emagine.ussd.utils.USSDConstants.NEXT_AVAILABLE_OFFER_DATE;
import static com.emagine.ussd.utils.USSDConstants.PAYMENT_METHOD;
import static com.emagine.ussd.utils.USSDConstants.RECHARGE_TARGET;
import static com.emagine.ussd.utils.USSDConstants.REWARD_CODE;
import static com.emagine.ussd.utils.USSDConstants.SPEND_TARGET;
import static com.emagine.ussd.utils.USSDConstants.SUBID_CNTRYCD;
import static com.emagine.ussd.utils.USSDConstants.SUBID_LENGTH;
import static com.emagine.ussd.utils.USSDConstants.TARGET_TYPE;
import static com.emagine.ussd.utils.USSDConstants.USSD_LOG_PROC_NAME;
import static com.emagine.ussd.utils.USSDConstants.USSD_P_INSERT_ECMP_SAG_OPT_INFO;
import static com.emagine.ussd.utils.USSDConstants.USSD_P_INSERT_SAG_OPT_INFO;
import static com.emagine.ussd.utils.USSDConstants.USSD_P_ML_NEW_USER_RECORD_INSERT;
import static com.emagine.ussd.utils.USSDConstants.USSD_P_ML_SAG_USER_RECORD_INSERT;
import static com.emagine.ussd.utils.USSDConstants.USSD_P_UPDATE_J4U_CONSENT_STATUS;
import static com.emagine.ussd.utils.USSDConstants.USSD_P_UPDATE_SAG_OPT_INFO;
import static com.emagine.ussd.utils.USSDConstants.USSD_P_UPSERT_J4U_CONSENT_STATUS;
import static com.emagine.ussd.utils.USSDConstants.USSD_TRXPRODIDMAP_DELETE_NAME;
import static com.emagine.ussd.utils.USSDConstants.USSD_TRX_PRODID_MAP_NAME;
import static com.emagine.ussd.utils.USSDConstants.WEEK_END_DATE;
import static com.emagine.ussd.utils.USSDConstants.WEEK_START_DATE;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import org.apache.log4j.Logger;
import org.voltdb.client.Client;
import org.voltdb.client.ProcCallException;
import com.comviva.voltdb.factory.DAOFactory;
import com.emagine.ussd.config.PropertiesLoader;
import com.emagine.ussd.model.J4UNewCustomerProfile;
import com.emagine.ussd.model.MessageDTO;
import com.emagine.ussd.model.UserInfo;
import com.emagine.ussd.utils.Utils;

/**
 * DAO class for all CUD operations of USSDPlugin
 */
public class UpdaterDAO {
    private static final Logger LOG = Logger.getLogger(UpdaterDAO.class);
    private Client voltDbClient;

    public UpdaterDAO() throws Exception {
        voltDbClient = DAOFactory.getInsertClient();
    }

    /**
     * inserts a log record into ECMP_T_USSD_LOG table
     *
     * @param args
     * @throws Exception
     */
    public void insertLog(final Object[] args) throws Exception {
        String sql = PropertiesLoader.getValue(USSD_LOG_PROC_NAME);
        executeProc(sql, args);
    }

    /**
     * @param msgDTO
     * @throws Exception
     */
    public void insertTrxProdIdMap(final MessageDTO msgDTO) throws Exception {
        String sql = PropertiesLoader.getValue(USSD_TRX_PRODID_MAP_NAME);
        String[] offerSeq = msgDTO.getOfferSequence();
        String pid1 = msgDTO.getProducts().get(offerSeq[0]);
        String pid2 = msgDTO.getProducts().get(offerSeq[1]);
        String pid3 = msgDTO.getProducts().get(offerSeq[2]);
        Object[] args = new Object[] { msgDTO.getUssdMsgId(), msgDTO.getTxnId(), msgDTO.getMsisdn(), pid1, pid2, pid3, null, null, Utils.getCurrentTimeStamp(), msgDTO.getNbaDateTime() };
        executeProc(sql, args);
    }

    public void insertTrxProdIdMap(UserInfo userInfo, String pid1, String pid2, String pid3) throws Exception {
        String sql = PropertiesLoader.getValue(USSD_TRX_PRODID_MAP_NAME);
        Date currentDate = Utils.getCurrentTimeStamp();
        Object[] args = new Object[] { userInfo.getUserMsgRef(), userInfo.getTxId(), userInfo.getMsisdn(), pid1, pid2, pid3, null, null, currentDate, currentDate };
        executeProc(sql, args);
    }
    
    public void insertTrxProdIdMap5(UserInfo userInfo, String pid1, String pid2, String pid3, String pid4, String pid5) throws Exception {
        String sql = PropertiesLoader.getValue(USSD_TRX_PRODID_MAP_NAME);
        Date currentDate = Utils.getCurrentTimeStamp();
        Object[] args = new Object[] { userInfo.getUserMsgRef(), userInfo.getTxId(), userInfo.getMsisdn(), pid1, pid2, pid3, pid4, pid5, currentDate, currentDate };
        executeProc(sql, args);
    }

    public void upsertMLOfferMsg(UserInfo userInfo, String subMenuType, String prodIds, String prodRfs) throws IOException {
        String insertSql = "USSD_P_ML_OFFER_MSG_UPSERT";
        Object[] args = new Object[] { userInfo.getMsisdn(), subMenuType, prodIds, userInfo.getMessageBody(), Utils.getCurrentTimeStamp(), prodRfs };
        executeProc(insertSql, args);
    }

    public void deleteAllSessionData() throws IOException, ProcCallException {
        String deleteSql = "DELETE FROM ECMP_T_USSD_TRX_PRODID_MAP";
        voltDbClient.callProcedure("@AdHoc", deleteSql);
    }

    public void deleteRecordForSession(String msisdn, int sessionId) throws Exception {
        String deleteSql = PropertiesLoader.getValue(USSD_TRXPRODIDMAP_DELETE_NAME);
        Object[] args = new Object[] { msisdn, sessionId };
        voltDbClient.callProcedure(deleteSql, args);
    }

    private void executeProc(final String sql, final Object... args) throws IOException {
        voltDbClient.callProcedure(new USSDDAOCallback(), sql, args);
    }

    public void updateReducedCCR(String offerRefFlag, String subscriberId) throws Exception {
        String procName = "ECMP_P_UPDATE_REFRESH_FLAG";

        if (subscriberId.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
            subscriberId = subscriberId.replaceFirst(PropertiesLoader.getValue(SUBID_CNTRYCD), "");
        }
        voltDbClient.callProcedure(new USSDDAOCallback(), procName, offerRefFlag, subscriberId);
    }

    public void updateOptInfo(UserInfo userInfo) throws Exception {
        String msisdn = userInfo.getMsisdn();
        if (msisdn.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
            msisdn = msisdn.replaceFirst(PropertiesLoader.getValue(SUBID_CNTRYCD), "");
        }

        LOG.debug("updateOptInfo :: isRagNeverOptInFlag = " + userInfo.isRagNeverOptInFlag());

        if (userInfo.isRagNeverOptInFlag()) {
            String procName = "USSD_P_INSERT_RAG_OPT_INFO";
            String optInfo = userInfo.isRagOptInFlag() ? "Y" : "N";
            voltDbClient.callProcedure(new USSDDAOCallback(), procName, msisdn, userInfo.getRagInfo().get(RECHARGE_TARGET), userInfo.getRagInfo().get(RECHARGE_TARGET), optInfo);
            String procedure = "USSD_P_INSERT_ECMP_OPT_INFO";
            voltDbClient.callProcedure(new USSDDAOCallback(), procedure, msisdn, userInfo.getTxId(), optInfo);

        } else {
            String procName = "USSD_P_UPDATE_RAG_OPT_INFO";
            String optInfo = userInfo.isRagOptInFlag() ? "Y" : "O";
            voltDbClient.callProcedure(new USSDDAOCallback(), procName, optInfo, msisdn);

            String procedure = "USSD_P_INSERT_ECMP_OPT_INFO";
            voltDbClient.callProcedure(new USSDDAOCallback(), procedure, msisdn, userInfo.getTxId(), optInfo);
        }

    }
    
    public void updateSAGOptInfo(UserInfo userInfo) throws Exception {
        String msisdn = userInfo.getMsisdn();
        if (msisdn.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
        	msisdn = Utils.getMsisdnWithoutCcode(msisdn);
        }

        LOG.debug("updateOptInfo :: isSagNeverOptInFlag = " + userInfo.isSagNeverOptInFlag());

        if (userInfo.isSagNeverOptInFlag()) {
            String procName = PropertiesLoader.getValue(USSD_P_INSERT_SAG_OPT_INFO);
            String optInfo = userInfo.isSagOptInFlag() ? FLAG_Y : FLAG_N;
            voltDbClient.callProcedure(new USSDDAOCallback(), procName, msisdn, userInfo.getSagInfo().get(SPEND_TARGET), userInfo.getSagInfo().get(SPEND_TARGET), optInfo);
            String procedure =PropertiesLoader.getValue(USSD_P_INSERT_ECMP_SAG_OPT_INFO);
            voltDbClient.callProcedure(new USSDDAOCallback(), procedure, msisdn, userInfo.getTxId(), optInfo);

        } else {
            String procName = PropertiesLoader.getValue(USSD_P_UPDATE_SAG_OPT_INFO);
            String optInfo = userInfo.isSagOptInFlag() ? FLAG_Y : FLAG_O;
            voltDbClient.callProcedure(new USSDDAOCallback(), procName, optInfo, msisdn);

            String procedure = PropertiesLoader.getValue(USSD_P_INSERT_ECMP_SAG_OPT_INFO);
            voltDbClient.callProcedure(new USSDDAOCallback(), procedure, msisdn, userInfo.getTxId(), optInfo);
        }

    }
    

    public void insertRagUserRecordDB(Map<String, String> ragUserRecord) {
        LOG.debug("insertRagUserRecordDB started");
        try {
            String procedure = "USSD_P_ML_RAG_USER_RECORD_INSERT";
            Object[] args = new Object[] { ragUserRecord.get(MSISDN), ragUserRecord.get(MSISDN), ragUserRecord.get(WEEK_START_DATE), ragUserRecord.get(WEEK_END_DATE), ragUserRecord.get(NEXT_AVAILABLE_OFFER_DATE), ragUserRecord.get(REWARD_CODE), ragUserRecord.get(TARGET_TYPE), ragUserRecord.get(PAYMENT_METHOD), ragUserRecord.get(RECHARGE_TARGET), Utils.getCurrentTimeStamp() };
            executeProc(procedure, args);
        } catch (Exception e) {
            LOG.error("insertRagUserRecordDB Exception", e);
        }
        LOG.debug("insertRagUserRecordDB ended");
    }   
    

    /**
     * inserts new RAG user record into ECAP_RAG_CAT_FILE_LOAD_STG table
     *
     * @param args
     * @throws Exception
     */
    public void insertRagUserCatFile(Map<String, String> ragUserRecord, String msisdn) {
        try {
            String sql = PropertiesLoader.getValue(INSERT_RAG_USER_CAT_PROC_NAME);
            Object[] args = new Object[] { msisdn, msisdn, ragUserRecord.get(WEEK_START_DATE), ragUserRecord.get(WEEK_END_DATE), ragUserRecord.get(NEXT_AVAILABLE_OFFER_DATE), ragUserRecord.get(REWARD_CODE), ragUserRecord.get(TARGET_TYPE), ragUserRecord.get(PAYMENT_METHOD), ragUserRecord.get(RECHARGE_TARGET), Utils.getCurrentTimeStamp() };
            executeProc(sql, args);
        } catch (Exception e) {
            LOG.error("insertRagUserCatFile Exception", e);
        }

    }
    
    public void insertSagUserRecordDB(Map<String, String> sagUserRecord) {
        LOG.debug("insertSagUserRecordDB started");
        try {
            String procedure = PropertiesLoader.getValue(USSD_P_ML_SAG_USER_RECORD_INSERT);
            Object[] args = new Object[] { sagUserRecord.get(MSISDN), sagUserRecord.get(MSISDN), sagUserRecord.get(WEEK_START_DATE), sagUserRecord.get(WEEK_END_DATE), sagUserRecord.get(NEXT_AVAILABLE_OFFER_DATE), sagUserRecord.get(REWARD_CODE), sagUserRecord.get(PAYMENT_METHOD), sagUserRecord.get(SPEND_TARGET), Utils.getCurrentTimeStamp() };
            executeProc(procedure, args);
        } catch (Exception e) {
            LOG.error("insertSagUserRecordDB Exception", e);
        }
        LOG.debug("insertSagUserRecordDB ended");
    }
    
    
    /**
     * inserts new SAG user record into ECAP_SAG_CAT_FILE_LOAD_STG table
     *
     * @param args
     * @throws Exception
     */
    public void insertSagUserCatFile(Map<String, String> sagUserRecord, String msisdn) {
    	try {
    		String sql = PropertiesLoader.getValue(INSERT_SAG_USER_CAT_PROC_NAME);
    		Object[] args = new Object[] { msisdn, msisdn, sagUserRecord.get(WEEK_START_DATE), sagUserRecord.get(WEEK_END_DATE), sagUserRecord.get(NEXT_AVAILABLE_OFFER_DATE), sagUserRecord.get(REWARD_CODE), sagUserRecord.get(PAYMENT_METHOD), sagUserRecord.get(SPEND_TARGET), Utils.getCurrentTimeStamp() };
    		executeProc(sql, args);            
    	} catch (Exception e) {
    		LOG.error("insertSagUserCatFile Exception", e);
    	}
    	LOG.debug("insertSagUserCatFile done!");
    }
    
    
    public void upsertUserConsentStatusDB(String msisdn) {
    	LOG.debug("insertUserConsentStatusDB started");    	

    	try {
    		if (msisdn.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
    			msisdn = msisdn.replaceFirst(PropertiesLoader.getValue(SUBID_CNTRYCD), "");
    		}
    		String procedure = PropertiesLoader.getValue(USSD_P_UPSERT_J4U_CONSENT_STATUS);
    		Object[] args = new Object[] {msisdn,FLAG_Y};
    		executeProc(procedure, args);
    	} catch (Exception e) {
    		LOG.error("insertUserConsentStatusDB Exception", e);
    	}
    	LOG.debug("insertUserConsentStatusDB ended");
    }   

    public void updateUserOptOutConsentStatusDB(String msisdn) {
    	LOG.debug("updateUserOptOutConsentStatusDB started");    	

    	try {
    		if (msisdn.length() == PropertiesLoader.getIntValue(SUBID_LENGTH)) {
    			msisdn = msisdn.replaceFirst(PropertiesLoader.getValue(SUBID_CNTRYCD), "");
    		}
    		String procedure = PropertiesLoader.getValue(USSD_P_UPDATE_J4U_CONSENT_STATUS);
    		Object[] args = new Object[] {FLAG_N,msisdn};
    		executeProc(procedure, args);
    	} catch (Exception e) {
    		LOG.error("updateUserOptOutConsentStatusDB Exception", e);
    	}
    	LOG.debug("updateUserOptOutConsentStatusDB ended");
    }   

    /**
     * @param J4UNewCustomerProfile
     * @throws Exception
     */
    public void insertNewUserInfo(final J4UNewCustomerProfile newCust) throws Exception {
    	LOG.debug("Inserting new customer data in ECMP_T_J4U_NEW_CUSTOMER_PROFILE table:- "+ newCust.getMsisdn());
        String sql = PropertiesLoader.getValue(USSD_P_ML_NEW_USER_RECORD_INSERT);
        Object[] args = new Object[] {newCust.getMsisdn(), newCust.getLangCategory(), newCust.getLangCode(), newCust.getSubscriberState(), newCust.getPaymentMethod(), newCust.getOfferPaymentMethod(), newCust.getSubscriberStartDate(), newCust.getDate()};
        executeProc(sql, args);
    }
}
