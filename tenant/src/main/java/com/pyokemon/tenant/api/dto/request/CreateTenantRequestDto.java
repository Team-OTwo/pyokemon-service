package com.pyokemon.tenant.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateTenantRequestDto {

  @NotBlank(message = "대표자명은 필수입니다")
  private String ceoName;

  @NotBlank(message = "사업자번호는 필수입니다")
  private String businessNumber;

  @NotBlank(message = "회사명은 필수입니다")
  private String corpName;

  @NotBlank(message = "사업자번호는 필수입니다")
  private String corpId;

  @NotBlank(message = "주소는 필수입니다")
  private String city;

  @NotBlank(message = "주소는 필수입니다")
  private String street;

  @NotBlank(message = "주소는 필수입니다")
  private String zipcode;

  @NotBlank(message = "아이디는 필수입니다")
  private String loginId;

  @NotBlank(message = "비밀번호는 필수입니다")
  private String password;
}
