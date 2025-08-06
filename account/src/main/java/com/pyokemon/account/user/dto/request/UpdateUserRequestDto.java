package com.pyokemon.account.user.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
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
public class UpdateUserRequestDto {

  @NotBlank(message = "이름은 필수입니다.")
  private String name;

  @NotBlank(message = "전화번호는 필수입니다.")
  @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
  private String phone;

  @NotNull(message = "생년월일은 필수입니다.")
  @Past(message = "생년월일은 과거 날짜여야 합니다.")
  private LocalDate birth;
}
