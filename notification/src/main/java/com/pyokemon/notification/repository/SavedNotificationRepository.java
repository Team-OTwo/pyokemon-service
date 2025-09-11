package com.pyokemon.notification.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.notification.dto.SavedNotificationDto;

@Mapper
public interface SavedNotificationRepository {
  Long saveNotification(SavedNotificationDto dto);

  Long deleteNotification(Long accountId, Long eventId);

  SavedNotificationDto selectNotification(Long accountId, Long eventId);

  List<SavedNotificationDto> findByTicketOpenAtBetween(@Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
