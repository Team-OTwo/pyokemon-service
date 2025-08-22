package com.pyokemon.common.kafka;

public class KafkaTopicConstants {
  public static final String PAYMENT_STATUS_UPDATED = "payment-status-updated";
  public static final String BOOKING_STATUS_UPDATED = "booking-status-updated";
  public static final String EVENT_STATUS_UPDATED = "event-status-updated";

  public static final String PAYMENT_STATUS_UPDATED_DLQ = "payment-status-updated-dlq";
  public static final String BOOKING_STATUS_UPDATED_DLQ = "booking-status-updated-dlq";
  public static final String EVENT_STATUS_UPDATED_DLQ = "event-status-updated-dlq";
}
