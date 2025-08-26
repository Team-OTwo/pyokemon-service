package com.pyokemon.notification.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;

import com.pyokemon.common.kafka.KafkaTopicConstants;
import com.pyokemon.notification.dto.NotificationSendRequestDto;
import com.pyokemon.notification.enums.NotificationType;
import com.pyokemon.notification.event.consumer.message.booking.BookingEvent;
import com.pyokemon.notification.remote.account.RemoteAccountService;
import com.pyokemon.notification.remote.account.dto.UserInfoResponseDto;
import com.pyokemon.notification.remote.event.RemoteEventService;
import com.pyokemon.notification.remote.event.dto.EventInfoResponseDto;
import com.pyokemon.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageConsumer {

  private final RemoteAccountService remoteAccountService;
  private final RemoteEventService remoteEventService;
  private final NotificationService notificationService;

  /**
   * 예매 상태 변경 이벤트를 처리하여 알림을 전송합니다.
   * 
   * @param bookingEvent 예매 이벤트 정보
   */
  @KafkaListener(
      topics = KafkaTopicConstants.EVENT_STATUS_UPDATED,
      properties = {JsonDeserializer.VALUE_DEFAULT_TYPE + 
                   ":com.pyokemon.notification.event.consumer.message.booking.BookingEvent"},
      groupId = "notification-service")
  void handleNotificationEvent(BookingEvent bookingEvent) {
    log.info("Received booking event: {}", bookingEvent);
    
    // 필수 정보 유효성 검증
    if (bookingEvent.getStatus() == null) {
      log.warn("Booking event status is null, skipping notification");
      return;
    }
    
    // 사용자 및 이벤트 정보 조회
    UserInfoResponseDto userInfo = getAccountInfo(bookingEvent.getAccountId());
    if (userInfo == null || !userInfo.getIsLogin() || userInfo.getFcmToken() == null) {
      log.warn("User info invalid or user not logged in, accountId: {}", bookingEvent.getAccountId());
      return;
    }
    
    EventInfoResponseDto eventInfo = getEventInfo(bookingEvent.getEventScheduleId(), bookingEvent.getSeatId());
    if (eventInfo == null) {
      log.warn("Event info not found for scheduleId: {} and seatId: {}", 
               bookingEvent.getEventScheduleId(), bookingEvent.getSeatId());
      return;
    }
    
    // 알림 메시지 생성 및 전송
    try {
      NotificationType type = NotificationType.valueOf(bookingEvent.getStatus());
      NotificationSendRequestDto message = createNotificationMessage(type, userInfo, eventInfo);
      
      if (message.getTitle() != null && message.getMessage() != null) {
        message.setToken(userInfo.getFcmToken());
        sendNotification(message, bookingEvent.getAccountId());
        log.info("Notification sent successfully for booking status: {}", type);
      }
    } catch (IllegalArgumentException e) {
      log.error("Invalid notification type: {}", bookingEvent.getStatus(), e);
    } catch (Exception e) {
      log.error("Failed to process notification for booking event: {}", bookingEvent, e);
    }
  }

  /**
   * 알림 유형에 따른 메시지를 생성합니다.
   * 
   * @param type 알림 유형
   * @param userInfo 사용자 정보
   * @param eventInfo 이벤트 정보
   * @return 생성된 알림 메시지
   */
  private NotificationSendRequestDto createNotificationMessage(
      NotificationType type, 
      UserInfoResponseDto userInfo, 
      EventInfoResponseDto eventInfo) {
      
    NotificationSendRequestDto message = new NotificationSendRequestDto();
    
    switch (type) {
      case BOOKED:
        message.setTitle(String.format("%s님, 오아시스 공연 티켓 예매 성공! 🎉", userInfo.getName()));
        message.setMessage(buildBookedMessage(userInfo.getName(), eventInfo));
        break;
        
      case CANCELLED:
        message.setTitle("예매 취소 알림");
        message.setMessage(String.format("%s님, %s 공연 예매가 취소되었습니다.", 
                         userInfo.getName(), 
                         eventInfo.getEvent().getEvent_title()));
        break;
        
      case PAYMENT_FAILED:
        message.setTitle("결제 실패 알림");
        message.setMessage(String.format("%s님, %s 공연 예매 결제가 실패했습니다. 결제 정보를 확인해주세요.", 
                         userInfo.getName(), 
                         eventInfo.getEvent().getEvent_title()));
        break;
        
      default:
        log.warn("지원하지 않는 알림 타입입니다: {}", type);
        break;
    }
    
    return message;
  }
  
  /**
   * 예매 성공 알림 메시지를 생성합니다.
   * 
   * @param userName 사용자 이름
   * @param eventInfo 이벤트 정보
   * @return 생성된 메시지 내용
   */
  private String buildBookedMessage(String userName, EventInfoResponseDto eventInfo) {
    return String.format(
        "%s님, 꿈에 그리던 %s 티켓 예매에 성공하셨어요!\n\n" +
        "잊지 못할 공연이 될 거예요.\n" +
        "・ 공연: %s\n" +
        "・ 일시: %s\n" +
        "・ 좌석: %s석 (%s층 %s열 %s석)\n\n" +
        "공연 당일, 설레는 마음으로 만나요!",
        userName,
        eventInfo.getEvent().getEvent_title(),
        eventInfo.getEvent().getEvent_title(),
        eventInfo.getEvent_schedule().getEvent_date(),
        eventInfo.getSeat().getSeat_class().getClass_name(),
        eventInfo.getSeat().getFloor(),
        eventInfo.getSeat().getRow(),
        eventInfo.getSeat().getCol()
    );
  }
  //  준희님 바꿔줘요!!!!!!!!!

  /**
   * 사용자 정보를 조회합니다.
   * 
   * @param accountId 계정 ID
   * @return 사용자 정보
   */
  private UserInfoResponseDto getAccountInfo(Long accountId) {
    try {
      return remoteAccountService.getUserInfo(accountId);
    } catch (Exception e) {
      log.error("Failed to get user info for accountId: {}", accountId, e);
      return null;
    }
  }

  /**
   * 이벤트 및 좌석 정보를 조회합니다.
   * 
   * @param scheduleId 일정 ID
   * @param seatId 좌석 ID
   * @return 이벤트 정보
   */
  private EventInfoResponseDto getEventInfo(Long scheduleId, Long seatId) {
    try {
      return remoteEventService.getSeatDetails(scheduleId, seatId);
    } catch (Exception e) {
      log.error("Failed to get event info for scheduleId: {} and seatId: {}", scheduleId, seatId, e);
      return null;
    }
  }

  /**
   * 알림을 전송합니다.
   * 
   * @param sendRequest 알림 전송 요청
   * @param accountId 계정 ID
   * @return 전송 결과
   */
  private String sendNotification(NotificationSendRequestDto sendRequest, Long accountId) {
    try {
      return notificationService.sendNotification(sendRequest, accountId);
    } catch (Exception e) {
      log.error("Failed to send notification to accountId: {}", accountId, e);
      return null;
    }
  }
}
