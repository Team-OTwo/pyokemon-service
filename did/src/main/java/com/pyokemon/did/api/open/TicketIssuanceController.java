package com.pyokemon.did.api.open;

import com.pyokemon.did.domain.dto.request.TenantInvitationRequest.CreateTenantInvitationRequest;
import com.pyokemon.did.domain.dto.response.TenantInvitationResponse.CreateTenantInvitationResponse;
import com.pyokemon.did.service.TenantInvitationService;
import com.pyokemon.common.dto.ResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class TicketIssuanceController {

    private final TenantInvitationService tenantInvitationService;

    /**
     * 테넌트 초대장 생성 API
     * 기존 connection이 없는 tenant에 대해서만 새로운 invitation URL 생성
     * 
     * @param request 초대장 생성 요청 (사용자 ID, 디바이스 ID, 테넌트 ID 목록)
     * @return 새로 생성된 초대장 목록 (기존 connection이 있는 tenant는 제외)
     */
    @PostMapping("/invitations")
    public ResponseEntity<ResponseDto<CreateTenantInvitationResponse>> createInvitation(
            @Valid @RequestBody CreateTenantInvitationRequest request) {
        
        log.info("테넌트 초대장 생성 요청: userId={}, deviceId={}, tenantIds={}", 
            request.getUserId(), request.getDeviceId(), request.getTenantIds());
        
        CreateTenantInvitationResponse response = tenantInvitationService.createTenantInvitation(request);
        
        log.info("테넌트 초대장 생성 완료: 생성된 초대장 수={}", 
            response.getInvitations() != null ? response.getInvitations().size() : 0);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ResponseDto.success(response, "테넌트 초대장 생성 완료"));
    }
}
