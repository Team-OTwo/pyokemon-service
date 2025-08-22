package com.pyokemon.booking.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.pyokemon.booking.dto.kafka.BookingEventDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.common.kafka.KafkaTopicConstants;

@ExtendWith(MockitoExtension.class)
class BookingEventPublisherTest {

  @Mock
  private KafkaTemplate<Long, Object> kafkaTemplate;

  @InjectMocks
  private BookingEventPublisher bookingEventPublisher;

  private Booking testBooking;

  @BeforeEach
  void setUp() {
    testBooking = Booking.builder().bookingId(1L).eventScheduleId(100L).accountId(200L)
        .paymentId(300L).status(Booking.Booked.BOOKED).createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now()).build();
  }

  @Test
  void publishBookingStatusUpdate_Booked_ShouldPublishEvent() {
    bookingEventPublisher.publishBookingStatusUpdate(testBooking);

    verify(kafkaTemplate).send(eq(KafkaTopicConstants.BOOKING_STATUS_UPDATED),
        eq(testBooking.getBookingId()), any(BookingEventDto.class));
  }

  @Test
  void publishBookingStatusUpdate_Canceled_ShouldPublishEvent() {
    testBooking.setStatus(Booking.Booked.CANCELED);

    bookingEventPublisher.publishBookingStatusUpdate(testBooking);

    verify(kafkaTemplate).send(eq(KafkaTopicConstants.BOOKING_STATUS_UPDATED),
        eq(testBooking.getBookingId()), any(BookingEventDto.class));
  }

  @Test
  void publishBookingStatusUpdate_Failed_ShouldPublishEvent() {
    testBooking.setStatus(Booking.Booked.FAILED);

    bookingEventPublisher.publishBookingStatusUpdate(testBooking);

    verify(kafkaTemplate).send(eq(KafkaTopicConstants.BOOKING_STATUS_UPDATED),
        eq(testBooking.getBookingId()), any(BookingEventDto.class));
  }

  @Test
  void publishBookingStatusUpdate_Pending_ShouldNotPublishEvent() {
    testBooking.setStatus(Booking.Booked.PENDING);

    bookingEventPublisher.publishBookingStatusUpdate(testBooking);

    verify(kafkaTemplate, never()).send(any(), any(), any());
  }
}
