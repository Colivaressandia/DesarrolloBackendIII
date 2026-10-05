package com.bancoxyz.banco_microservicio.config;

import com.bancoxyz.banco_microservicio.event.TransaccionCreadaEvent;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;

import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topic.partitions:3}")
    private int topicPartitions;

    @Value("${app.kafka.topic.replication-factor:1}")
    private int topicReplicationFactor;

    @Value("${app.kafka.consumer.concurrency:3}")
    private int consumerConcurrency;

    @Value("${spring.kafka.listener.auto-startup:true}")
    private boolean listenerAutoStartup;

    @Bean
    public NewTopic transaccionesTopic() {
        return TopicBuilder.name("transacciones-creadas")
                .partitions(topicPartitions)
                .replicas(topicReplicationFactor)
                .build();
    }

    // =========================================================
    // PRODUCER
    // =========================================================

    @Bean
    public ProducerFactory<String, TransaccionCreadaEvent> producerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        config.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        config.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, TransaccionCreadaEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // =========================================================
    // CONSUMER
    // =========================================================

    @Bean
    public ConsumerFactory<String, TransaccionCreadaEvent> consumerFactory() {

        JsonDeserializer<TransaccionCreadaEvent> deserializer =
                new JsonDeserializer<>(TransaccionCreadaEvent.class);

        deserializer.addTrustedPackages(
                "com.bancoxyz.banco_microservicio.event"
        );

        Map<String, Object> config = new HashMap<>();

        config.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        config.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "bank-api-group"
        );

        config.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer
        );
    }

    // Este es el bean que necesita @KafkaListener
    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, TransaccionCreadaEvent>
            kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, TransaccionCreadaEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(consumerConcurrency);
        factory.setAutoStartup(listenerAutoStartup);

        return factory;
    }
}