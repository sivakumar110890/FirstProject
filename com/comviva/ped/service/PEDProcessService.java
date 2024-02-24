package com.comviva.ped.service;

import static com.comviva.ped.model.PEDCONSTANT.NO_PLAY;
import static com.comviva.ped.model.PEDCONSTANT.NO_PRIZE;
import static com.comviva.ped.model.PEDCONSTANT.PED;
import static com.comviva.ped.model.PEDCONSTANT.PRIZE_TYPE_GSM;
import static com.comviva.ped.model.PEDCONSTANT.RANGE;
import static com.comviva.ped.model.PEDCONSTANT.SOURCE;
import static com.comviva.ped.model.PEDCONSTANT.STOP_PLAY;
import static com.emagine.ussd.utils.USSDConstants.FLAG_Y;
import static com.emagine.ussd.utils.USSDConstants.LANG_CODE;
import static com.emagine.ussd.utils.USSDConstants.ML_FLAG;
import static com.emagine.ussd.utils.USSDConstants.MSISDN;
import static com.emagine.ussd.utils.USSDConstants.OP_REWARDSPUB_TOPIC_NAME;
import static com.emagine.ussd.utils.USSDConstants.PRODUCT_ID;
import static com.emagine.ussd.utils.USSDConstants.PRODUCT_PRICE;
import static com.emagine.ussd.utils.USSDConstants.PROG_SHORT_CD;
import static com.emagine.ussd.utils.USSDConstants.REWARDSPUB_TOPIC_NAME;
import static com.emagine.ussd.utils.USSDConstants.SEL_PROD_TYPE;
import static com.emagine.ussd.utils.USSDConstants.TRANSACTION_ID;
import static com.emagine.ussd.utils.USSDConstants.USSD_ENABLE_MULTI_CS;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.voltdb.client.ProcCallException;

import com.comviva.ped.dao.PEDLookUPDAO;
import com.comviva.ped.dao.PEDUpdateDAO;
import com.comviva.ped.model.PrizeLibrary;
import com.comviva.ped.utils.PEDRandomPrizesCache;
import com.emagine.ussd.config.PropertiesLoader;
import com.emagine.ussd.dao.LookUpDAO;
import com.emagine.ussd.service.publisher.UssdEventPublisher;
import com.emagine.ussd.utils.USSDConstants;

/**
 * Implementation of PED module database service.
 * Business logic to handle the all PED process.
 * 
 * @author chandra.tekam
 *
 */
public class PEDProcessService implements IPEDProcessService {
    private static final Logger LOGGER = Logger.getLogger(PEDProcessService.class);
    private PEDLookUPDAO pedLookUpDao;
    private PEDUpdateDAO pedUpdateDao;
    private LookUpDAO lookUpDAO;
    private SecureRandom random;

    public PEDProcessService() throws Exception {
        pedLookUpDao = new PEDLookUPDAO();
        pedUpdateDao = new PEDUpdateDAO();
        lookUpDAO = new LookUpDAO();
        random = new SecureRandom();
    }

    @Override
    public int getAvailablePlays(String msisdn) throws Exception {
        LOGGER.debug("getAvailablePlays=> " + msisdn);
        // get the play expiary count
        // call the global variable PLAY_EXPIRY_DAYS’
        int playCount = 0;
        if (this.getPlayExpiryDays() > 0) {
            playCount = pedLookUpDao.getAvailablePlaysHistory(msisdn) + pedLookUpDao.getAvailablePlaysDOD1(msisdn);
        } else {
            playCount = pedLookUpDao.getAvailablePlaysDOD1(msisdn);
        }

        int playedCount = pedLookUpDao.getMsisdnPlayedCount(msisdn);
        LOGGER.debug("getAvailablePlays=>playCount - playedCount => " + playCount + "-" + playedCount);
        return playCount - playedCount;
    }

    @Override
    public int getPlayExpiryDays() throws Exception {
    	int playExpiryDays = pedLookUpDao.getPedPlaysExpirayDays();
        LOGGER.debug("getPlayExpiryDays => " + playExpiryDays);
        return playExpiryDays;

    }

    @Override
    public List<String> getPrizeHistory(String msisdn) throws Exception {
        LOGGER.debug("getPrizeHistory=> " + msisdn);
        return pedLookUpDao.getPrizeHistory(msisdn);
    }

