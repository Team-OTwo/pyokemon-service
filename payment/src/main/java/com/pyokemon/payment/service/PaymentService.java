package com.pyokemon.payment.service;

import org.springframework.stereotype.Service;

import com.pyokemon.payment.dto.PaymentDto;
import com.pyokemon.payment.dto.PaymentInitiateRequestDto;
import com.pyokemon.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
  final PaymentRepository paymentRepository;

  public void reserve(PaymentInitiateRequestDto request) {
    PaymentDto dto = PaymentDto.builder()
      .bookingId(request.getBookingId())
      .orderId(request.getOrderId())
      .amount(request.getAmount())
      .method(request.getMethod())
      .status("READY")
      .accountId(request.getAccountId())
            .build();

    paymentRepository.insertInitiatePayment(dto);


  }
}
