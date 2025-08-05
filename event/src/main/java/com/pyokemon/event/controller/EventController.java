package com.pyokemon.event.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.dto.EventRegisterDto;
import com.pyokemon.event.dto.EventResponseDto;
import com.pyokemon.event.entity.Event;
import com.pyokemon.event.service.EventScheduleService;
import com.pyokemon.event.service.EventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
  private final EventService eventService;
  private final EventScheduleService eventScheduleService;

  // 이벤트 등록
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseDto<EventResponseDto> registerEvent(
      @Valid @RequestBody EventRegisterDto eventRegisterDto) {
    EventResponseDto registeredEvent = eventService.registerEvent(eventRegisterDto);
    return ResponseDto.success(registeredEvent, "Event registered successfully");
  }

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

  @GetMapping("/{eventId}")
  public ResponseEntity<EventDetailResponseDTO> getEventDetail(@PathVariable Long eventId) {
    EventDetailResponseDTO dto = eventService.getEventDetailByEventId(eventId);
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

  // 검색
  @GetMapping("/keyword")
  public List<EventItemResponseDTO> getEventSearch(@RequestParam String keyword,
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "9") int size,
      @RequestParam(defaultValue = "전체") String genre) {
    int offset = (page - 1) * size;
    return eventScheduleService.getEventSearch(keyword, offset, size, genre);
  }
}
