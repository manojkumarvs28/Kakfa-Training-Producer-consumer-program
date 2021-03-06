package com.kafkabeginnerscourse.kafka.tutorial1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoAssignAndSeek {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(ConsumerDemoAssignAndSeek.class.getName());

        String bootstarpServers = "127.0.0.1:9092";
        String groupId = "myFouthConsumerApplication";
        String topicName = "first_topic";

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstarpServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        //properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String,String> consumer = new KafkaConsumer<String, String>(properties);

        TopicPartition topicPartition = new TopicPartition(topicName, 0);
        consumer.assign(Arrays.asList(topicPartition));

        //seek
        long offsetToReadFrom = 1L;
        consumer.seek(topicPartition, offsetToReadFrom);
        int numberOfMessagesToRead = 5;
        boolean keepOnReading =true;
        int numberOfMessagesReadSoFar = 0;

        while(keepOnReading) {
            ConsumerRecords<String, String> consumerRecords =consumer.poll(Duration.ofMillis(100));
            for(ConsumerRecord record: consumerRecords) {
                numberOfMessagesReadSoFar += 1;
                logger.info("Topic: "+ record.topic() + " offset: " +  record.offset() + " partition: " + record.partition() + " key: " + record.key() + " value: " +record.value());
                if(numberOfMessagesReadSoFar >= numberOfMessagesToRead) {
                    keepOnReading = false;
                    break;
                }
            }
        }

        logger.info("Exiting the application");

    }
}
