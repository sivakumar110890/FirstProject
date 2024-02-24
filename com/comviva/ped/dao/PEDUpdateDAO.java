package com.comviva.ped.dao;

import static com.comviva.ped.model.PEDCONSTANT.PED_P_PLAYED_COUNT_HISTORY_UPSERT;
import static com.comviva.ped.model.PEDCONSTANT.PED_P_PLAY_HISTORY_ECAP_INSERT;
import static com.comviva.ped.model.PEDCONSTANT.PED_P_PLAY_HISTORY_INSERT;
import static com.comviva.ped.model.PEDCONSTANT.PED_P_PRIZE_STATS_UPSERT;

import org.apache.log4j.Logger;
import org.voltdb.client.Client;

import com.comviva.voltdb.factory.DAOFactory;
import com.emagine.ussd.config.PropertiesLoader;
import com.emagine.ussd.dao.USSDDAOCallback;

/**
 * PED insert and update data base handling
 *
 * @author chandra.tekam
 */
public class PEDUpdateDAO {
    private static final Logger LOG = Logger.getLogger(PEDUpdateDAO.class);
    private Client voltDbClient = null;

    public PEDUpdateDAO() throws Exception {
        voltDbClient = DAOFactory.getClient();

    }

    private void executeProc(final String sql, final Object... args) throws Exception {
        voltDbClient.callProcedure(new USSDDAOCallback(), sql, args);
    }

    public void insertIntoPlayHistory(String msisdn, String prizeId, String status) throws Exception {
        String procudreName = PropertiesLoader.getValue(PED_P_PLAY_HISTORY_INSERT);
        Object[] args = new Object[] { msisdn, prizeId, status, msisdn };
        executeProc(procudreName, args);

    }

    public void insertIntoPlayHistoryEcap(String msisdn, String prizeId, String status, String txnId) throws Exception {
        String procudreName = PropertiesLoader.getValue(PED_P_PLAY_HISTORY_ECAP_INSERT);
        LOG.debug("pocedure name= > " + procudreName + " msisdn=> " + msisdn + " prizeId=>  " + prizeId + "" + " status= > " + status + " txnId => " + txnId);
        Object[] args = new Object[] { msisdn, prizeId, status, txnId };
        executeProc(procudreName, args);

    }

    public void upsertPrizeRewardCount(int prizeCount, String prizeId) throws Exception {
        String procudreName = PropertiesLoader.getValue(PED_P_PRIZE_STATS_UPSERT);
        Object[] args = new Object[] { prizeId, prizeCount };
        executeProc(procudreName, args);

    }

    // PED_P_PLAYED_COUNT_HISTORY_UPSERT
    public void upsertPedPlayedCountByMsisdn(String msisdn, int playedCount) throws Exception {
        String procudreName = PropertiesLoader.getValue(PED_P_PLAYED_COUNT_HISTORY_UPSERT);
        Object[] args = new Object[] { msisdn, playedCount };
        executeProc(procudreName, args);

    }

}
