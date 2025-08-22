package com.pyokemon.event.service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.pyokemon.event.entity.Seat;
import com.pyokemon.event.entity.SeatClass;
import com.pyokemon.event.repository.SeatRepository;
import com.pyokemon.event.repository.SeatClassRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisService {

  private final RedisTemplate<String, String> redis;
  private final SeatRepository seatRepository;
  private final SeatClassRepository seatClassRepository;

  public RedisService(@Qualifier("redisTemplate") RedisTemplate<String, String> redis,
                      SeatRepository seatRepository, 
                      SeatClassRepository seatClassRepository) {
    this.redis = redis;
    this.seatRepository = seatRepository;
    this.seatClassRepository = seatClassRepository;
  }

  private static final int TOTAL_SEATS = 120;

  // 기존 패턴 (하위 호환성을 위해 유지)
  private static final String SEAT_STATUS_KEY_PATTERN = "seat:status:%d";
  private static final String SEAT_HOLD_KEY_PATTERN = "seat:hold:%d:%d";
  
  // 새로운 패턴 (seatClassName 기반)
  private static final String SEAT_CLASS_STATUS_KEY_PATTERN = "seat:class:status:%d:%s"; // scheduleId:seatClassName
  private static final String VENUE_SCHEDULE_KEY_PATTERN = "venue:schedule:%d:%d"; // venueId:scheduleId

  public void initSeatStatuses(Long scheduleId, Long venueId) {
    log.info("좌석 상태 초기화 시작: scheduleId={}, venueId={}", scheduleId, venueId);
    
    try {
      // 1. venue-schedule 매핑 저장
      String venueScheduleKey = String.format(VENUE_SCHEDULE_KEY_PATTERN, venueId, scheduleId);
      redis.opsForValue().set(venueScheduleKey, "active", Duration.ofDays(30));
      
      // 2. venueId로 모든 좌석 조회
      List<Seat> seats = seatRepository.findByVenueId(venueId);
      if (seats.isEmpty()) {
        log.warn("해당 venue에 좌석이 없습니다: venueId={}", venueId);
        return;
      }
      
      // 3. seatClassId별로 그룹화
      Map<Long, List<Seat>> seatsByClass = seats.stream()
          .collect(Collectors.groupingBy(Seat::getSeatClassId));
      
      // 4. 각 seatClass별로 Redis에 저장
      for (Map.Entry<Long, List<Seat>> entry : seatsByClass.entrySet()) {
        Long seatClassId = entry.getKey();
        List<Seat> classSeats = entry.getValue();
        
        // seatClass 정보 조회
        Optional<SeatClass> seatClassOpt = seatClassRepository.findById(seatClassId);
        if (seatClassOpt.isEmpty()) {
          log.warn("SeatClass를 찾을 수 없습니다: seatClassId={}", seatClassId);
          continue;
        }
        
        String className = seatClassOpt.get().getClassName();
        String classKey = String.format(SEAT_CLASS_STATUS_KEY_PATTERN, scheduleId, className);
        
        // 이미 초기화된 경우 스킵
        Long existingSize = redis.opsForHash().size(classKey);
        if (existingSize != null && existingSize > 0) {
          log.info("좌석 클래스 상태가 이미 초기화됨: scheduleId={}, className={}, existingSeats={}", 
                   scheduleId, className, existingSize);
          continue;
        }
        
        // 좌석 상태 초기화
        Map<String, String> initMap = new LinkedHashMap<>();
        for (Seat seat : classSeats) {
          initMap.put(String.valueOf(seat.getSeatId()), "");
        }
        
        redis.opsForHash().putAll(classKey, initMap);
        
        log.info("좌석 클래스 상태 초기화 완료: scheduleId={}, className={}, seats={}", 
                 scheduleId, className, classSeats.size());
      }
      
      log.info("전체 좌석 상태 초기화 완료: scheduleId={}, venueId={}, totalSeats={}", 
               scheduleId, venueId, seats.size());
    } catch (Exception e) {
      log.error("좌석 상태 초기화 실패: scheduleId={}, venueId={}, error={}", 
                scheduleId, venueId, e.getMessage(), e);
      throw new RuntimeException("좌석 상태 초기화에 실패했습니다: " + e.getMessage(), e);
    }
  }
  
  // 하위 호환성을 위한 기존 메서드 유지
  @Deprecated
  public void initSeatStatuses(Long scheduleId) {
    log.warn("Deprecated method called. Please use initSeatStatuses(Long scheduleId, Long venueId)");
    
    try {
      String key = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);

      Long existingSize = redis.opsForHash().size(key);
      if (existingSize != null && existingSize > 0) {
        log.info("좌석 상태가 이미 초기화됨: scheduleId={}, existingSeats={}", scheduleId, existingSize);
        return;
      }

      Map<String, String> initMap = new LinkedHashMap<>(TOTAL_SEATS);
      for (int i = 1; i <= TOTAL_SEATS; i++) {
        initMap.put(String.valueOf(i), "");
      }

      redis.opsForHash().putAll(key, initMap);

      log.info("좌석 상태 초기화 완료: scheduleId={}, seats={}", scheduleId, TOTAL_SEATS);
    } catch (Exception e) {
      log.error("좌석 상태 초기화 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      throw new RuntimeException("좌석 상태 초기화에 실패했습니다: " + e.getMessage(), e);
    }
  }

  public void holdSeat(Long scheduleId, Integer seatId, Long userId, long ttlSeconds) {
    if (seatId < 1 || seatId > TOTAL_SEATS) {
      throw new IllegalArgumentException("좌석 번호는 1~120 사이여야 합니다: " + seatId);
    }

    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);

    redis.opsForValue().set(holdKey, String.valueOf(userId), ttlSeconds, TimeUnit.SECONDS);

    log.info("좌석 홀드 설정: scheduleId={}, seatId={}, userId={}, ttl={}초", scheduleId, seatId, userId,
        ttlSeconds);
  }

  public void releaseSeatHold(Long scheduleId, Integer seatId) {
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);
    redis.delete(holdKey);

    log.info("좌석 홀드 해제: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public void confirmSeat(Long scheduleId, Integer seatId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);

    redis.delete(holdKey);

    redis.opsForHash().put(statusKey, String.valueOf(seatId), "BOOKED");
    
    // 새로운 구조에도 업데이트
    try {
      updateSeatStatusByClassName(scheduleId, Long.valueOf(seatId), "BOOKED");
    } catch (Exception e) {
      log.warn("새로운 구조 좌석 상태 업데이트 실패: scheduleId={}, seatId={}, error={}", 
               scheduleId, seatId, e.getMessage());
    }

    log.info("좌석 예약 확정: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public void blockSeat(Long scheduleId, Integer seatId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    redis.opsForHash().put(statusKey, String.valueOf(seatId), "BLOCKED");
    
    // 새로운 구조에도 업데이트
    try {
      updateSeatStatusByClassName(scheduleId, Long.valueOf(seatId), "BLOCKED");
    } catch (Exception e) {
      log.warn("새로운 구조 좌석 상태 업데이트 실패: scheduleId={}, seatId={}, error={}", 
               scheduleId, seatId, e.getMessage());
    }

    log.info("좌석 블록: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public void clearSeatStatus(Long scheduleId, Integer seatId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);
    
    redis.delete(holdKey);
    
    redis.opsForHash().put(statusKey, String.valueOf(seatId), "");
    
    // 새로운 구조에도 업데이트
    try {
      updateSeatStatusByClassName(scheduleId, Long.valueOf(seatId), "");
    } catch (Exception e) {
      log.warn("새로운 구조 좌석 상태 업데이트 실패: scheduleId={}, seatId={}, error={}", 
               scheduleId, seatId, e.getMessage());
    }
    
    log.info("좌석 상태 초기화: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public void cancelSeat(Long scheduleId, Integer seatId) {
    clearSeatStatus(scheduleId, seatId);
    log.info("좌석 예매 취소: scheduleId={}, seatId={}", scheduleId, seatId);
  }

  public String getSeatStatus(Long scheduleId, Integer seatId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, seatId);

    String holdValue = redis.opsForValue().get(holdKey);
    if (holdValue != null) {
      return "HELD";
    }

    Object status = redis.opsForHash().get(statusKey, String.valueOf(seatId));
    return status != null ? (String) status : "";
  }

  public Map<String, String> getAllSeatStatuses(Long scheduleId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);
    Map<Object, Object> statusMap = redis.opsForHash().entries(statusKey);

    Map<String, String> result = new LinkedHashMap<>();

    for (int i = 1; i <= TOTAL_SEATS; i++) {
      String seatId = String.valueOf(i);
      String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, i);

      String holdValue = redis.opsForValue().get(holdKey);
      if (holdValue != null) {
        result.put(seatId, "HELD");
      } else {
        Object status = statusMap.get(seatId);
        result.put(seatId, status != null ? (String) status : "");
      }
    }

    return result;
  }

  public void deleteSeatStatuses(Long scheduleId) {
    String statusKey = String.format(SEAT_STATUS_KEY_PATTERN, scheduleId);

    for (int i = 1; i <= TOTAL_SEATS; i++) {
      String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, i);
      redis.delete(holdKey);
    }

    redis.delete(statusKey);

    log.info("좌석 상태 삭제 완료: scheduleId={}", scheduleId);
  }
  
  // 새로운 메서드들 - seatClassName 기반
  
  /**
   * 전체 좌석 상태를 seatClassName별로 조회
   * @param scheduleId 스케줄 ID
   * @return Map<String, Map<String, String>> - seatClassName -> {seatId -> status}
   */
  public Map<String, Map<String, String>> getAllSeatStatusesBySeatClass(Long scheduleId) {
    log.info("좌석 클래스별 전체 상태 조회: scheduleId={}", scheduleId);
    
    try {
      Map<String, Map<String, String>> result = new LinkedHashMap<>();
      
      // 모든 seatClass 조회
      List<SeatClass> seatClasses = seatClassRepository.findAll();
      
      for (SeatClass seatClass : seatClasses) {
        String className = seatClass.getClassName();
        String classKey = String.format(SEAT_CLASS_STATUS_KEY_PATTERN, scheduleId, className);
        
        Map<Object, Object> statusMap = redis.opsForHash().entries(classKey);
        if (!statusMap.isEmpty()) {
          Map<String, String> classStatuses = new LinkedHashMap<>();
          for (Map.Entry<Object, Object> entry : statusMap.entrySet()) {
            String seatId = (String) entry.getKey();
            String status = (String) entry.getValue();
            
            // hold 상태 확인
            String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, Integer.parseInt(seatId));
            String holdValue = redis.opsForValue().get(holdKey);
            if (holdValue != null) {
              classStatuses.put(seatId, "HELD");
            } else {
              classStatuses.put(seatId, status != null ? status : "");
            }
          }
          result.put(className, classStatuses);
        }
      }
      
      return result;
    } catch (Exception e) {
      log.error("좌석 클래스별 상태 조회 실패: scheduleId={}, error={}", scheduleId, e.getMessage(), e);
      throw new RuntimeException("좌석 클래스별 상태 조회에 실패했습니다: " + e.getMessage(), e);
    }
  }
  
  /**
   * 특정 seatClassName의 좌석 상태 조회
   * @param scheduleId 스케줄 ID
   * @param seatClassName 좌석 클래스명
   * @return Map<String, String> - {seatId -> status}
   */
  public Map<String, String> getSeatStatusesBySeatClassName(Long scheduleId, String seatClassName) {
    log.info("좌석 클래스별 상태 조회: scheduleId={}, seatClassName={}", scheduleId, seatClassName);
    
    try {
      String classKey = String.format(SEAT_CLASS_STATUS_KEY_PATTERN, scheduleId, seatClassName);
      Map<Object, Object> statusMap = redis.opsForHash().entries(classKey);
      
      Map<String, String> result = new LinkedHashMap<>();
      for (Map.Entry<Object, Object> entry : statusMap.entrySet()) {
        String seatId = (String) entry.getKey();
        String status = (String) entry.getValue();
        
        // hold 상태 확인
        String holdKey = String.format(SEAT_HOLD_KEY_PATTERN, scheduleId, Integer.parseInt(seatId));
        String holdValue = redis.opsForValue().get(holdKey);
        if (holdValue != null) {
          result.put(seatId, "HELD");
        } else {
          result.put(seatId, status != null ? status : "");
        }
      }
      
      return result;
    } catch (Exception e) {
      log.error("좌석 클래스별 상태 조회 실패: scheduleId={}, seatClassName={}, error={}", 
                scheduleId, seatClassName, e.getMessage(), e);
      throw new RuntimeException("좌석 클래스별 상태 조회에 실패했습니다: " + e.getMessage(), e);
    }
  }
  
  /**
   * seatClassName 기반으로 좌석 상태 업데이트
   * @param scheduleId 스케줄 ID
   * @param seatId 좌석 ID
   * @param status 상태
   */
  public void updateSeatStatusByClassName(Long scheduleId, Long seatId, String status) {
    try {
      // seatId로 seatClass 정보 조회
      List<Seat> seats = seatRepository.findByVenueId(getVenueIdByScheduleId(scheduleId));
      Optional<Seat> seatOpt = seats.stream()
          .filter(seat -> seat.getSeatId().equals(seatId))
          .findFirst();
      
      if (seatOpt.isEmpty()) {
        throw new IllegalArgumentException("좌석을 찾을 수 없습니다: seatId=" + seatId);
      }
      
      Seat seat = seatOpt.get();
      Optional<SeatClass> seatClassOpt = seatClassRepository.findById(seat.getSeatClassId());
      if (seatClassOpt.isEmpty()) {
        throw new IllegalArgumentException("좌석 클래스를 찾을 수 없습니다: seatClassId=" + seat.getSeatClassId());
      }
      
      String className = seatClassOpt.get().getClassName();
      String classKey = String.format(SEAT_CLASS_STATUS_KEY_PATTERN, scheduleId, className);
      
      redis.opsForHash().put(classKey, String.valueOf(seatId), status);
      
      log.info("좌석 상태 업데이트: scheduleId={}, seatId={}, className={}, status={}", 
               scheduleId, seatId, className, status);
    } catch (Exception e) {
      log.error("좌석 상태 업데이트 실패: scheduleId={}, seatId={}, status={}, error={}", 
                scheduleId, seatId, status, e.getMessage(), e);
      throw new RuntimeException("좌석 상태 업데이트에 실패했습니다: " + e.getMessage(), e);
    }
  }
  
  /**
   * scheduleId로 venueId 조회 (Redis에서)
   */
  private Long getVenueIdByScheduleId(Long scheduleId) {
    // Redis에서 venue-schedule 매핑 정보를 찾기 위해 패턴 검색
    Set<String> keys = redis.keys(String.format("venue:schedule:*:%d", scheduleId));
    if (keys.isEmpty()) {
      throw new IllegalArgumentException("해당 스케줄의 venue 정보를 찾을 수 없습니다: scheduleId=" + scheduleId);
    }
    
    String key = keys.iterator().next();
    String[] parts = key.split(":");
    return Long.parseLong(parts[2]); // venue:schedule:venueId:scheduleId
  }
}
