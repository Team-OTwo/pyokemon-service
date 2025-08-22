package com.pyokemon.event.controller;

import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.event.dto.tenant.TenantBookingStatusResponse;
import com.pyokemon.event.service.TenantEventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class TenantBookingController {
  private final TenantEventService tenantEventService;

//  // 공연 일정별 예매 현황 조회
//  @GetMapping
//  public TenantBookingStatusResponse getBookingStatusByEventScheduleId(
//      @RequestParam Long eventScheduleId) {
//    return tenantEventService.getTenantBookingStatusByEventScheduleId(eventScheduleId);
//  }
}