    @Override
    public String processPlay(String msisdn, int languageCode, String txnId) {
        LOGGER.debug("START processPlay=> msisdn => " + msisdn + " txnId=> " + txnId);
        String prizeId = NO_PRIZE;
        String prizeDescription = "";
        String status = "";
        PrizeLibrary prizeLibrary = null;
        try {
            // Execute Play Routine & determine Prize / no prize
            LOGGER.info("request getAvailablePlays =>");
            int availablePlays = getAvailablePlays(msisdn);
            LOGGER.info("verify  availablePlays =>" + availablePlays);
            if (availablePlays > 0) {
            	prizeLibrary = executePlayRoutine(languageCode);
            	if (!NO_PRIZE.equalsIgnoreCase(prizeLibrary.getPrizeId())) {
            		prizeDescription = prizeLibrary.getPrizeDescription();
            		prizeId = prizeLibrary.getPrizeId();
            		status = "Redeemed";
            		// increase prize record count
            		increasePrizeRecordCount(prizeLibrary.getPrizeId());
            		// record rewarded Play into play history
            		recordRewardedPlay(msisdn, prizeLibrary, status);
            		// sent prize
            		// provision the reward if prize
            		LOGGER.debug("prizeLibrary.getPrizeType()=>" + prizeLibrary.getPrizeType());
            		if (PRIZE_TYPE_GSM.equalsIgnoreCase(prizeLibrary.getPrizeType())) {
            			JSONObject message = new JSONObject();
            			message.put(MSISDN, msisdn);
            			message.put(TRANSACTION_ID, txnId);
            			message.put(PRODUCT_ID, prizeLibrary.getPrizeId());
            			message.put(PROG_SHORT_CD, "1");
            			message.put(LANG_CODE, languageCode);
            			message.put(ML_FLAG, FLAG_Y);
            			message.put(SEL_PROD_TYPE, prizeLibrary.getPrizeType());
            			message.put(PRODUCT_PRICE, 0L);
            			message.put(SOURCE, PED);

            			if (PropertiesLoader.getIntValue(USSD_ENABLE_MULTI_CS) == 1) {
            				String csType = lookUpDAO.getCSType(msisdn);
            				if (csType.equalsIgnoreCase(USSDConstants.CS_TYPE_OP)) {
            					publishToTopic(PropertiesLoader.getValue(OP_REWARDSPUB_TOPIC_NAME), message.toString());
            					LOGGER.info("Published message on Openet OP_rewards topic for Ml user :: " + message);
            				}else {
            					publishToTopic(PropertiesLoader.getValue(REWARDSPUB_TOPIC_NAME), message.toString());
            					LOGGER.info("Published message on rewards topic for Ml user :: " + message);
            				}
            			}else {	
            				publishToTopic(PropertiesLoader.getValue(REWARDSPUB_TOPIC_NAME), message.toString());
            				LOGGER.info("Published message on rewards topic for Ml user :: " + message);
            			}
            		}
            	} 
            	// increase played count of msisdn
            	increasePlayedCountOfMsisdn(msisdn);
            } else {
                prizeId = NO_PLAY;
            }
            if ("".equals(prizeDescription)) {
                recordRewardedPlayEcap(msisdn, prizeId, prizeId, txnId);
            } else {
                recordRewardedPlayEcap(msisdn, prizeId, status, txnId);
                prizeId = prizeDescription;
            }

        } catch (Exception e) {
            LOGGER.error("processPlay service error=>", e);
        }
        LOGGER.info("End processPlay => prizeId =>" + prizeId);
        return prizeId;
    }

    /**
     * Sample play till it gets prize or No prize
     * 
     * @return
     * @throws Exception
     */
    private PrizeLibrary executePlayRoutine(int langCode) throws Exception {
        LOGGER.debug("executePlayRoutine => ");
        return getRandomPrizeByRange(langCode);
    }

