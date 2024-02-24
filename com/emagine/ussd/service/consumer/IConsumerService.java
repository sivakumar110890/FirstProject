package com.emagine.ussd.service.consumer;


public interface IConsumerService {

    /**
     * @return
     * @throws Exception
     */
    String startUssdConsumer() throws Exception;

    /**
     * @return
     * @throws Exception
     */
    String stopUssdConsumer() throws Exception;

}
