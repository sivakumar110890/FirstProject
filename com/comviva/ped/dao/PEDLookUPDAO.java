package com.comviva.ped.dao;

import static com.comviva.ped.model.PEDCONSTANT.ERED_PLAY_ALLOCATION_HISTORY_SELECT;
import static com.comviva.ped.model.PEDCONSTANT.PED_P_AVAILABLE_PLAYS_DOD1_SELECT;
import static com.comviva.ped.model.PEDCONSTANT.PED_P_GLOBAL_VARIABLES_SELECT;
import static com.comviva.ped.model.PEDCONSTANT.PED_P_PLAYED_COUNT_HISTORY_SELECT;
import static com.comviva.ped.model.PEDCONSTANT.PED_P_PLAY_HISTORY_SELECT;
import static com.comviva.ped.model.PEDCONSTANT.PED_P_PRIZE_LIBRARY_SELECT;
import static com.comviva.ped.model.PEDCONSTANT.PED_P_PRIZE_STATS_SELECT;
import static com.comviva.ped.model.PEDCONSTANT.PED_P_RANDOM_PRIZE_SELECT;
import static com.comviva.ped.model.PEDCONSTANT.PLAY_EXPIRY_DAYS;
import static com.comviva.ped.model.PEDCONSTANT.STOP_PLAY_FLAG_T;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.client.Client;
import org.voltdb.client.ProcCallException;

import com.comviva.ped.model.PrizeLibrary;
import com.comviva.voltdb.factory.DAOFactory;
import com.emagine.ussd.config.PropertiesLoader;
import com.emagine.ussd.model.PEDRandomPrizeInfo;

public class PEDLookUPDAO {
    private static final Logger LOGGER = Logger.getLogger(PEDLookUPDAO.class) ;
    private static final String MIN_RANGE = "MIN_RANGE";
    private static final String MAX_RANGE = "MAX_RANGE";
    private static final String PRIZE_ID = "PRIZE_ID";
    private static final String WIEGHTAGE = "WIEGHTAGE";

    private Client voltDbClient = null;

    public PEDLookUPDAO() throws Exception {
        voltDbClient = DAOFactory.getClient();

    }

    public int getAvailablePlaysHistory(String msisdn) throws Exception {
        int noOfPlays = 0;
        VoltTable[] voltTable = voltDbClient.callProcedure(PropertiesLoader.getValue(ERED_PLAY_ALLOCATION_HISTORY_SELECT), msisdn).getResults();
        if (voltTable[0].advanceRow()) {
            noOfPlays = (int) voltTable[0].get(0, VoltType.INTEGER);
        }
        return noOfPlays;
    }

    public List<String> getPrizeHistory(String msisdn) throws Exception {
        List<String> prizeHistoryList = new ArrayList<>();
        String prizeId = null;
        VoltTable[] voltTable = voltDbClient.callProcedure(PropertiesLoader.getValue(PED_P_PLAY_HISTORY_SELECT), msisdn).getResults();
        while (voltTable[0].advanceRow()) {
            prizeId = (String) voltTable[0].get(0, VoltType.STRING);
            prizeHistoryList.add(prizeId);
        }
        return prizeHistoryList;
    }

    public boolean getPedProcessFlag(String name) throws Exception {
        String value = "";
        VoltTable[] voltTable = voltDbClient.callProcedure(PropertiesLoader.getValue(PED_P_GLOBAL_VARIABLES_SELECT), name).getResults();
        if (voltTable[0].advanceRow()) {
            value = (String) voltTable[0].get(0, VoltType.STRING);
        }
        return STOP_PLAY_FLAG_T.equalsIgnoreCase(value);
    }

    public int getPedPlaysExpirayDays() throws Exception {
        String value = "0";
        VoltTable[] voltTable = voltDbClient.callProcedure(PropertiesLoader.getValue(PED_P_GLOBAL_VARIABLES_SELECT), PLAY_EXPIRY_DAYS).getResults();
        if (voltTable[0].advanceRow()) {
            value = (String) voltTable[0].get(0, VoltType.STRING);
        }
        return Integer.parseInt(value);
    }

