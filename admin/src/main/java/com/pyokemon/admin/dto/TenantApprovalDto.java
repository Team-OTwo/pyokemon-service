package com.pyokemon.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantApprovalDto {
    private Long id;
    private String did;
    private String companyName;
    private String businessNumber;
    private String contactEmail;
    private String contactPhone;
    private String status;
    private String approvalReason;
} 