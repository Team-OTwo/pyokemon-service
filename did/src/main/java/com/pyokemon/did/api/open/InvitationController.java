package com.pyokemon.did.api.open;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.did.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.did.domain.dto.response.InvitationResponse;
import com.pyokemon.did.service.DeviceConnectionService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/invitations")
@RestController
@AllArgsConstructor
public class InvitationController {

  private final DeviceConnectionService deviceConnectionService;

  @PostMapping
  public ResponseEntity<ResponseDto<InvitationResponse>> createInvitations() {

    // Gateway에서 전달받은 헤더 정보 추출
    Long userId = GatewayRequestHeaderUtils.getUserIdOrThrowException();
    String deviceId = GatewayRequestHeaderUtils.getUserDeviceOrThrowException();
    log.info("초대장 생성 요청: userId={}", userId);

    InvitationResponse invitations = deviceConnectionService.createInvitations(userId);

    log.info("invitations={}", invitations);
    // 비즈니스 로직
    return ResponseEntity.ok(ResponseDto.success(invitations));
  }
}
