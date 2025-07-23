package com.pyokemon.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequestDto {
    
    @NotBlank(message = "테넌트명은 필수입니다")
    private String tenantName;
    
    @NotBlank(message = "주소는 필수입니다")
    private String address;
    
    @NotBlank(message = "대표자명은 필수입니다")
    private String ownerName;
    
    @NotBlank(message = "사업자번호는 필수입니다")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자번호 형식이 올바르지 않습니다 (예: 123-45-67890)")
    private String businessNumber;
    
    @NotBlank(message = "회사명은 필수입니다")
    private String companyName;
    
    @NotBlank(message = "아이디는 필수입니다")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문, 숫자 4-20자여야 합니다")
    private String loginId;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
} 