package com.pyokemon.event.repository;

import java.util.List;

import com.pyokemon.event.dto.*;
import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.Event;

@Mapper
public interface TenantEventRepository {

  List<TenantEventListDto> findTenantEventListByAccountId(Long accountId);

  TenantEventDetailResponseDTO findTenantEventDetailByEventId(Long eventId);

  TenantBookingDetailResponseDTO findTenantBookingDetailByEventScheduleId(Long eventScheduleId);

  List<MonthlyEventDTO> findMonthlyEventsByAccountId(Long accountId, String startDate,
      String endDate);

  MonthlySummaryDTO findMonthlySummaryByAccountId(Long accountId, String startDate, String endDate);

  int updateEvent(Event event);

  Long save(Event event);

  Long cancelEvent(CancelEventResponseDTO dto);

  Long findEventScheduleId (Long eventId);
}
