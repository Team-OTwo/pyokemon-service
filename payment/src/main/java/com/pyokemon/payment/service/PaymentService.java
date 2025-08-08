package com.pyokemon.payment.service;

import org.springframework.stereotype.Service;

import com.pyokemon.payment.dto.PaymentDto;
import com.pyokemon.payment.dto.PaymentInitiateRequestDto;
import com.pyokemon.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
  final PaymentRepository paymentRepository;

  public void reserve(PaymentInitiateRequestDto request) {
    PaymentDto dto = new PaymentDto();
    dto.setBookingId(request.getBookingId());
    dto.setOrderId(request.getOrderId());
    dto.setAmount(request.getAmount());
    dto.setMethod(request.getMethod());
    dto.setStatus("READY");
    dto.setAccountId(request.getAccountId());

    paymentRepository.insertInitiatePayment(dto);

  }
}
