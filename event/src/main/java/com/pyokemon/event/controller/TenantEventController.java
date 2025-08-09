package com.pyokemon.event.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.event.dto.EventRegisterDto;
import com.pyokemon.event.dto.EventResponseDto;
import com.pyokemon.event.dto.EventScheduleDto;
import com.pyokemon.event.dto.EventUpdateDto;
import com.pyokemon.event.dto.MonthlyEventSummaryResponse;
import com.pyokemon.event.dto.TenantBookingDetailResponseDTO;
import com.pyokemon.event.dto.TenantEventDetailResponseDTO;
import com.pyokemon.event.dto.TenantEventListDto;
import com.pyokemon.event.service.TenantEventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events/tenant")
@RequiredArgsConstructor
public class TenantEventController {
    private final TenantEventService tenantEventService;

    // 테넌트별 공연 목록 조회 (테넌트 웹용)
    @GetMapping
    public ResponseDto<List<TenantEventListDto>> getTenantEventList(@RequestParam Long account_id) {
        List<TenantEventListDto> events = tenantEventService.getTenantEventListByAccountId(account_id);
        return ResponseDto.success(events,
                "Tenant events retrieved successfully for account_id: " + account_id);
    }

    // 테넌트별 월간 공연 요약 조회
    @GetMapping("/monthly-summary")
    public ResponseDto<MonthlyEventSummaryResponse> getTenantMonthlySummary(
            @RequestParam Long account_id, @RequestParam int year, @RequestParam int month) {
        MonthlyEventSummaryResponse response =
                tenantEventService.getMonthlyEventSummary(account_id, year, month);
        return ResponseDto.success(response,
                "Monthly summary retrieved successfully for account_id: " + account_id);
    }

    // 테넌트용 이벤트 상세조회 (가격 정보 포함)
    @GetMapping("/{eventId}/detail")
    public ResponseDto<TenantEventDetailResponseDTO> getTenantEventDetail(
            @PathVariable Long eventId) {
        TenantEventDetailResponseDTO eventDetail = tenantEventService.getTenantEventDetailByEventId(eventId);
        return ResponseDto.success(eventDetail, "Tenant event detail retrieved successfully");
    }

    // 테넌트용 예매 현황 조회 (event_schedule_id 기반)
    @GetMapping("/booking/{eventScheduleId}/detail")
    public ResponseDto<TenantBookingDetailResponseDTO> getTenantBookingDetail(
            @PathVariable Long eventScheduleId) {
        TenantBookingDetailResponseDTO bookingDetail =
                tenantEventService.getTenantBookingDetailByEventScheduleId(eventScheduleId);
        return ResponseDto.success(bookingDetail, "Tenant booking detail retrieved successfully");
    }

    // 이벤트 등록 (테넌트용)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<EventResponseDto> registerEvent(
            @Valid @RequestBody EventRegisterDto eventRegisterDto) {
        EventResponseDto registeredEvent = tenantEventService.registerEvent(eventRegisterDto, eventRegisterDto.getAccountId());
        return ResponseDto.success(registeredEvent, "Event registered successfully");
    }

    // 이벤트 수정 (테넌트용)
    @PutMapping("/{eventId}")
    public ResponseDto<EventResponseDto> updateEvent(@PathVariable Long eventId,
            @Valid @RequestBody EventUpdateDto eventUpdateDto) {
        eventUpdateDto.setEventId(eventId);
        EventResponseDto updatedEvent = tenantEventService.updateEvent(eventUpdateDto);
        return ResponseDto.success(updatedEvent, "Event updated successfully");
    }

    // 일정 등록 (기존 공연에 일정 추가) - 테넌트용
    @PostMapping("/{eventId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<String> registerEventSchedule(@PathVariable Long eventId,
            @Valid @RequestBody EventScheduleDto eventScheduleDto) {
        String result = tenantEventService.registerEventSchedule(eventId, eventScheduleDto);
        return ResponseDto.success(result, "Event schedule registered successfully");
    }
} 