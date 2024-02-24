package com.emagine.ussd.service.publisher;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import com.emagine.ussd.model.TopicMessage;

public class UssdEventPublisher {

    private static final Logger LOGGER = Logger.getLogger(UssdEventPublisher.class);
    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String PROPERTY_FILE_PATH = USER_DIR + "/config/ussd.properties";
    private static Properties props = new Properties();
    private static Properties queueKafkaProps = new Properties();
    private static LinkedBlockingDeque<TopicMessage> kafkaBuffer;
    private static int bufferSize = 0;
    private static long queueWaitTime = 0;
    private static ExecutorService executor = null;
    private static boolean isRunnable = true;
    private static long publisherIdleTime;
    private static int kafkaCounterVar;

    @SuppressWarnings("rawtypes")
    private static Future futureThread;

    static {
        try {
            LOGGER.debug("ConfigFileLocation = " + System.getProperty("user.dir"));
            props.load(new FileInputStream(new File(PROPERTY_FILE_PATH)));

            // buffer size for linked blocking queue
            bufferSize = Integer.parseInt((String) props.get("kafka.publisher.queue.buffer.size"));
            queueWaitTime = Long.parseLong((String) props.get("kafka.publisher.queue.wait.time.in.ms"));

            kafkaBuffer = new LinkedBlockingDeque<>(bufferSize);
            kafkaCounterVar = Integer.parseInt((String) props.get("kafka.publisher.counter.variable"));
            publisherIdleTime = Long.parseLong((String) props.get("kafka.publisher.idle.time.in.min"));

            queueKafkaProps.put("bootstrap.servers", props.get("kafka.publisher.bootstrap.servers"));
            queueKafkaProps.put("batch.size", props.get("kafka.publisher.batch.size"));
            queueKafkaProps.put("linger.ms", props.get("kafka.publisher.linger.ms"));
            queueKafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            queueKafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        } catch (Exception e) {
            LOGGER.error("Error in static block. " + e.getMessage());
        }
    }

    public static void startPublisher() {
        try {
            isRunnable = true;
            executor = Executors.newFixedThreadPool(1);
            futureThread = executor.submit(() -> pollQueue());
            LOGGER.info("USSD publisher thread started. isRunnable = " + isRunnable);
        } catch (Exception e) {
            LOGGER.error("Error in starting the publisher. " + e.getMessage() + "::", e);
        }
    }

    public static void pollQueue() {
        int varCount = 0;
        LocalDateTime publisherOpenedTime = LocalDateTime.now();
        Producer<String, String> queueProducer = null;
        boolean isPublisherOpened = false;
        while (isRunnable) {
            try {
                if (!kafkaBuffer.isEmpty()) {
                    if (varCount == 0) {
                        queueProducer = new KafkaProducer<>(queueKafkaProps);
                        publisherOpenedTime = LocalDateTime.now();
                        LOGGER.debug("Opening Kafka Producer... PublisherOpenedTime = " + publisherOpenedTime);
                        isPublisherOpened = true;
                    }
                    
                    while (!kafkaBuffer.isEmpty()) {
                        TopicMessage topicMessage = kafkaBuffer.poll();
                        String topic = topicMessage.getTopic();
                        LOGGER.debug("Message Deque-ed and published to Queue: " + topic);
                        queueProducer.send(new ProducerRecord<>(topic, topicMessage.getMessage()));
                    }
                    if (varCount != 0 && varCount == kafkaCounterVar - 1) {
                        LOGGER.debug("Closing Kafka Producer...");
                        queueProducer.flush();
                        queueProducer.close();
                        varCount = 0;
                        queueProducer = null;
                        isPublisherOpened = false;
                    } else {
                        varCount++;
                    }
                    TimeUnit.MILLISECONDS.sleep(queueWaitTime);
                } else {
                    if (isPublisherOpened && ChronoUnit.MINUTES.between(publisherOpenedTime, LocalDateTime.now()) >= publisherIdleTime) {
                        LOGGER.debug("Closing Kafka Producer... PublisherClosingTime = " + LocalDateTime.now());
                        queueProducer.flush();
                        queueProducer.close();
                        varCount = 0;
                        queueProducer = null;
                        isPublisherOpened = false;
                    }
                    TimeUnit.MILLISECONDS.sleep(queueWaitTime);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOGGER.error("Thread interrupted", ex);
            } catch (Exception e) {
                LOGGER.error("Error in polling the queue. " + e.getMessage() + "::", e);
            }
        }
    }

    public static void stopExecutorThread() {
        try {
            if (executor != null) {
                executor.shutdown();
            }

            if (futureThread != null) {
                futureThread.cancel(true);
            }
        } catch (Exception e) {
            LOGGER.error("Error in polling the queue. " + e.getMessage() + "::", e);
        } finally {
            isRunnable = false;
        }

        LOGGER.info("USSD publisher thread closed. isRunnable = " + isRunnable);
    }

    public void addEvent(String topicName, String message) {
        TopicMessage topicMessage = new TopicMessage();
        topicMessage.setTopic(topicName);
        topicMessage.setMessage(message);
        kafkaBuffer.add(topicMessage);
    }

}
