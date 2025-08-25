package com.pyokemon.notification.repository;

import java.util.List;

import javax.management.Notification;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.notification.dto.NotificationResponseDto;

@Mapper
public interface NotificationRepository {

  List<NotificationResponseDto> findNotificationListByAccountId(Long accountId, Long cursorId,
      int size);

  int updateNotificationReadByNotificationId(Long notificationId, Long accountId);

  int updateNotificationReadAllByAccountId(Long accountId);
}
