package com.pyokemon.notification.service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.*;
import com.pyokemon.notification.dto.NotificationListResponseDtoApp;
import com.pyokemon.notification.dto.NotificationResponseDto;
import com.pyokemon.notification.dto.NotificationSendRequestDto;
import com.pyokemon.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;

  public NotificationListResponseDtoApp getNotificationListApp(Long accountId, Long cursorId,
      int size) {
    List<NotificationResponseDto> notifications =
        notificationRepository.findNotificationListByAccountId(accountId, cursorId, size + 1);
    NotificationListResponseDtoApp notificationListApp = new NotificationListResponseDtoApp();

    if (notifications.size() > size) {
      NotificationResponseDto lastItem = notifications.get(size);
      notificationListApp.setLastCursorId(lastItem.getNotificationId());
      notifications = notifications.subList(0, size);
    } else {
      notificationListApp.setLastCursorId(null);
    }

    notificationListApp.setNotifications(notifications);

    return notificationListApp;
  }

  public int readNotification(Long notificationId, Long accountId) {
    return notificationRepository.updateNotificationReadByNotificationId(notificationId, accountId);
  }

  public int readAllNotification(Long accountId) {
    return notificationRepository.updateNotificationReadAllByAccountId(accountId);
  }

  public String sendNotification(NotificationSendRequestDto notificationSendRequest,
      Long accountId) {
    try {
      Message message = Message.builder().setToken(notificationSendRequest.getToken())
          .setNotification(Notification.builder().setTitle(notificationSendRequest.getTitle())
              .setBody(notificationSendRequest.getMessage()).build())
          .build();
      String response = FirebaseMessaging.getInstance().sendAsync(message).get();
      return response;
    } catch (ExecutionException | InterruptedException e) {
      log.error(e.getMessage());
      throw new RuntimeException("FCM 멀티캐스트 메세지 전송실패");
    }
  }
}
