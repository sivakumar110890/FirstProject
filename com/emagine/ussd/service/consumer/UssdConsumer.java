package com.emagine.ussd.service.consumer;

import org.apache.log4j.Logger;

import com.emagine.kafka.consumer.EventConsumer;
import com.emagine.ussd.service.IUssdServcie;
import com.emagine.ussd.service.UssdService;

/**
 * Topic subscriber class listens to topic and processes the messages.
 */
public class UssdConsumer extends EventConsumer {

    private static final Logger LOGGER = Logger.getLogger(UssdConsumer.class);

    /**
     * Instantiates a new Ussd Consumer.
     *
     * @param groupId
     *            the group id
     * @param topic
     *            the topic
     * @param threads
     *            the threads
     */
    public UssdConsumer(String groupId, String topic, int threads) {
        super(groupId, topic, threads);
    }

    @Override
    public void onReceive(String message, int threadId) {
        LOGGER.info("onRecieve method :: [Start] - [Thread Id]=[" + threadId + "] Message ==> " + message);
        try {
            /*
             * Remove [ "" ], Because while Volt pushing the msg to kafka it
             * adds "" internally.
             */
            if (message.startsWith("\"")) {
                message = message.subSequence(1, message.length() - 1).toString();
                message = message.replace("\"\"", "\"");
            }
            IUssdServcie ussdServcie = new UssdService();
            ussdServcie.processQueryBalanceRequest(message);
            LOGGER.info("onRecieve method :: [END] - [Thread Id]=[" + threadId + "]");
        } catch (Exception e) {
            LOGGER.error("onRecieve method :: [ERROR OCCURED]  thread Id =[" + threadId + "] :: Message = ", e);
        }
    }
}
