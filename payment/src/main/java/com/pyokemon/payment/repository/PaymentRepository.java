package com.pyokemon.payment.repository;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.payment.dto.PaymentDto;
import com.pyokemon.payment.entity.Payment;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PaymentRepository {
  void insertInitiatePayment(PaymentDto dto);

  void updatePayment(String orderId, String paymentKey, String status, String method);

  void updatePaymentFailed(String orderId, String status, String method);

  Payment selectByOrderId(String orderId);

  void cancelPayment(String orderId, String status);

  Payment selectLatestByBookingId(Long bookingId);

  Payment selectByOrderIdStatus(String orderId);

  Optional<Payment> findByPaymentId(@Param("paymentId") Long paymentId);

  List<Payment> findByPaymentIds(@Param("ids") List<Long> ids);

  Long sumTotalRevenueByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);
}
