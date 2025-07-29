package com.pyokemon.account.tenant.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegisterRequestDto {

  @NotBlank(message = "로그인 ID는 필수입니다.")
  private String loginId;

  @NotBlank(message = "비밀번호는 필수입니다.")
  private String password;

  @NotBlank(message = "테넌트명은 필수입니다.")
  private String name;

  @NotBlank(message = "사업자번호는 필수입니다.")
  private String corpId;

  @NotBlank(message = "시/도는 필수입니다.")
  private String city;

  @NotBlank(message = "도로명은 필수입니다.")
  private String street;

  @NotBlank(message = "우편번호는 필수입니다.")
  private String zipcode;

  @NotBlank(message = "대표명은 필수입니다.")
  private String ceo;
}
