package com.kafkabeginnerscourse.kafka.tutorial1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.omg.SendingContext.RunTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoWithThreads {
    public static void main(String[] args) {
        new ConsumerDemoWithThreads().run();
    }

    public ConsumerDemoWithThreads() {

    }

    public void run() {
        Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.class.getName());
        String bootstarpServers = "127.0.0.1:9092";
        String groupId = "myThirdConsumerApplication";
        String topicName = "first_topic";
        CountDownLatch latch = new CountDownLatch(1);
        Runnable myConsumerRunnable = new ConsumerThread(latch,topicName,bootstarpServers,groupId);
        Thread myThread = new Thread(myConsumerRunnable);
        myThread.start();

        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            logger.info("Caught ShutDownHook");
            ((ConsumerThread)myConsumerRunnable).shutdown();
        } ));

        try {
            latch.await();
        }catch(InterruptedException e) {
            e.printStackTrace();
            logger.error("");
        } finally {
            logger.info("Closing Application");
        }
    }

    public class ConsumerThread implements Runnable {

        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;
        private Logger logger = LoggerFactory.getLogger(ConsumerThread.class.getName());


        public ConsumerThread(CountDownLatch latch, String topicName, String bootstarpServers, String groupId) {
            this.latch = latch;


            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstarpServers);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            consumer = new KafkaConsumer<String, String>(properties);
            consumer.subscribe(Arrays.asList(topicName));
        }

        @Override
        public void run() {
            try {
                while (true) {
                    ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord record : consumerRecords) {

                        logger.info("Topic: " + record.topic() + " offset: " + record.offset() + " partition: " + record.partition() + " key: " + record.key() + " value: " + record.value());
                    }
                }
            } catch(WakeupException e) {
                logger.info("Received ShutDown Signal");
            } finally {
                consumer.close();
                //tell our main code we're done with the consumer
                latch.countDown();
            }
        }

        public void shutdown() {
            //Interupt consumer poll and throw WakeUpException
            consumer.wakeup();
        }
    }
}
