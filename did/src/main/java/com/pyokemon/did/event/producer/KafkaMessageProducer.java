package com.pyokemon.did.event.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageProducer {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void send(String topic, Object message) {
    kafkaTemplate.send(topic, message);
  }
}
