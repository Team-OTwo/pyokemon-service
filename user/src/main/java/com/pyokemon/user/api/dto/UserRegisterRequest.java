package com.pyokemon.user.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.pyokemon.user.api.validation.ValidBirthDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {

    @NotBlank(message = "이름은 필수입니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,20}$", 
            message = "이름은 2~20자의 한글, 영문, 숫자만 가능합니다")
    private String name;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>])[\\S]{8,20}$",
            message = "비밀번호는 8자 이상 20자 이하이며, 숫자와 특수문자를 포함해야 합니다")
    private String password;

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
            message = "올바른 전화번호 형식이 아닙니다")
    private String phone;

    @NotNull(message = "생년월일은 필수입니다")
    @ValidBirthDate
    private LocalDate birth;
} 