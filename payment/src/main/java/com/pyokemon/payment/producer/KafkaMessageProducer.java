package com.pyokemon.payment.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.pyokemon.payment.dto.PaymentKafkaDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaMessageProducer {

  private final KafkaTemplate<Long, PaymentKafkaDto> kafkaTemplate;

  public void sendPaymentConfirmed(PaymentKafkaDto dto) {
    kafkaTemplate.send("payment-status-updated", dto.getPaymentId(), dto);
  }
}
