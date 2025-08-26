package com.pyokemon.notification.repository;

import java.util.List;


import com.pyokemon.notification.entity.Notifications;
import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.notification.dto.NotificationResponseDto;

@Mapper
public interface NotificationRepository {

    void save(Notifications notification);

    List<NotificationResponseDto> findNotificationListByAccountId(Long accountId, Long cursorId,
          int size);

    int updateNotificationReadByNotificationId(Long notificationId, Long accountId);

    int updateNotificationReadAllByAccountId(Long accountId);
}
