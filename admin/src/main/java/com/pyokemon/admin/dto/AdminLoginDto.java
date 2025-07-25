package com.pyokemon.admin.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class AdminLoginDto {
    
    @NotBlank(message = "아이디는 필수입니다")
    private String adminId;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
} 