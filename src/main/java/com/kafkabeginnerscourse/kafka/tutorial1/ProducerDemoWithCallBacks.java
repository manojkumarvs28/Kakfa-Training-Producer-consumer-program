package com.kafkabeginnerscourse.kafka.tutorial1;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallBacks {
    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallBacks.class);
        String bootstrapServers = "127.0.0.1:9092";
        Properties properties = new Properties();
      /*  properties.setProperty("bootstrap.servers", bootstrapServers);
        properties.setProperty("key.serializers", StringSerializer.class.getName());
        properties.setProperty("value.serrializers", StringSerializer.class.getName());*/
      properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
      properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);
        for(int i=0; i<10; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "Hi Manoj" + Integer.toString(i));
            producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (e == null) {
                        logger.info("offset: " + recordMetadata.offset() + " partition: " + recordMetadata.partition() + " topic: " + recordMetadata.topic() + " timestamp: " + recordMetadata.timestamp());
                    } else {
                        logger.error("Error while producing: ", e);
                    }
                }
            });
        }
        producer.flush();
        producer.close();
    }
}
