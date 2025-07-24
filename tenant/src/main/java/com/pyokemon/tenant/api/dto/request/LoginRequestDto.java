package com.pyokemon.tenant.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequestDto {

  @NotBlank(message = "아이디는 필수입니다")
  private String loginId;

  @NotBlank(message = "비밀번호는 필수입니다")
  private String password;

}
