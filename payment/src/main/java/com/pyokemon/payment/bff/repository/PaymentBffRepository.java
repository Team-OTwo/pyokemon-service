package com.pyokemon.payment.bff.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.payment.entity.Payment;

@Mapper
public interface PaymentBffRepository {

  Optional<Payment> findByPaymentId(@Param("paymentId") Long paymentId);

  List<Payment> findByPaymentIds(@Param("ids") List<Long> ids);

  Long sumTotalRevenueByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);
}