    public List<PEDRandomPrizeInfo> getRandomPrize() throws Exception {
        LOGGER.info("getRandomPrize - START");
        List<PEDRandomPrizeInfo> pedRandomPrizeInfoList = new ArrayList<>() ;
        PEDRandomPrizeInfo pedPriceInfo ;
        VoltTable[] voltTable = voltDbClient.callProcedure(PropertiesLoader.getValue(PED_P_RANDOM_PRIZE_SELECT)).getResults();
        while (voltTable[0].advanceRow()) {
            pedPriceInfo = new PEDRandomPrizeInfo () ;
            pedPriceInfo.setPrizeID(voltTable[0].getString(PRIZE_ID));
            pedPriceInfo.setMaxRange((Integer) voltTable[0].get(MAX_RANGE, VoltType.INTEGER));
            pedPriceInfo.setMinRange((Integer) voltTable[0].get(MIN_RANGE, VoltType.INTEGER));
            pedPriceInfo.setWeightage((Integer) voltTable[0].get(WIEGHTAGE, VoltType.INTEGER));
            pedRandomPrizeInfoList.add(pedPriceInfo) ;
        }
        LOGGER.info("getRandomPrize - END - Total Records - " + pedRandomPrizeInfoList.size());
        return pedRandomPrizeInfoList ;
    }

    public int retrieveTotalPrizeCount(String prizeId) throws Exception {
        int value = 0;
        VoltTable[] voltTable = voltDbClient.callProcedure(PropertiesLoader.getValue(PED_P_PRIZE_STATS_SELECT), prizeId).getResults();
        if (voltTable[0].advanceRow()) {
            value = (int) voltTable[0].get(0, VoltType.INTEGER);
        }
        return value;
    }

    public PrizeLibrary retrievePrizeDetailsById(String prizeId, int languageCode) throws Exception {
        PrizeLibrary prizeLibrary = null;

        VoltTable[] voltTable = voltDbClient.callProcedure(PropertiesLoader.getValue(PED_P_PRIZE_LIBRARY_SELECT), prizeId, languageCode).getResults();
        if (voltTable[0].advanceRow()) {
            prizeLibrary = new PrizeLibrary();
            prizeLibrary.setPrizeDescription(voltTable[0].getString("PRIZE_DESC"));
            prizeLibrary.setProbability((int) voltTable[0].get("PROBABILITY", VoltType.INTEGER));
            prizeLibrary.setPrizeId(prizeId);
            prizeLibrary.setLanguageCode(languageCode);
            prizeLibrary.setMaxWins((int) voltTable[0].get("MAX_WINS", VoltType.INTEGER));
            prizeLibrary.setPrizeType(voltTable[0].getString("PRIZE_TYPE"));
            prizeLibrary.setRedemptionCode(voltTable[0].getString("REDEMPTION_CODE"));
        }
        return prizeLibrary;
    }

    public int getAvailablePlaysDOD1(String msisdn) throws Exception {
        int noOfPlays = 0;
        VoltTable[] voltTable = voltDbClient.callProcedure(PropertiesLoader.getValue(PED_P_AVAILABLE_PLAYS_DOD1_SELECT), msisdn).getResults();
        if (voltTable[0].advanceRow()) {
            noOfPlays = (int) voltTable[0].get(0, VoltType.INTEGER);
        }
        return noOfPlays;

    }

    public int getMsisdnPlayedCount(String msisdn) throws IOException, ProcCallException {
        int noOfPlayedCount = 0;
        VoltTable[] voltTable = voltDbClient.callProcedure(PED_P_PLAYED_COUNT_HISTORY_SELECT, msisdn).getResults();
        if (voltTable[0].advanceRow()) {
            noOfPlayedCount = (int) voltTable[0].get(0, VoltType.INTEGER);
        }
        return noOfPlayedCount;

    }

}
