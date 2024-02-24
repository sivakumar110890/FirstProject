package com.comviva.ped.service;

import java.util.List;


/**
 * Service to handle the ped module
 *
 * @author chandra.tekam
 */
public interface IPEDProcessService {
    /**
     * @param msisdn
     * @return
     * @throws Exception
     */
    int getAvailablePlays(String msisdn) throws Exception ;

    /**
     * @param msisdn
     * @return
     * @throws Exception
     */
    List<String> getPrizeHistory(String msisdn) throws Exception;

    /**
     * @return
     * @throws Exception
     */
    boolean checkPedProcessPlayFlag() throws Exception;

    /**
     * @param msisdn
     * @param languageCode
     * @param txnId
     * @return
     */
    String processPlay(String msisdn, int languageCode, String txnId);

    /**
     * @return
     * @throws Exception
     */
    int getPlayExpiryDays() throws Exception;
}
