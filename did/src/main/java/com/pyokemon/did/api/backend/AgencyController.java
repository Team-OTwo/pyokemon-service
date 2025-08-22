package com.pyokemon.did.api.backend;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.did.domain.dto.request.EventInvitationRequest.CreateEventInvitationRequest;
import com.pyokemon.did.domain.dto.request.WalletMetadataRequest.CreateWalletRequest;
import com.pyokemon.did.service.EventInvitationService;
import com.pyokemon.did.service.WalletMetadataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/backend/agency")
@RequiredArgsConstructor
public class AgencyController {
    private final WalletMetadataService walletMetadataService;
    private final EventInvitationService eventInvitationService;

    /**
     * 테넌트를 위한 지갑 프로비저닝 및 메타데이터 등록
     *
     * @param requestDto 지갑 프로비저닝 요청
     * @return 성공 상태가 포함된 응답
     */
    @PostMapping("/wallet")
    public ResponseEntity<ResponseDto<Void>> createWallet(@RequestBody @Valid CreateWalletRequest requestDto) {
        walletMetadataService.createWallet(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseDto.success("OK"));
    }

    /**
     * 초대장 프로비저닝 및 이벤트 초대장 등록
     *
     * @param requestDto 초대장 프로비저닝 요청
     * @return 성공 상태가 포함된 응답
     */
    @PostMapping("/event-invitation")
    public ResponseEntity<ResponseDto<Void>> createEventInvitation(@RequestBody @Valid CreateEventInvitationRequest requestDto) {
        eventInvitationService.createEventInvitation(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseDto.success("OK"));
    }
}
