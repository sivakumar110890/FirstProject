package com.emagine.ussd.service;


public interface IUssdServcie {

    /**
     * @param message
     * @throws Exception
     */
    void processQueryBalanceRequest(String message) throws Exception;

}
