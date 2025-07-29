package com.pyokemon.account.user.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

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
public class UserRegisterRequestDto {

  @NotBlank(message = "로그인 ID는 필수입니다.")
  private String loginId;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = "비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
  private String password;

  @NotBlank(message = "이름은 필수입니다.")
  private String name;

  @NotBlank(message = "전화번호는 필수입니다.")
  @Pattern(regexp = "^[0-9]{10,11}$", message = "전화번호는 10-11자리 숫자여야 합니다.")
  private String phone;

  @NotNull(message = "생년월일은 필수입니다.")
  private LocalDate birth;
}
