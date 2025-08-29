package com.pyokemon.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.pyokemon.payment.dto.PaymentInitiateRequestDto;
import com.pyokemon.payment.repository.PaymentRepository;
import com.pyokemon.payment.service.PaymentService;

class PaymentServiceTest {

  private PaymentRepository paymentRepository;
  private PaymentService paymentService;

  @BeforeEach
  void setUp() {
    paymentRepository = mock(PaymentRepository.class);
    paymentService = new PaymentService(paymentRepository);
  }

  @Test
  void reserve_shouldInsertPaymentDto() {
    // given
    PaymentInitiateRequestDto request = new PaymentInitiateRequestDto();
    request.setBookingId(100L);
    request.setOrderId("ORDER-1234");
    request.setAmount(5000);
    request.setMethod("CARD");

    Long eventScheduleId = 200L;

    // when
    paymentService.reserve(request, eventScheduleId);

    // then
    ArgumentCaptor<com.pyokemon.payment.dto.PaymentDto> captor =
        ArgumentCaptor.forClass(com.pyokemon.payment.dto.PaymentDto.class);

    verify(paymentRepository, times(1)).insertInitiatePayment(captor.capture());

    var savedDto = captor.getValue();
    assertThat(savedDto.getBookingId()).isEqualTo(100L);
    assertThat(savedDto.getOrderId()).isEqualTo("ORDER-1234");
    assertThat(savedDto.getAmount()).isEqualTo(5000);
    assertThat(savedDto.getMethod()).isEqualTo("CARD");
    assertThat(savedDto.getStatus()).isEqualTo("READY");
    assertThat(savedDto.getEventScheduleId()).isEqualTo(200L);
  }
}
