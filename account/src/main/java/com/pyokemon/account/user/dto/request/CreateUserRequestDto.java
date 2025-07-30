package com.pyokemon.account.user.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.*;

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
public class CreateUserRequestDto {

  @NotBlank(message = "로그인 ID는 필수입니다.")
  @Size(min = 4, max = 20, message = "로그인 ID는 4~20자여야 합니다.")
  private String loginId;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
      message = "비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
  private String password;

  @NotBlank(message = "이름은 필수입니다.")
  private String name;

  @NotBlank(message = "전화번호는 필수입니다.")
  @Pattern(
          regexp = "^\\d{3}-\\d{3,4}-\\d{4}$",
          message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)"
  )
  private String phone;

  @NotNull(message = "생년월일은 필수입니다.")
  @Past(message = "생년월일은 과거 날짜여야 합니다.")
  private LocalDate birth;
}
