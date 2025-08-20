package com.pyokemon.account.user.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationDto {
    private String name;
    private String fcmToken;
    private Boolean isLogin;
}
