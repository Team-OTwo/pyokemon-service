package com.pyokemon.common.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class CommonKafkaConfig {
    
    public static <T> ProducerFactory<String, T> createProducerFactory(String bootstrapServers) {
        return createProducerFactory(bootstrapServers, null);
    }
    
    public static <T> ProducerFactory<String, T> createProducerFactory(String bootstrapServers, Map<String, Object> additionalConfig) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        if (additionalConfig != null) {
            config.putAll(additionalConfig);
        }
        
        return new DefaultKafkaProducerFactory<>(config);
    }
    
    public static <T> ConsumerFactory<String, T> createConsumerFactory(
            String bootstrapServers,
            String groupId,
            Class<T> valueType,
            String... trustedPackages) {
        return createConsumerFactory(bootstrapServers, groupId, valueType, null, trustedPackages);
    }
    
    public static <T> ConsumerFactory<String, T> createConsumerFactory(
            String bootstrapServers,
            String groupId,
            Class<T> valueType,
            Map<String, Object> additionalConfig,
            String... trustedPackages) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        if (additionalConfig != null) {
            config.putAll(additionalConfig);
        }
        
        JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(valueType);
        jsonDeserializer.setRemoveTypeHeaders(false);
        jsonDeserializer.addTrustedPackages(trustedPackages);
        jsonDeserializer.setUseTypeMapperForKey(true);
        
        return new DefaultKafkaConsumerFactory<>(
            config,
            new StringDeserializer(),
            jsonDeserializer
        );
    }
} 