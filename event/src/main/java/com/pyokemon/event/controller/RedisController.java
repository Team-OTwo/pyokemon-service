package com.pyokemon.event.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.event.service.RedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Slf4j
public class RedisController {

  private final RedisService redisService;

  /**
   * 특정 공연 스케줄의 모든 좌석 상태를 seatClassName별로 조회합니다.
   */
  @GetMapping("/{scheduleId}/status/by-class")
  public ResponseEntity<Map<String, Map<String, String>>> getAllSeatStatusesBySeatClass(@PathVariable Long scheduleId) {
    log.info("좌석 클래스별 전체 상태 조회 요청: scheduleId={}", scheduleId);

    try {
      Map<String, Map<String, String>> seatStatuses = redisService.getAllSeatStatusesBySeatClass(scheduleId);
      return ResponseEntity.ok(seatStatuses);
    } catch (Exception e) {
      log.error("좌석 클래스별 전체 상태 조회 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 특정 seatClassName의 좌석 상태를 조회합니다.
   */
  @GetMapping("/{scheduleId}/status/class/{seatClassName}")
  public ResponseEntity<Map<String, String>> getSeatStatusesBySeatClassName(
      @PathVariable Long scheduleId,
      @PathVariable String seatClassName) {
    log.info("좌석 클래스별 상태 조회 요청: scheduleId={}, seatClassName={}", scheduleId, seatClassName);

    try {
      Map<String, String> seatStatuses = redisService.getSeatStatusesBySeatClassName(scheduleId, seatClassName);
      return ResponseEntity.ok(seatStatuses);
    } catch (Exception e) {
      log.error("좌석 클래스별 상태 조회 실패: scheduleId={}, seatClassName={}, error={}", 
                scheduleId, seatClassName, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 좌석을 홀드 상태로 설정합니다.
   */
  @PostMapping("/{scheduleId}/{seatId}/hold")
  public ResponseEntity<String> holdSeat(
      @PathVariable Long scheduleId,
      @PathVariable Long seatId,
      @RequestHeader("X-Auth-AccountId") Long userId) {
    // TTL 고정 300초
    long ttlSeconds = 300L;
    log.info("좌석 홀드 요청: scheduleId={}, seatId={}, userId={}, ttl={}초", scheduleId, seatId, userId, ttlSeconds);

    try {
      redisService.holdSeat(scheduleId, seatId, userId, ttlSeconds);
      return ResponseEntity.ok("좌석 홀드 성공");
    } catch (Exception e) {
      log.error("좌석 홀드 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId, e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 홀드 실패: " + e.getMessage());
    }
  }

  /**
   * 좌석 홀드를 해제합니다.
   */
  @DeleteMapping("/{scheduleId}/{seatId}/hold")
  public ResponseEntity<String> releaseSeatHold(@PathVariable Long scheduleId, @PathVariable Long seatId) {
    log.info("좌석 홀드 해제 요청: scheduleId={}, seatId={}", scheduleId, seatId);

    try {
      redisService.releaseSeatHold(scheduleId, seatId);
      return ResponseEntity.ok("좌석 홀드 해제 성공");
    } catch (Exception e) {
      log.error("좌석 홀드 해제 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId, e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 홀드 해제 실패: " + e.getMessage());
    }
  }
}
