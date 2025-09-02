package com.pyokemon.did.api.open;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.common.web.context.GatewayRequestHeaderUtils;
import com.pyokemon.did.domain.dto.response.InvitationResponse;
import com.pyokemon.did.service.DeviceConnectionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/invitations")
@RestController
@AllArgsConstructor
@Tag(name = "초대장 관리", description = "ACA-Py 초대장 관련 API")
public class InvitationController {

  private final DeviceConnectionService deviceConnectionService;

  @Operation(summary = "초대장 생성", description = "user, mediator acapy 연결을 위한 invitation_url을 반환해줍니다")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "초대장 생성 성공",
          content = @Content(schema = @Schema(implementation = InvitationResponse.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "401", description = "인증 실패"),
      @ApiResponse(responseCode = "500", description = "서버 오류")})
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