    /**
     * @param langCode
     * @return
     * @throws Exception
     */
    public PrizeLibrary getRandomPrizeByRange(int langCode) throws Exception {

        int randomNum = random.nextInt(RANGE);

        LOGGER.debug("getRandomPrizeByRange => randomNum=>" + randomNum);
        // step2 => 1 offer randomly selected as per probability weightings
        PEDRandomPrizesCache pedRandomPrizesCache = PEDRandomPrizesCache.instance() ;
        String prizeId = pedRandomPrizesCache.getRandomPrize(randomNum);
        LOGGER.debug("getRandomPrizeByRange => randomNum=>" + randomNum + " prizeId=>" + prizeId);

        PrizeLibrary prizeLibrary = new PrizeLibrary();
        if (null != prizeId && !NO_PRIZE.equalsIgnoreCase(prizeId)) {
            prizeLibrary = getPrizeDetails(prizeId, langCode);
            int totalPrizeCount = getTotalPrizeCount(prizeId);
            // Check if max number of wins have been reached
            LOGGER.debug("Check if max number of wins have been reached totalPrizeCount=>" + totalPrizeCount + "=> max wins =>" + prizeLibrary.getMaxWins());
            if (totalPrizeCount >= prizeLibrary.getMaxWins()) {
                prizeLibrary.setPrizeId(NO_PRIZE);
            }
        } else {
            prizeLibrary.setPrizeId(NO_PRIZE);
        }

        return prizeLibrary;
    }

    /**
     * increase prize record count in ECMP_T_PED_PRIZE_STATS
     * @param prizeId
     */
    public void increasePrizeRecordCount(String prizeId) {
        LOGGER.debug("increasePrizeRecordCount => ");
        try {
            int count = pedLookUpDao.retrieveTotalPrizeCount(prizeId);
            pedUpdateDao.upsertPrizeRewardCount(count + 1, prizeId);

        } catch (IOException | ProcCallException e) {
            LOGGER.error("Error increasePrizeRecordCount => ", e);
        } catch (Exception e) {
            LOGGER.error("Exception increasePrizeRecordCount => ", e);
        }

    }

    /**
     * increase prize record count
     * 
     * @throws Exception
     */
    private void increasePlayedCountOfMsisdn(String msisdn) throws Exception {
        LOGGER.debug("increasePlayedCountOfMsisdn => ");
        int playedCount = pedLookUpDao.getMsisdnPlayedCount(msisdn);
        pedUpdateDao.upsertPedPlayedCountByMsisdn(msisdn, playedCount + 1);

    }

    /**
     * @param msisdn
     * @param prizeId
     * @param status
     * @param txnId
     * @throws Exception
     */
    public void recordRewardedPlayEcap(String msisdn, String prizeId, String status, String txnId) throws Exception {
        LOGGER.debug("recordRewardedPlayEcap => ");
        // insert into ECMP_T_PLAY_HISTORY_ECAP
        pedUpdateDao.insertIntoPlayHistoryEcap(msisdn, prizeId, status, txnId);

    }

    /**
     * record rewarded Play
     * ECMP_T_PLAY_HISTORY and ECMP_T_PLAY_HISTORY_ECAP.
     * 
     * @param msisdn
     * @param prizeLibrary
     * @param status
     * @throws Exception
     */
    public void recordRewardedPlay(String msisdn, PrizeLibrary prizeLibrary, String status) throws Exception {
        LOGGER.debug("recordRewardedPlay => ");
        // insert into ECMP_T_PLAY_HISTORY
        pedUpdateDao.insertIntoPlayHistory(msisdn, prizeLibrary.getPrizeDescription(), status);
    }

    /**
     *
     */
    @Override
    public boolean checkPedProcessPlayFlag() throws Exception {
        LOGGER.debug("checkPedProcessPlayFlag => ");
        return pedLookUpDao.getPedProcessFlag(STOP_PLAY);

    }

    /**
     * @param prizeId
     * @return
     * @throws Exception
     */
    public int getTotalPrizeCount(String prizeId) throws Exception {
        LOGGER.debug("getTotalPrizeCount => ");
        return pedLookUpDao.retrieveTotalPrizeCount(prizeId);
    }

    /**
     * @param prizeId
     * @param langCode
     * @return
     * @throws Exception
     */
    public PrizeLibrary getPrizeDetails(String prizeId, int langCode) throws Exception {
        LOGGER.debug("getPrizeDetails => ");
        return pedLookUpDao.retrievePrizeDetailsById(prizeId, langCode);
    }

    private void publishToTopic(String topicName, String message) throws Exception {
        UssdEventPublisher eventPublisher = new UssdEventPublisher();
        eventPublisher.addEvent(topicName, message);
    }
}
