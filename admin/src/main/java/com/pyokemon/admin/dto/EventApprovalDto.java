package com.pyokemon.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이벤트 승인 관련 요청 및 응답을 위한 통합 DTO 클래스
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventApprovalDto {
  // === 요청 및 응답 공통 필드 ===
  private Long id;
  // 승인/거절 사유 (요청 시 사용)
  private String reason;
  // 공연 상태 (응답 시 사용) PENDING, APPROVED, REJECTED 중 하나
  private String status;
}
