package com.pyokemon.payment.bff.service;

// import org.springframework.security.crypto.password.PasswordEncoder;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.payment.bff.dto.PaymentDto;
import com.pyokemon.payment.bff.repository.PaymentBffRepository;
import com.pyokemon.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentBffService {

  private final PaymentBffRepository paymentBffRepository;

  public PaymentDto getPayment(Long paymentId){
    Optional<Payment> paymentOpt = paymentBffRepository.findByPaymentId(paymentId);

    if(paymentOpt.isEmpty()){
      throw new BusinessException("없음 ㅋㅋ", "ㅋㅋㄹㅃㅃ");
    }

    Payment payment = paymentOpt.get();

    return PaymentDto.builder()
            .paymentId(paymentId)
            .amount(payment.getAmount())
            .method(payment.getMethod())
            .status(payment.getStatus())
            .updatedAt(payment.getUpdatedAt())
            .build();
  }
}
