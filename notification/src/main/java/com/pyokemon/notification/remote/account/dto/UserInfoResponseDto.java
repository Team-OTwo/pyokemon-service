package com.pyokemon.notification.remote.account.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDto {
    private String name;
    private String fcmToken;
    private Boolean isLogin;
}
