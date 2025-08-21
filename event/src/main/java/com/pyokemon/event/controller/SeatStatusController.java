package com.pyokemon.event.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.event.service.SeatStatusInitService;
import com.pyokemon.event.service.SeatStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
@Slf4j
public class SeatStatusController {

  private final SeatStatusService seatStatusService;
  private final SeatStatusInitService seatStatusInitService;

  /**
   * 특정 공연 스케줄의 모든 좌석 상태를 조회합니다.
   */
  @GetMapping("/{scheduleId}/status")
  public ResponseEntity<Map<String, String>> getAllSeatStatuses(@PathVariable Long scheduleId) {
    log.info("좌석 상태 조회 요청: scheduleId={}", scheduleId);

    try {
      Map<String, String> seatStatuses = seatStatusService.getAllSeatStatuses(scheduleId);
      return ResponseEntity.ok(seatStatuses);
    } catch (Exception e) {
      log.error("좌석 상태 조회 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 특정 좌석의 상태를 조회합니다.
   */
  @GetMapping("/{scheduleId}/{seatId}/status")
  public ResponseEntity<String> getSeatStatus(@PathVariable Long scheduleId,
      @PathVariable Integer seatId) {
    log.info("좌석 상태 조회 요청: scheduleId={}, seatId={}", scheduleId, seatId);

    try {
      String status = seatStatusService.getSeatStatus(scheduleId, seatId);
      return ResponseEntity.ok(status);
    } catch (Exception e) {
      log.error("좌석 상태 조회 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId,
          e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 좌석을 홀드 상태로 설정합니다.
   */
  @PostMapping("/{scheduleId}/{seatId}/hold")
  public ResponseEntity<String> holdSeat(
      @PathVariable Long scheduleId,
      @PathVariable Integer seatId,
      @RequestHeader("X-Auth-AccountId") Long userId) {
    // TTL 고정 300초
    long ttlSeconds = 300L;
    log.info("좌석 홀드 요청: scheduleId={}, seatId={}, userId={}, ttl={}초", scheduleId, seatId,
        userId, ttlSeconds);

    try {
      seatStatusService.holdSeat(scheduleId, seatId, userId, ttlSeconds);
      return ResponseEntity.ok("좌석 홀드 성공");
    } catch (Exception e) {
      log.error("좌석 홀드 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId,
          e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 홀드 실패: " + e.getMessage());
    }
  }

  /**
   * 좌석 홀드를 해제합니다.
   */
  @DeleteMapping("/{scheduleId}/{seatId}/hold")
  public ResponseEntity<String> releaseSeatHold(@PathVariable Long scheduleId,
      @PathVariable Integer seatId) {
    log.info("좌석 홀드 해제 요청: scheduleId={}, seatId={}", scheduleId, seatId);

    try {
      seatStatusService.releaseSeatHold(scheduleId, seatId);
      return ResponseEntity.ok("좌석 홀드 해제 성공");
    } catch (Exception e) {
      log.error("좌석 홀드 해제 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId,
          e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 홀드 해제 실패: " + e.getMessage());
    }
  }

  /**
   * 좌석을 예약 확정 상태로 변경합니다.
   */
  @PostMapping("/{scheduleId}/{seatId}/confirm")
  public ResponseEntity<String> confirmSeat(@PathVariable Long scheduleId,
      @PathVariable Integer seatId) {
    log.info("좌석 예약 확정 요청: scheduleId={}, seatId={}", scheduleId, seatId);

    try {
      seatStatusService.confirmSeat(scheduleId, seatId);
      return ResponseEntity.ok("좌석 예약 확정 성공");
    } catch (Exception e) {
      log.error("좌석 예약 확정 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId,
          e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 예약 확정 실패: " + e.getMessage());
    }
  }

  /**
   * 좌석 예매를 취소하고 상태를 가용으로 변경합니다.
   */
  @PostMapping("/{scheduleId}/{seatId}/cancel")
  public ResponseEntity<String> cancelSeat(@PathVariable Long scheduleId,
      @PathVariable Integer seatId) {
    log.info("좌석 예매 취소 요청: scheduleId={}, seatId={}", scheduleId, seatId);

    try {
      seatStatusService.cancelSeat(scheduleId, seatId);
      return ResponseEntity.ok("좌석 예매 취소 성공");
    } catch (Exception e) {
      log.error("좌석 예매 취소 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId,
          e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 예매 취소 실패: " + e.getMessage());
    }
  }

  /**
   * 좌석을 블록 상태로 설정합니다.
   */
  @PostMapping("/{scheduleId}/{seatId}/block")
  public ResponseEntity<String> blockSeat(@PathVariable Long scheduleId,
      @PathVariable Integer seatId) {
    log.info("좌석 블록 요청: scheduleId={}, seatId={}", scheduleId, seatId);

    try {
      seatStatusService.blockSeat(scheduleId, seatId);
      return ResponseEntity.ok("좌석 블록 성공");
    } catch (Exception e) {
      log.error("좌석 블록 실패: scheduleId={}, seatId={}, error={}", scheduleId, seatId,
          e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 블록 실패: " + e.getMessage());
    }
  }

  /**
   * 공연 스케줄의 모든 좌석 상태를 삭제합니다.
   */
  @DeleteMapping("/{scheduleId}/status")
  public ResponseEntity<String> deleteSeatStatuses(@PathVariable Long scheduleId) {
    log.info("좌석 상태 삭제 요청: scheduleId={}", scheduleId);

    try {
      seatStatusService.deleteSeatStatuses(scheduleId);
      return ResponseEntity.ok("좌석 상태 삭제 성공");
    } catch (Exception e) {
      log.error("좌석 상태 삭제 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 상태 삭제 실패: " + e.getMessage());
    }
  }

  /**
   * 수동 초기화(테스트/운영툴용): 좌석 해시가 없으면 1..120을 빈 문자열로 채움.
   */
  @PostMapping("/{scheduleId}/init")
  public ResponseEntity<String> initSeats(@PathVariable Long scheduleId) {
    try {
      seatStatusInitService.initSeatStatuses(scheduleId);
      return ResponseEntity.ok("좌석 초기화 완료");
    } catch (Exception e) {
      log.error("좌석 초기화 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      return ResponseEntity.internalServerError().body("좌석 초기화 실패: " + e.getMessage());
    }
  }

  public static class SeatHoldRequest {
    private Long userId;
    private Long ttlSeconds;

    public Long getUserId() {
      return userId;
    }

    public void setUserId(Long userId) {
      this.userId = userId;
    }

    public Long getTtlSeconds() {
      return ttlSeconds;
    }

    public void setTtlSeconds(Long ttlSeconds) {
      this.ttlSeconds = ttlSeconds;
    }
  }
}
