package com.pyokemon.event.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.event.repository.EventScheduleRepository;
import com.pyokemon.event.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Slf4j
public class RedisController {

  private final RedisService redisService;
  private final EventScheduleRepository eventScheduleRepository;

  @GetMapping("/{scheduleId}/status/by-class")
  public ResponseEntity<Map<String, Integer>> getAvailableSeatCountsBySeatClass(
      @PathVariable Long scheduleId) {
    log.info("좌석 클래스별 남은 좌석 개수 조회 요청: scheduleId={}", scheduleId);

    try {
      Map<String, Integer> availableSeatCounts =
          redisService.getAvailableSeatCountsBySeatClass(scheduleId);
      return ResponseEntity.ok(availableSeatCounts);
    } catch (Exception e) {
      log.error("좌석 클래스별 남은 좌석 개수 조회 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/{scheduleId}/status/class/{seatClassName}")
  public ResponseEntity<Map<String, String>> getSeatStatusesBySeatClassName(
      @PathVariable Long scheduleId, @PathVariable String seatClassName) {
    log.info("좌석 클래스별 상태 조회 요청: scheduleId={}, seatClassName={}", scheduleId, seatClassName);

    try {
      Map<String, String> seatStatuses =
          redisService.getSeatStatusesBySeatClassName(scheduleId, seatClassName);
      return ResponseEntity.ok(seatStatuses);
    } catch (Exception e) {
      log.error("좌석 클래스별 상태 조회 실패: scheduleId={}, seatClassName={}, error={}", scheduleId,
          seatClassName, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping("/{scheduleId}/{seatId}/hold")
  public ResponseEntity<String> holdSeat(@PathVariable Long scheduleId, @PathVariable Long seatId) {
    Long userId = GatewayRequestHeaderUtils.getAccountIdOrThrow();
    long ttlSeconds = 300L;
    log.info("좌석 홀드 요청: scheduleId={}, seatId={}, userId={}, ttl={}초", scheduleId, seatId, userId,
        ttlSeconds);

    try {
      redisService.holdSeat(scheduleId, seatId, userId, ttlSeconds);
      return ResponseEntity.ok("좌석 홀드 성공");
    } catch (Exception e) {
      log.error("좌석 홀드 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId, e.getMessage(),
          e);
      return ResponseEntity.internalServerError().body("좌석 홀드 실패: " + e.getMessage());
    }
  }

  @DeleteMapping("/{scheduleId}/{seatId}/hold")
  public ResponseEntity<String> releaseSeatHold(@PathVariable Long scheduleId,
      @PathVariable Long seatId) {
    log.info("좌석 홀드 해제 요청: scheduleId={}, seatId={}", scheduleId, seatId);

    try {
      redisService.releaseSeatHold(scheduleId, seatId);
      return ResponseEntity.ok("좌석 홀드 해제 성공");
    } catch (Exception e) {
      log.error("좌석 홀드 해제 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId,
          e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 홀드 해제 실패: " + e.getMessage());
    }
  }

  @PostMapping("/admin/event-redis/init/{eventScheduleId}")
  public ResponseEntity<String> initializeSpecificEvent(@PathVariable Long eventScheduleId) {
    try {
      Long venueId = eventScheduleRepository.findVenueIdByEventScheduleId(eventScheduleId);
      redisService.initSeatStatuses(eventScheduleId, venueId);
      return ResponseEntity.ok("특정 이벤트 Redis 초기화 완료: eventScheduleId=" + eventScheduleId);

    } catch (Exception e) {
      log.error("특정 이벤트 Redis 초기화 실패: eventScheduleId={}, error={}", eventScheduleId,
          e.getMessage(), e);
      e.printStackTrace();
      return ResponseEntity.internalServerError().body("Redis 초기화 실패: " + e.getMessage());
    }
  }
}
