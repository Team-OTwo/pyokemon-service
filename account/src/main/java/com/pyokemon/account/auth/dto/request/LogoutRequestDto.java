package com.pyokemon.account.auth.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequestDto {
    private String deviceNumber;
}
