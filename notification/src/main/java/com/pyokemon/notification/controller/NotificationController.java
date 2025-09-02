package com.pyokemon.notification.controller;

import java.util.List;

import org.apache.coyote.Response;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.notification.dto.NotificationListResponseDto;
import com.pyokemon.notification.dto.NotificationListResponseDtoApp;
import com.pyokemon.notification.dto.NotificationResponseDto;
import com.pyokemon.notification.dto.NotificationSendRequestDto;
import com.pyokemon.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;


  // 앱 알림 조회
  @GetMapping
  public ResponseDto<NotificationListResponseDtoApp> getNotificationListApp(
      @RequestParam(required = false) Long cursorId, @RequestParam(defaultValue = "8") int size) {
    Long accountId = GatewayRequestHeaderUtils.getAccountIdOrThrow();
    NotificationListResponseDtoApp notifications =
        notificationService.getNotificationListApp(accountId, cursorId, size);
    return ResponseDto.success(notifications, "알람조회가 완료 되었습니다.");
  }

  // 전송확인을 위한
  @PostMapping("/send")
  public ResponseDto sendNotification(@RequestHeader(value = "X-Auth-AccountId") Long accountId,
      @RequestBody NotificationSendRequestDto notificationSendRequest) {

    String result = notificationService.sendNotification(notificationSendRequest, accountId);
    if (result != null) {
      return ResponseDto.success("알람이 전송되었습니다.");
    }
    return null;
  }

  @PutMapping("/{notificationId}")
  public ResponseDto readNotification(@RequestHeader(value = "X-Auth-AccountId") Long accountId,
      @PathVariable Long notificationId) {
    notificationService.readNotification(notificationId, accountId);
    return ResponseDto.success("알림이 읽음 처리 되었습니다.");
  }

  @PutMapping("/readAll")
  public ResponseDto readAllNotification(
      @RequestHeader(value = "X-Auth-AccountId") Long accountId) {
    notificationService.readAllNotification(accountId);
    return ResponseDto.success("알림이 전부 읽음 처리 되었습니다.");
  }
}
