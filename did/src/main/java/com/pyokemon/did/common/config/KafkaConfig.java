package com.pyokemon.did.common.config;

import com.pyokemon.common.config.CommonKafkaConfig;
import com.pyokemon.did.event.consumer.message.booking.BookingEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public ProducerFactory<String, Object> kafkaProducerFactory() {
        return CommonKafkaConfig.createProducerFactory(bootstrapServers);
    }

    @Bean
    public ConsumerFactory<String, BookingEventDto> bookingEventDtoConsumerFactory() {
        return CommonKafkaConfig.createConsumerFactory(bootstrapServers, applicationName,
                BookingEventDto.class, "com.pyokemon.booking.dto.kafka", "com.pyokemon.did.event.consumer.message.booking");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingEventDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BookingEventDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bookingEventDtoConsumerFactory());
        return factory;
    }

    @Bean("kafkaTemplate")
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }
}
