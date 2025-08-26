package com.pyokemon.notification.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;

import com.pyokemon.common.kafka.KafkaTopicConstants;
import com.pyokemon.notification.dto.NotificationSendRequestDto;
import com.pyokemon.notification.enums.NotificationType;
import com.pyokemon.notification.event.consumer.message.notification.NotificationEvent;
import com.pyokemon.notification.remote.account.RemoteAccountService;
import com.pyokemon.notification.remote.account.dto.UserInfoResponseDto;
import com.pyokemon.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageConsumer {

  @KafkaListener(topics = KafkaTopicConstants.NOTIFICATION_REQUEST,
      properties = {JsonDeserializer.VALUE_DEFAULT_TYPE
          + ":com.pyokemon.notification.event.consumer.message.notification.NotificationEvent"},
      groupId = "notification-service")
  void handleNotificationEvent(NotificationEvent notificationEvent) {
    log.info("Received notification event {}", notificationEvent);
    UserInfoResponseDto userInfo = getAccountInfo(notificationEvent.getAccountId());
    NotificationSendRequestDto message = new NotificationSendRequestDto();
    message.setToken(userInfo.getFcmToken());
    NotificationType type = NotificationType.valueOf(notificationEvent.getStatus());

    // 임시 테스트를 위한 소스 전체 수정 예정

    switch (type) {
      case BOOKED:
        message.setTitle("예매 완료 알림");
        message.setMessage("예매가 완료되었습니다.");
        break; // break 필수!

      case CANCELLED:
        message.setTitle("예매 취소 알림");
        message.setMessage("예매가 취소되었습니다.");
        break;

      default:
        // 처리하지 않는 타입에 대한 로그
        log.warn("지원하지 않는 알림 타입입니다: {}", type);
        break;
    }
    if (notificationEvent.getStatus() != null) {
      sendNotification(message, notificationEvent.getAccountId());
    }


  }

  private final RemoteAccountService remoteAccountService;
  private final NotificationService notificationService;

  public UserInfoResponseDto getAccountInfo(Long accountId) {
    return remoteAccountService.getUserInfo(accountId);
  }

  public String sendNotification(NotificationSendRequestDto sendRequest, Long accountId) {
    return notificationService.sendNotification(sendRequest, accountId);
  }
}
