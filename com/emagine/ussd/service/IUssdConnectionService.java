package com.emagine.ussd.service;

import com.emagine.ussd.connection.TransmitMessage;
import com.emagine.ussd.model.UserInfo;

public interface IUssdConnectionService {

    /**
     * @return
     * @throws Exception
     */
    String open() throws Exception;

    /**
     * @return
     * @throws Exception
     */
    String close() throws Exception;

    /**
     * @param userInfo
     * @throws Exception
     */
    void sendMesage(UserInfo userInfo) throws Exception;

    /**
     * @param message
     * @throws Exception
     */
	void sendMesage(TransmitMessage message) throws Exception;
	
}
