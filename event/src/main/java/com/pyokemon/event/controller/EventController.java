package com.pyokemon.event.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.common.exception.code.AccountErrorCodes;
import com.pyokemon.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.event.dto.*;
import com.pyokemon.event.repository.TenantEventRepository;
import com.pyokemon.event.service.EventScheduleService;
import com.pyokemon.event.service.EventService;
import com.pyokemon.event.service.TenantEventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
  private final EventService eventService;
  private final EventScheduleService eventScheduleService;
  private final TenantEventService tenantEventService;
  private final TenantEventRepository tenantEventRepository;

  // 오늘 오픈 티켓
  @GetMapping("/open-today")
  public List<EventItemResponseDTO> getOpenTicketsToday() {
    return eventScheduleService.getTodayOpenedTickets();
  }

  // 오픈 예정 티켓
  @GetMapping("/to-be-opened")
  public List<EventItemResponseDTO> getOpenTicketsToBeOpened() {
    return eventScheduleService.getTicketsToBeOpened();
  }

  // 공연 상세 조회
  @GetMapping("/{eventId}")
  public ResponseEntity<EventDetailResponseDTO> getEventDetail(@PathVariable Long eventId)
      throws NotFoundException {
    Long accountId = GatewayRequestHeaderUtils.getAccountIdOrThrow();
    EventDetailResponseDTO dto = eventService.getEventDetail(eventId, accountId);
    return ResponseEntity.ok(dto);
  }

  // 장르별 리스트 조회
  @GetMapping
  public List<EventItemResponseDTO> getConcertsByPage(
      @RequestParam(defaultValue = "전체") String genre, @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "9") int size) {
    int offset = (page - 1) * size;
    return eventScheduleService.getConcertsByPage(genre, offset, size);
  }

  // 관심 공연 등록, 취소
  @PostMapping("/save/{eventId}")
  public ResponseEntity<String> saveEvent(@PathVariable Long eventId) {
    Long accountId = GatewayRequestHeaderUtils.getAccountIdOrThrow();
    return ResponseEntity.ok(eventService.saveSavedEvent(accountId, eventId));
  }

  // 관심 공연 조회
  @GetMapping("/saved-events")
  public List<EventItemResponseDTO> getSavedEvents(@RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "9") int size) {
    Long accountId = GatewayRequestHeaderUtils.getAccountIdOrThrow();
    if (accountId == null) {
      throw new BusinessException("로그인이 필요합니다.", AccountErrorCodes.ACCESS_DENIED);
    }

    int offset = (page - 1) * size;
    return eventService.getSavedEvents(accountId, offset, size);
  }

  // 검색
  @GetMapping("/keyword")
  public List<EventItemResponseDTO> getEventSearch(@RequestParam String keyword,
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int size,
      @RequestParam(defaultValue = "전체") String genre) {
    int offset = (page - 1) * size;
    return eventScheduleService.getEventSearch(keyword, offset, size, genre);
  }

  // 예매 초기 정보 조회
  @GetMapping("/booking-info/{eventScheduleId}")
  public List<BookingInfoResponseDTO> getBookingInfo(@PathVariable Long eventScheduleId) {
    return eventScheduleService.getBookingInfo(eventScheduleId);
  }

  // 등급 좌석 정보 조회
  @GetMapping("/booking-info/{eventScheduleId}/{seatGrade}")
  public List<SeatInfoResponseDTO> getSeatInfoByGrade(@PathVariable Long eventScheduleId,
      @PathVariable String seatGrade) {
    return eventScheduleService.getSeatInfoByGrade(eventScheduleId, seatGrade);
  }

  // 공연등록 승인
  @PostMapping("/approved/{eventId}")
  public ResponseEntity<CancelEventResponseDTO> updateApprovedStatusEvent(
      @PathVariable Long eventId) {
    CancelEventResponseDTO dto = new CancelEventResponseDTO();
    dto.setEventId(eventId);
    dto.setStatus("APPROVED");
    tenantEventRepository.cancelEvent(dto);
    return ResponseEntity.ok().build();
  }

  // 공연등록 거절
  @PostMapping("/rejected/{eventId}")
  public ResponseEntity<CancelEventResponseDTO> updateRejectedStatusEvent(
      @PathVariable Long eventId) {
    CancelEventResponseDTO dto = new CancelEventResponseDTO();
    dto.setEventId(eventId);
    dto.setStatus("REJECTED");
    tenantEventRepository.cancelEvent(dto);
    return ResponseEntity.ok().build();
  }

  // 좌석 상세 정보 조회
  @GetMapping("/seat-detail")
  public ResponseEntity<SeatDetailResponseDTO> getSeatDetail(@RequestParam Long eventScheduleId,
      @RequestParam Long seatId) throws NotFoundException {
    SeatDetailResponseDTO dto = eventService.getSeatDetail(eventScheduleId, seatId);
    return ResponseEntity.ok(dto);
  }

}
