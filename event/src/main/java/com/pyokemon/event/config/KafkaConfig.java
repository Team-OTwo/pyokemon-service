package com.pyokemon.event.config;

import com.pyokemon.common.config.CommonKafkaConfig;
import com.pyokemon.event.dto.kafka.BookingEventDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.application.name}")
    private String applicationName;

//    @Value("${spring.kafka.consumer.group-id}")
//    private String groupId;

//    @Bean
//    public ConsumerFactory<String, Object> consumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//
//        // JSON 역직렬화 설정
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.pyokemon.*");
//        // 기본 타입 설정 - Spring의 자동 타입 추론이 작동하지 않을 때 사용
//        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.pyokemon.common.dto.kafka.BookingEvent");
//
//        return new DefaultKafkaConsumerFactory<>(props);
//    }

//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
//            new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory());
//        return factory;
//    }

    @Bean
    public ConsumerFactory<String, BookingEventDto> bookingEventDtoConsumerFactory() {

        return CommonKafkaConfig.createConsumerFactory(bootstrapServers, applicationName, BookingEventDto.class,"com.pyokemon.common.dto.kafka", "com.pyokemon.common.dto.kafka" );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingEventDto> bookingEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BookingEventDto> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bookingEventDtoConsumerFactory());
        return factory;
    }
}
