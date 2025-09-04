package com.pyokemon.notification.event.consumer;

import com.pyokemon.notification.dto.SavedNotificationDto;
import com.pyokemon.notification.dto.kafka.SaveEventKafkaDto;
import com.pyokemon.notification.repository.NotificationRepository;
import com.pyokemon.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SaveEventListener {

  private final NotificationRepository notificationRepository;
  private final NotificationService notificationService;

  @KafkaListener(
      topics = "#{T(com.pyokemon.common.kafka.KafkaTopicConstants).EVENT_SAVED_UPDATED}",
      groupId = "${spring.application.name}", containerFactory = "eventkafkaListenerContainerFactory")

  public void handleSaveEvent(SaveEventKafkaDto event) {
    log.info("Received raw event: {}", event);

    if (event == null) {
      log.warn("[event-saved-updated] Received null event");
      return;
    }

    Long eventId = event.getEventId();
    Long accountId = event.getAccountId();
    String title = event.getTitle();
    LocalDateTime ticketOpenAt = event.getTicketOpenAt();

    log.info(
        "[event-saved-updated] Received event: eventId={}, accountId={}, title={}",
        eventId, accountId, title);

    if (eventId == null || accountId == null) {
      log.warn("[event-saved-updated] BookingEventDto missing required fields: {}", event);
      return;
    }

    notificationService.savedNotification(accountId, eventId, title, ticketOpenAt);

  }
}
