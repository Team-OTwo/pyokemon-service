package com.pyokemon.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PerformanceApprovalDto {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private String title;
    private String description;
    private String venue;
    private LocalDateTime performanceDate;
    private BigDecimal ticketPrice;
    private Integer totalSeats;
    private String status;
    private String approvalReason;
} 