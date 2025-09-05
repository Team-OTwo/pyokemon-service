package com.pyokemon.booking.service;

import org.springframework.stereotype.Service;

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

  public void publishBookingStatusUpdate(Booking booking) {
    try {
      BookingEventDto event = BookingEventDto.builder().bookingId(booking.getId())
          .eventScheduleId(booking.getEventScheduleId()).seatId(booking.getSeatId())
          .accountId(booking.getAccountId()).tenantId(booking.getTenantId())
          .status(booking.getStatus().name()).build();

      kafkaMessageSender.send(KafkaTopicConstants.BOOKING_STATUS_UPDATED,
          String.valueOf(booking.getId()), event);

      log.info("Published booking status update event: bookingId={}, status={}, message={}",
          booking.getId(), booking.getStatus().name(), event);
    } catch (Exception e) {
      log.error("Failed to publish booking status update event for booking: {}", booking.getId(),
          e);
    }
  }

  public void publishBookingEvent(BookingEventDto eventDto) {
    try {
      kafkaMessageSender.send(KafkaTopicConstants.BOOKING_STATUS_UPDATED,
          String.valueOf(eventDto.getBookingId()), eventDto);

      log.info("Published booking event: bookingId={}, status={}, message={}",
          eventDto.getBookingId(), eventDto.getStatus(), eventDto);
    } catch (Exception e) {
      log.error("Failed to publish booking event for booking: {}", eventDto.getBookingId(), e);
    }
  }
}
