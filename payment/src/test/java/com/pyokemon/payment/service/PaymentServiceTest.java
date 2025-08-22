package com.pyokemon.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.pyokemon.payment.dto.PaymentDto;
import com.pyokemon.payment.dto.PaymentInitiateRequestDto;
import com.pyokemon.payment.repository.PaymentRepository;

class PaymentServiceTest {

  private PaymentRepository paymentRepository;
  private PaymentService paymentService;

  @BeforeEach
  void setUp() {
    paymentRepository = mock(PaymentRepository.class);
    paymentService = new PaymentService(paymentRepository);
  }

  @Test
  void reserve_shouldSavePaymentWithCorrectData() {
    // given
    PaymentInitiateRequestDto request = new PaymentInitiateRequestDto();
    request.setBookingId(1L);
    request.setOrderId("ORDER-123");
    request.setAmount(5000);
    request.setMethod("간편결제");
    request.setAccountId(42L);

    // when
    paymentService.reserve(request);

    // then
    ArgumentCaptor<PaymentDto> captor = ArgumentCaptor.forClass(PaymentDto.class);
    verify(paymentRepository, times(1)).insertInitiatePayment(captor.capture());

    PaymentDto saved = captor.getValue();
    assertEquals(1L, saved.getBookingId());
    assertEquals("ORDER-123", saved.getOrderId());
    assertEquals(5000, saved.getAmount());
    assertEquals("간편결제", saved.getMethod());
    assertEquals("READY", saved.getStatus());
    assertEquals(42L, saved.getAccountId());
  }
}
