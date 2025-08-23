package com.pyokemon.event.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.common.config.CommonKafkaConfig;
import com.pyokemon.event.dto.kafka.BookingEventDto;

@Configuration
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.kafka.consumer.group-id}")
  private String groupId;

  @Autowired
  private ObjectMapper objectMapper;

  @Bean
  public ProducerFactory<String, Object> kafkaProducerFactory() {
    return CommonKafkaConfig.createProducerFactory(bootstrapServers);
  }

  @Bean
  public ConsumerFactory<String, BookingEventDto> bookingEventDtoConsumerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    // ErrorHandlingDeserializer 설정
    config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
    config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

    JsonDeserializer<BookingEventDto> jsonDeserializer =
        new JsonDeserializer<>(BookingEventDto.class);
    jsonDeserializer.setRemoveTypeHeaders(false);
    jsonDeserializer.addTrustedPackages("com.pyokemon.booking.dto.kafka",
        "com.pyokemon.event.dto.kafka");
    jsonDeserializer.setUseTypeMapperForKey(true);

    return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), jsonDeserializer);
  }

  @Bean
  @Primary
  public ConcurrentKafkaListenerContainerFactory<String, BookingEventDto> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, BookingEventDto> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(bookingEventDtoConsumerFactory());
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
    return factory;
  }

  @Bean("kafkaTemplate")
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(kafkaProducerFactory());
  }

}
