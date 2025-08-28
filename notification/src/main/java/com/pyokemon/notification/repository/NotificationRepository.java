package com.pyokemon.notification.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.notification.dto.NotificationResponseDto;
import com.pyokemon.notification.entity.Notifications;

@Mapper
public interface NotificationRepository {

  void save(Notifications notification);

  List<NotificationResponseDto> findNotificationListByAccountId(Long accountId, Long cursorId,
      int size);

  int updateNotificationReadByNotificationId(Long notificationId, Long accountId);

  int updateNotificationReadAllByAccountId(Long accountId);
}
