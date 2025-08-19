package com.pyokemon.event.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.dto.MonthlyEventDTO;
import com.pyokemon.event.dto.MonthlySummaryDTO;
import com.pyokemon.event.dto.TenantBookingDetailResponseDTO;
import com.pyokemon.event.dto.TenantEventDetailResponseDTO;
import com.pyokemon.event.dto.TenantEventListDto;
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



  // 앱 커서 기반 공연 조회
  List<TenantEventListDto> findEventListForApp(Long accountId, LocalDateTime cursorDate, Long cursorId, int limit);
}
