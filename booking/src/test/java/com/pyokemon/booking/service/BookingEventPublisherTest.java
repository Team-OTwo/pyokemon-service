package com.pyokemon.booking.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pyokemon.booking.dto.kafka.BookingEventDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.common.kafka.KafkaMessageSender;
import com.pyokemon.common.kafka.KafkaTopicConstants;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingEventPublisher 단위 테스트")
class BookingEventPublisherTest {

  @Mock
  private KafkaMessageSender kafkaMessageSender;

  @InjectMocks
  private BookingEventPublisher bookingEventPublisher;

  private Booking testBooking;
  private BookingEventDto testEventDto;

  @BeforeEach
  void setUp() {
    testBooking = new Booking();
    testBooking.setId(1L);
    testBooking.setEventScheduleId(100L);
    testBooking.setSeatId(200L);
    testBooking.setAccountId(300L);
    testBooking.setTenantId(400L);
    testBooking.setPaymentId(500L);
    testBooking.setStatus(Booking.Booked.BOOKED);
    testBooking.setCreatedAt(LocalDateTime.now());
    testBooking.setUpdatedAt(LocalDateTime.now());

    testEventDto = BookingEventDto.builder().bookingId(1L).eventScheduleId(100L).seatId(200L)
        .accountId(300L).tenantId(400L).status("BOOKED").build();
  }

  @Test
  @DisplayName("예약 상태 업데이트 이벤트 발행 - 성공")
  void publishBookingStatusUpdate_Success() {
    // When
    bookingEventPublisher.publishBookingStatusUpdate(testBooking);

    // Then
    verify(kafkaMessageSender).send(eq(KafkaTopicConstants.BOOKING_STATUS_UPDATED), eq("1"),
        any(BookingEventDto.class));
  }

  @Test
  @DisplayName("예약 상태 업데이트 이벤트 발행 - 예외 발생 시 로그 기록")
  void publishBookingStatusUpdate_Exception_LogsError() {
    // Given
    doThrow(new RuntimeException("Kafka error")).when(kafkaMessageSender).send(anyString(),
        anyString(), any());

    // When
    bookingEventPublisher.publishBookingStatusUpdate(testBooking);

    // Then
    verify(kafkaMessageSender).send(eq(KafkaTopicConstants.BOOKING_STATUS_UPDATED), eq("1"),
        any(BookingEventDto.class));
    // 예외가 발생해도 메서드가 정상적으로 완료되어야 함
  }

  @Test
  @DisplayName("예약 이벤트 발행 - 성공")
  void publishBookingEvent_Success() {
    // When
    bookingEventPublisher.publishBookingEvent(testEventDto);

    // Then
    verify(kafkaMessageSender).send(eq(KafkaTopicConstants.BOOKING_STATUS_UPDATED), eq("1"),
        eq(testEventDto));
  }

  @Test
  @DisplayName("예약 이벤트 발행 - 예외 발생 시 로그 기록")
  void publishBookingEvent_Exception_LogsError() {
    // Given
    doThrow(new RuntimeException("Kafka error")).when(kafkaMessageSender).send(anyString(),
        anyString(), any());

    // When
    bookingEventPublisher.publishBookingEvent(testEventDto);

    // Then
    verify(kafkaMessageSender).send(eq(KafkaTopicConstants.BOOKING_STATUS_UPDATED), eq("1"),
        eq(testEventDto));
    // 예외가 발생해도 메서드가 정상적으로 완료되어야 함
  }

  @Test
  @DisplayName("예약 상태 업데이트 이벤트 발행 - PENDING 상태")
  void publishBookingStatusUpdate_PendingStatus() {
    // Given
    Booking pendingBooking = new Booking();
    pendingBooking.setId(2L);
    pendingBooking.setEventScheduleId(101L);
    pendingBooking.setSeatId(201L);
    pendingBooking.setAccountId(301L);
    pendingBooking.setTenantId(401L);
    pendingBooking.setPaymentId(null);
    pendingBooking.setStatus(Booking.Booked.PENDING);
    pendingBooking.setCreatedAt(LocalDateTime.now());
    pendingBooking.setUpdatedAt(LocalDateTime.now());

    // When
    bookingEventPublisher.publishBookingStatusUpdate(pendingBooking);

    // Then
    verify(kafkaMessageSender).send(eq(KafkaTopicConstants.BOOKING_STATUS_UPDATED), eq("2"),
        any(BookingEventDto.class));
  }

  @Test
  @DisplayName("예약 상태 업데이트 이벤트 발행 - CANCELED 상태")
  void publishBookingStatusUpdate_CanceledStatus() {
    // Given
    Booking canceledBooking = new Booking();
    canceledBooking.setId(3L);
    canceledBooking.setEventScheduleId(102L);
    canceledBooking.setSeatId(202L);
    canceledBooking.setAccountId(302L);
    canceledBooking.setTenantId(402L);
    canceledBooking.setPaymentId(502L);
    canceledBooking.setStatus(Booking.Booked.CANCELED);
    canceledBooking.setCreatedAt(LocalDateTime.now());
    canceledBooking.setUpdatedAt(LocalDateTime.now());

    // When
    bookingEventPublisher.publishBookingStatusUpdate(canceledBooking);

    // Then
    verify(kafkaMessageSender).send(eq(KafkaTopicConstants.BOOKING_STATUS_UPDATED), eq("3"),
        any(BookingEventDto.class));
  }

  @Test
  @DisplayName("예약 상태 업데이트 이벤트 발행 - FAILED 상태")
  void publishBookingStatusUpdate_FailedStatus() {
    // Given
    Booking failedBooking = new Booking();
    failedBooking.setId(4L);
    failedBooking.setEventScheduleId(103L);
    failedBooking.setSeatId(203L);
    failedBooking.setAccountId(303L);
    failedBooking.setTenantId(403L);
    failedBooking.setPaymentId(503L);
    failedBooking.setStatus(Booking.Booked.FAILED);
    failedBooking.setCreatedAt(LocalDateTime.now());
    failedBooking.setUpdatedAt(LocalDateTime.now());

    // When
    bookingEventPublisher.publishBookingStatusUpdate(failedBooking);

    // Then
    verify(kafkaMessageSender).send(eq(KafkaTopicConstants.BOOKING_STATUS_UPDATED), eq("4"),
        any(BookingEventDto.class));
  }

  @Test
  @DisplayName("예약 상태 업데이트 이벤트 발행 - EXPIRED 상태")
  void publishBookingStatusUpdate_ExpiredStatus() {
    // Given
    Booking expiredBooking = new Booking();
    expiredBooking.setId(5L);
    expiredBooking.setEventScheduleId(104L);
    expiredBooking.setSeatId(204L);
    expiredBooking.setAccountId(304L);
    expiredBooking.setTenantId(404L);
    expiredBooking.setPaymentId(null);
    expiredBooking.setStatus(Booking.Booked.EXPIRED);
    expiredBooking.setCreatedAt(LocalDateTime.now());
    expiredBooking.setUpdatedAt(LocalDateTime.now());

    // When
    bookingEventPublisher.publishBookingStatusUpdate(expiredBooking);

    // Then
    verify(kafkaMessageSender).send(eq(KafkaTopicConstants.BOOKING_STATUS_UPDATED), eq("5"),
        any(BookingEventDto.class));
  }
}
