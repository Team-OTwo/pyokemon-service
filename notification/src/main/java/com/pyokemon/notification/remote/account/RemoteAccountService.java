package com.pyokemon.notification.remote.account;


import com.pyokemon.notification.remote.account.dto.UserInfoResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "remote-account-service",
        url = "${account.service.base-url}"
)
public interface RemoteAccountService {

    @GetMapping("/api/users/notification")
    public UserInfoResponseDto getUserInfo(
        @RequestParam(value = "accountId", required = true) Long accountId);
}
