package com.emagine.ussd.service.consumer;

import static com.emagine.ussd.utils.USSDConstants.ONE;
import static com.emagine.ussd.utils.USSDConstants.SUCCESS;
import static com.emagine.ussd.utils.USSDConstants.TWO;
import static com.emagine.ussd.utils.USSDConstants.USSD_CONSUMER_TOPIC;
import static com.emagine.ussd.utils.USSDConstants.ZERO;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import com.emagine.ussd.config.PropertiesLoader;
import com.emagine.ussd.exception.USSDPluginException;

/**
 * @author shaju.kakkara
 *
 */
@Component
public class ConsumerService implements IConsumerService {
    private static final Logger LOGGER = Logger.getLogger(ConsumerService.class);

    private static List<UssdConsumer> ussdConsumerList = new ArrayList<>();

    public String startUssdConsumer() throws Exception {
        try {
            LOGGER.info("startUssdConsumer method :: [Start]");
            String queryMgrTopic = PropertiesLoader.getValue(USSD_CONSUMER_TOPIC);
            String[] topicList = queryMgrTopic.split("~");
            for (int i = 0; i < topicList.length; i++) {
                String topicArray = topicList[i];
                String[] topicDetails = topicArray.split(":");
                String topic = topicDetails[ZERO];
                String groupId = topicDetails[ONE];
                int threadCount = Integer.parseInt(topicDetails[TWO]);
                UssdConsumer ussdConsumer = new UssdConsumer(groupId, topic, threadCount);
                ussdConsumer.startConsumer();
                ussdConsumerList.add(ussdConsumer);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("Config Exception -", ex);
            throw new USSDPluginException("INCORRECT Configuration in ussd.properties for ussd.consumer.topic");
        }
        return SUCCESS;
    }

    public String stopUssdConsumer() throws Exception {
        LOGGER.info("stopUssdConsumer method :: [Start]");
        boolean isConsumerNull = false;
        if (ussdConsumerList != null) {
            for (UssdConsumer ussdConsumer : ussdConsumerList) {
                if (ussdConsumer != null) {
                    ussdConsumer.shutdownAllConsumers();
                } else {
                    isConsumerNull = true;
                }
            }
            if (isConsumerNull) {
                forceStopAllConsumers();
            }
        } else {
            forceStopAllConsumers();
        }
        LOGGER.info("stopUssdConsumer method :: [End]");
        return SUCCESS;
    }

    /**
     * Force Stop All Kafka Consumer Threads
     * @throws Exception
     */
    public void forceStopAllConsumers() throws Exception {
        try {
            /*
             * Stop all consumers as we are not sure which one has been garbage
             * collected.
             */
            String workflowManagerTopicList = PropertiesLoader.getValue(USSD_CONSUMER_TOPIC);
            String[] topicList = workflowManagerTopicList.split("~");
            for (int i = 0; i < topicList.length; i++) {
                String topicArray = topicList[i];
                String[] topicDetails = topicArray.split(":");
                String topic = topicDetails[ZERO];
                String groupId = topicDetails[ONE];
                int threadCount = Integer.getInteger(topicDetails[TWO]).intValue();
                UssdConsumer ussdConsumer = new UssdConsumer(groupId, topic, threadCount);
                ussdConsumer.shutdownAllConsumers();
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            LOGGER.error("Config Exception -", ex);
            throw new USSDPluginException ("INCORRECT Configuration in ussd.properties for ussd.consumer.topic");
        }
    }

}
