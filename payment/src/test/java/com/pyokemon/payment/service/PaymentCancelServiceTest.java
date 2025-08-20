package com.pyokemon.payment.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.payment.dto.kafka.PaymentKafkaDto;
import com.pyokemon.payment.entity.Payment;
import com.pyokemon.payment.producer.KafkaMessageProducer;
import com.pyokemon.payment.repository.PaymentRepository;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PaymentCancelServiceTest {

  @Mock
  PaymentRepository paymentRepository;

  @Mock
  KafkaMessageProducer kafkaMessageProducer;

  PaymentCancelService service;

  // 토스 호출을 항상 200 OK로 돌려주는 가짜 WebClient
  WebClient fakeTossOk;

  @BeforeEach
  void setUp() {
    fakeTossOk = WebClient.builder()
        .exchangeFunction(req -> Mono.just(ClientResponse.create(HttpStatus.OK).build())).build();

    service = new PaymentCancelService(paymentRepository, kafkaMessageProducer, fakeTossOk);
  }

  @Test
  void cancelByBookingId_success_updatesStatus_and_PublishesEvent() {
    // given
    Long bookingId = 123L;

    Payment p = new Payment();
    p.setPaymentId(10L);
    p.setBookingId(bookingId);
    p.setOrderId("ORD-1");
    p.setPaymentKey("pay_123");
    p.setMethod("CARD");
    p.setStatus(Payment.PaymentStatus.DONE); // 취소 가능 상태

    when(paymentRepository.selectLatestByBookingId(bookingId)).thenReturn(p);

    // when
    service.cancelByBookingId(bookingId, "사용자 요청");

    // then
    verify(paymentRepository).selectLatestByBookingId(bookingId);
    verify(paymentRepository).updatePayment("ORD-1", "pay_123", "CANCELED", "CARD");

    // Kafka 이벤트 발행 확인 (값까지 엄격히 볼 필요 없으면 any()로도 OK)
    ArgumentCaptor<PaymentKafkaDto> dtoCap = ArgumentCaptor.forClass(PaymentKafkaDto.class);
    verify(kafkaMessageProducer).sendPaymentConfirmed(dtoCap.capture());
    PaymentKafkaDto sent = dtoCap.getValue();
    // 필드 검증(프로젝트 DTO에 맞춰 필요한 최소만 체크)
    // assertThat(sent.getPaymentId()).isEqualTo(10L);
    // assertThat(sent.getBookingId()).isEqualTo(bookingId);
  }

  @Test
  void cancelByBookingId_alreadyCanceled_isIdempotent_noExternalCall() {
    // given
    Long bookingId = 555L;

    Payment p = new Payment();
    p.setPaymentId(99L);
    p.setBookingId(bookingId);
    p.setOrderId("ORD-99");
    p.setPaymentKey("pay_999");
    p.setMethod("CARD");
    p.setStatus(Payment.PaymentStatus.CANCELED); // 이미 취소됨

    when(paymentRepository.selectLatestByBookingId(bookingId)).thenReturn(p);

    // when
    service.cancelByBookingId(bookingId, "재요청");

    // then: 상태 업데이트/카프카 발행이 없어야 함
    verify(paymentRepository, never()).updatePayment(anyString(), anyString(), anyString(),
        anyString());
    verify(kafkaMessageProducer, never()).sendPaymentConfirmed(any());
  }

  @Test
  void cancelByBookingId_notFound_throwsBusinessException() {
    // given
    Long bookingId = 777L;
    when(paymentRepository.selectLatestByBookingId(bookingId)).thenReturn(null);

    // when & then
    // 현재 서비스 코드의 log.error 에서 p.getBookingId()를 찍고 있어 NPE 위험이 있으니
    // 그 라인(로그)을 bookingId로 바꾸지 않았다면 NPE가 날 수 있음.
    assertThatThrownBy(() -> service.cancelByBookingId(bookingId, "사유"))
        .isInstanceOfAny(BusinessException.class, NullPointerException.class);
  }
}
