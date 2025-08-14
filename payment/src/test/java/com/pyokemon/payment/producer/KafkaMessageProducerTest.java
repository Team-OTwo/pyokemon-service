package com.pyokemon.payment.producer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.pyokemon.payment.dto.kafka.PaymentKafkaDto;
import com.pyokemon.payment.entity.Payment;

@ExtendWith(MockitoExtension.class)
class KafkaMessageProducerTest {

  @Mock
  KafkaTemplate<Long, PaymentKafkaDto> kafkaTemplate;

  KafkaMessageProducer producer;

  @Captor
  ArgumentCaptor<String> topicCaptor;
  @Captor
  ArgumentCaptor<Long> keyCaptor;
  @Captor
  ArgumentCaptor<PaymentKafkaDto> valueCaptor;

  @BeforeEach
  void setUp() {
    producer = new KafkaMessageProducer(kafkaTemplate);
  }

  @Test
  void sendPaymentConfirmed_sendsDoneEventToExpectedTopic() {
    // given: 결제 완료(DONE) 이벤트 DTO
    PaymentKafkaDto dto = new PaymentKafkaDto(10L, 777L, "DONE");
    // builder가 있다면:
    // PaymentKafkaDto dto = PaymentKafkaDto.builder()
    // .paymentId(10L).bookingId(777L).status("DONE").build();

    // when
    producer.sendPaymentConfirmed(dto);

    // then
    verify(kafkaTemplate, times(1)).send(anyString(), anyLong(), any(PaymentKafkaDto.class));
    verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

    assertThat(topicCaptor.getValue()).isEqualTo("payment-status-updated");
    assertThat(keyCaptor.getValue()).isEqualTo(10L);
    assertThat(valueCaptor.getValue()).isSameAs(dto);
    assertThat(valueCaptor.getValue().getStatus()).isEqualTo("DONE"); // 상태 확인

    verifyNoMoreInteractions(kafkaTemplate);
  }
}
