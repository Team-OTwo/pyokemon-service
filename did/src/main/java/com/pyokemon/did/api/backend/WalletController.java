package com.pyokemon.did.api.backend;

import com.pyokemon.common.dto.ResponseDto;
import com.pyokemon.did.dto.request.ProvisionWalletRequest;
import com.pyokemon.did.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 지갑 관리 작업을 위한 REST 컨트롤러
 */
@RestController
@Slf4j
@RequestMapping("/backend/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    /**
     * 테넌트를 위한 지갑 프로비저닝 및 메타데이터 등록
     *
     * @param requestDto 지갑 프로비저닝 요청
     * @return 성공 상태가 포함된 응답
     */
    @PostMapping
    public ResponseEntity<ResponseDto<Void>> provisionWallet(@RequestBody @Valid ProvisionWalletRequest requestDto) {
        walletService.provisionWallet(requestDto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ResponseDto.success("OK"));
    }
}
