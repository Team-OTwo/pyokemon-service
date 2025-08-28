package com.pyokemon.payment.service;

import org.springframework.stereotype.Service;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.PaymentErrorCodes;
import com.pyokemon.payment.entity.Payment;
import com.pyokemon.payment.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentExpiredService {
  private final PaymentRepository paymentRepository;

  public void expireByBookingId(Long bookingId, String reason) {
    Payment p = paymentRepository.selectLatestByBookingId(bookingId);
    if (p == null) {
      log.error("Payment {} not found.", p.getBookingId());
      throw new BusinessException("Payment not found.", PaymentErrorCodes.PAYMENT_NOT_FOUND);
    }

    if (p.getStatus() == Payment.PaymentStatus.EXPIRED)
      return;

    paymentRepository.cancelPayment(p.getOrderId(), "EXPIRED");
  }
}
