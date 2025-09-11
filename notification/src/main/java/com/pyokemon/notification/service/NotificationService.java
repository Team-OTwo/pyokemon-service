package com.pyokemon.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.*;
import com.pyokemon.notification.dto.NotificationListResponseDtoApp;
import com.pyokemon.notification.dto.NotificationResponseDto;
import com.pyokemon.notification.dto.NotificationSendRequestDto;
import com.pyokemon.notification.dto.SavedNotificationDto;
import com.pyokemon.notification.entity.Notifications;
import com.pyokemon.notification.entity.SavedNotification;
import com.pyokemon.notification.remote.account.RemoteAccountService;
import com.pyokemon.notification.remote.account.dto.UserInfoResponseDto;
import com.pyokemon.notification.repository.NotificationRepository;
import com.pyokemon.notification.repository.SavedNotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final SavedNotificationRepository savedNotificationRepository;
  private final RemoteAccountService remoteAccountService;

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
    log.info("Sending notification to account {}", notificationSendRequest);
    log.info("Sending notification to account {}", accountId);
    try {
      Message message = Message.builder().setToken(notificationSendRequest.getToken())
          .setNotification(Notification.builder().setTitle(notificationSendRequest.getTitle())
              .setBody(notificationSendRequest.getMessage()).build())
          .build();
      String response = FirebaseMessaging.getInstance().sendAsync(message).get();

      Notifications notification = new Notifications();
      notification.setTitle(notificationSendRequest.getTitle());
      notification.setMessage(notificationSendRequest.getMessage());
      notification.setAccountId(accountId);

      notificationRepository.save(notification);

      return response;
    } catch (ExecutionException | InterruptedException e) {
      log.error(e.getMessage());
      throw new RuntimeException("FCM 멀티캐스트 메세지 전송실패");
    }
  }

  private UserInfoResponseDto getAccountInfo(Long accountId) {
    try {
      return remoteAccountService.getUserInfo(accountId);
    } catch (Exception e) {
      log.error("Failed to get user info for accountId: {}", accountId, e);
      return null;
    }
  }

  public String savedNotification(Long accountId, Long eventId, String title,
      LocalDateTime ticketOpenAt) {
    SavedNotificationDto existing =
        savedNotificationRepository.selectNotification(accountId, eventId);

    if (existing == null) {
      SavedNotificationDto dto =
          SavedNotificationDto.builder().accountId(accountId).eventId(eventId).title(title)
              .message(title + "북마크 등록 되었습니다.").ticketOpenAt(ticketOpenAt).build();

      savedNotificationRepository.saveNotification(dto);

      return dto.getMessage();
    } else {
      savedNotificationRepository.deleteNotification(accountId, eventId);

      return title + "북마크 취소되었습니다.";
    }
  }

  @Transactional
  @Scheduled(cron = "0 */1 * * * *")
  public void sendBeforeTicketOpenNotification() {
    LocalDateTime now = LocalDateTime.now();


    LocalDateTime start = now.plusHours(1).withSecond(0).withNano(0);
    LocalDateTime end = start.plusMinutes(1);

    List<SavedNotificationDto> notifications =
        savedNotificationRepository.findByTicketOpenAtBetween(start, end);

    for (SavedNotificationDto notification : notifications) {
      try {
        UserInfoResponseDto userInfo = getAccountInfo(notification.getAccountId());

        if (userInfo == null) {
          log.warn("계정 정보를 찾을 수 없음. accountId={}", notification.getAccountId());
          continue;
        }

        String fcmToken = userInfo.getFcmToken();
        if (fcmToken == null || fcmToken.isBlank()) {
          log.warn("No FCM token for accountId: {}", notification.getAccountId());
          continue;
        }

        String title = notification.getTitle();

        Message message = Message.builder().setToken(fcmToken)
            .setNotification(
                Notification.builder().setTitle(title).setBody(title + " 티켓이 1시간 후 오픈됩니다!").build())
            .build();

        String response = FirebaseMessaging.getInstance().sendAsync(message).get();
        log.info("FCM sent: {}, accountId={}", response, notification.getAccountId());

      } catch (ExecutionException e) {
        log.error("FCM 전송 실패 (accountId={}): {}", notification.getAccountId(), e.getMessage());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("FCM 전송 중 인터럽트 발생 (accountId={}): {}", notification.getAccountId(),
            e.getMessage());
      } catch (Exception e) {
        log.error("FCM 전송 중 알 수 없는 오류 (accountId={}): {}", notification.getAccountId(),
            e.getMessage(), e);
      }
    }
  }


}
