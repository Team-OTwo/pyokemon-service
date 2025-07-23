package com.pyokemon.tenant.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordRequestDto {

    @NotBlank(message = "현재 비밀번호를 입력하세요")
    private String currentPassword;

    @NotBlank(message = "새로운 비밀번호를 입력하세요")
    private String newPassword;

}
