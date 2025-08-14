package com.pyokemon.booking.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.booking.dto.kafka.BookingEventDto;
import com.pyokemon.booking.entity.Booking;
import com.pyokemon.common.kafka.KafkaMessageSender;
import com.pyokemon.common.kafka.KafkaTopicConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventPublisher {

  private final KafkaMessageSender kafkaMessageSender;
  private final ObjectMapper objectMapper;

  public void publishBookingStatusUpdate(Booking booking) {
    try {
      BookingEventDto event = BookingEventDto.builder().bookingId(booking.getBookingId())
          .eventScheduleId(booking.getEventScheduleId()).accountId(booking.getAccountId())
          .tenantId(booking.getTenantId()).status(booking.getStatus().name()).build();

      String jsonMessage = objectMapper.writeValueAsString(event);
      kafkaMessageSender.send(KafkaTopicConstants.BOOKING_STATUS_UPDATED, booking.getBookingId(), jsonMessage);

      log.info("Published booking status update event: bookingId={}, status={}, message={}",
          booking.getBookingId(), booking.getStatus().name(), jsonMessage);
    } catch (Exception e) {
      log.error("Failed to publish booking status update event for booking: {}",
          booking.getBookingId(), e);
    }
  }
}
