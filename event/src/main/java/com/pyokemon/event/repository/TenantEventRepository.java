package com.pyokemon.event.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.dto.*;
import com.pyokemon.event.dto.tenant.*;
import com.pyokemon.event.dto.tenant.app.TenantEventDetailDtoForApp;
import com.pyokemon.event.entity.Event;
import org.apache.ibatis.annotations.Param;

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

  Long findEventScheduleId(Long eventId);

  // 앱 커서 기반 공연 조회
  List<TenantEventDetailDtoForApp> findEventListForApp(Long accountId, LocalDateTime cursorDate,
      Long cursorId, int limit, String genre);

  Long countActiveEventsByTenant(@Param("tenantId") Long tenantId, @Param("year") int year, @Param("month") int month);

  List<Long> findScheduleIdsByTenantAndMonth(@Param("tenantId") Long tenantId, @Param("year") int year, @Param("month") int month);
}
