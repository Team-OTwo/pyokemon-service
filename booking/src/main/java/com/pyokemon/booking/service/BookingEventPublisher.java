package com.pyokemon.booking.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.pyokemon.booking.dto.kafka.BookingEventDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.common.kafka.KafkaTopicConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventPublisher {

  private final KafkaTemplate<Long, Object> kafkaTemplate;

  public void publishBookingStatusUpdate(Booking booking) {
    try {
      BookingEventDto event = BookingEventDto.builder().bookingId(booking.getBookingId())
          .eventScheduleId(booking.getEventScheduleId()).accountId(booking.getAccountId())
          .status(booking.getStatus().name()).build();

      kafkaTemplate.send(KafkaTopicConstants.BOOKING_STATUS_UPDATED, booking.getBookingId(), event);

      log.info("Published booking status update event: bookingId={}, status={}",
          booking.getBookingId(), booking.getStatus().name());
    } catch (Exception e) {
      log.error("Failed to publish booking status update event for booking: {}",
          booking.getBookingId(), e);
    }
  }
}
