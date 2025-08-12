// src/test/java/com/pyokemon/payment/service/TossPaymentServiceTest.java
package com.pyokemon.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.pyokemon.payment.dto.PaymentConfirmRequestDto;
import com.pyokemon.payment.dto.PaymentConfirmResponseDto;
import com.pyokemon.payment.dto.PaymentKafkaDto;
import com.pyokemon.payment.entity.Payment;
import com.pyokemon.payment.entity.Payment.PaymentStatus;
import com.pyokemon.payment.producer.KafkaMessageProducer;
import com.pyokemon.payment.repository.PaymentRepository;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class TossPaymentServiceTest {

  @Mock
  PaymentRepository paymentRepository;

  @Mock
  KafkaMessageProducer kafkaMessageProducer;

  // ✅ Deep stubs로 체인 한 줄 스텁
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  WebClient webClient;

  @InjectMocks
  TossPaymentService tossPaymentService;

  // ====== 성공 케이스 ======
  @Test
  void confirm_success_updatesPayment_sendsKafka_returnsDto() {
    // given
    PaymentConfirmRequestDto req = new PaymentConfirmRequestDto();
    set(req, "orderId", "ORDER_123");
    set(req, "paymentKey", "pay_abc");

    PaymentConfirmResponseDto resp = new PaymentConfirmResponseDto();
    // 네 DTO 구조에 맞게 수정 (세터가 있으면 세터 사용)
    set(resp, "method", "CARD");

    // WebClient 체인: 성공 응답
    when(webClient.post().uri(anyString()).bodyValue(any()).exchangeToMono(any()))
        .thenReturn(Mono.just(resp));

    // DB 재조회 결과 (Kafka payload 구성용)
    Payment persisted =
        Payment.builder().paymentId(100L).bookingId(200L).status(PaymentStatus.DONE).build();
    when(paymentRepository.selectByOrderId("ORDER_123")).thenReturn(persisted);

    // when
    PaymentConfirmResponseDto result = tossPaymentService.confirm(req);

    // then
    assertThat(result).isNotNull();
    verify(paymentRepository).updatePayment("ORDER_123", "pay_abc", "DONE", "CARD");
    verify(paymentRepository).selectByOrderId("ORDER_123");

    ArgumentCaptor<PaymentKafkaDto> captor = ArgumentCaptor.forClass(PaymentKafkaDto.class);
    verify(kafkaMessageProducer, times(1)).sendPaymentConfirmed(captor.capture());

    PaymentKafkaDto sent = captor.getValue();
    assertThat(sent.getPaymentId()).isEqualTo(100L);
    assertThat(sent.getBookingId()).isEqualTo(200L);
    // status 타입이 String이면 "DONE" 비교, enum이면 생략 또는 맞게 비교
    // 예) assertThat(sent.getStatus()).isEqualTo("DONE");
  }

  // ====== 실패 케이스(승인 실패) ======
  @Test
  void confirm_failure_marksFailed_sendsKafka_thenThrows() {
    // given
    PaymentConfirmRequestDto req = new PaymentConfirmRequestDto();
    set(req, "orderId", "ORDER_123");
    set(req, "paymentKey", "pay_abc");

    // WebClient 체인: 실패 응답
    when(webClient.post().uri(anyString()).bodyValue(any()).exchangeToMono(any()))
        .thenReturn(Mono.error(new RuntimeException("Toss confirm failed")));

    // 실패 후 DB 재조회 결과 (Kafka 실패 알림용)
    Payment failedRow =
        Payment.builder().paymentId(111L).bookingId(222L).status(PaymentStatus.FAILED).build();
    when(paymentRepository.selectByOrderId("ORDER_123")).thenReturn(failedRow);

    // when & then
    assertThrows(RuntimeException.class, () -> tossPaymentService.confirm(req));

    verify(paymentRepository).updatePaymentFailed(eq("ORDER_123"), eq("FAILED"), isNull());
    verify(paymentRepository).selectByOrderId("ORDER_123");

    ArgumentCaptor<PaymentKafkaDto> captor = ArgumentCaptor.forClass(PaymentKafkaDto.class);
    verify(kafkaMessageProducer, times(1)).sendPaymentConfirmed(captor.capture());

    PaymentKafkaDto sent = captor.getValue();
    assertThat(sent.getPaymentId()).isEqualTo(111L);
    assertThat(sent.getBookingId()).isEqualTo(222L);
    // 예) assertThat(sent.getStatus()).isEqualTo("FAILED");
  }

  // ====== fail() 직접 호출 ======
  @Test
  void fail_marksFailed_and_sendsKafka() {
    // given
    PaymentConfirmRequestDto req = new PaymentConfirmRequestDto();
    set(req, "orderId", "ORDER_999");

    Payment failedRow =
        Payment.builder().paymentId(999L).bookingId(888L).status(PaymentStatus.FAILED).build();
    when(paymentRepository.selectByOrderId("ORDER_999")).thenReturn(failedRow);

    // when
    tossPaymentService.fail(req);

    // then
    verify(paymentRepository).updatePaymentFailed(eq("ORDER_999"), eq("FAILED"), isNull());
    verify(paymentRepository).selectByOrderId("ORDER_999");

    ArgumentCaptor<PaymentKafkaDto> captor = ArgumentCaptor.forClass(PaymentKafkaDto.class);
    verify(kafkaMessageProducer, times(1)).sendPaymentConfirmed(captor.capture());

    PaymentKafkaDto sent = captor.getValue();
    assertThat(sent.getPaymentId()).isEqualTo(999L);
    assertThat(sent.getBookingId()).isEqualTo(888L);
  }

  // ====== 리플렉션 유틸: 세터가 없을 때만 사용 ======
  private static void set(Object target, String field, Object value) {
    try {
      var f = target.getClass().getDeclaredField(field);
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception ignored) {
    }
  }
}
