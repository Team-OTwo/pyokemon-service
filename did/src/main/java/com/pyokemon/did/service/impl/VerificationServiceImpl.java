package com.pyokemon.did.service.impl;


import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import com.pyokemon.did.domain.repository.VerificationRepository;
import com.pyokemon.did.remote.acapy.common.dto.request.JwtVerifyRequest;
import com.pyokemon.did.remote.acapy.common.dto.response.JwtVerifyResponse;
import com.pyokemon.did.remote.acapy.service.RemoteTenantAcaPyService;
import com.pyokemon.did.service.DeviceConnectionService;
import com.pyokemon.did.service.IssuedVcService;
import com.pyokemon.did.service.VerificationService;
import com.pyokemon.did.service.WalletService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.pyokemon.common.exception.code.DidErrorCodes.ACAPY_SERVICE_ERROR;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final RemoteTenantAcaPyService remoteTenantAcaPyService;

    private final WalletService walletService;
    private final DeviceConnectionService deviceConnectionService;
    private final IssuedVcService issuedVcService;

    @Override
    public CreateVerificationResponse createVerificationUrl(CreateVerificationRequest request, Long tenantId) {

        Long bookingId = request.getBooking_id();
        JwtVerifyRequest jwtVerifyRequest = JwtVerifyRequest.of(request.getJwt());
        String authorization = walletService.getWalletToken(tenantId);

        //Jwt 서명 검증 - admin API 호출
        JwtVerifyResponse jwtVerifyResponse;
        String credoPublicDid;
        try{
            jwtVerifyResponse = remoteTenantAcaPyService.jwtVerify(authorization, jwtVerifyRequest);
            if (jwtVerifyResponse == null || jwtVerifyResponse.getPayload() == null || jwtVerifyResponse.getPayload().getDid() == null) {
                throw new RuntimeException("JWT 검증 응답이 유효하지 않습니다.");
            }
            credoPublicDid = jwtVerifyResponse.getPayload().getDid();
        }
        catch (FeignException e){
            log.error("ACA-Py JWT 검증 API 호출 실패. Status: {}, Body: {}", e.status(), e.contentUTF8());
            throw new BusinessException("JWT 서명 검증에 실패했습니다: " + e.getMessage(), ACAPY_SERVICE_ERROR);
        }

        //invi_url, pres_ex_id 조회
        Long userId = deviceConnectionService.getUserIdByDidOrThrow(credoPublicDid);
        Map<String, String> stringMap = issuedVcService.sendVerifiyInviUrlOrThrow(userId, tenantId, bookingId);
        return new CreateVerificationResponse(stringMap.get("verifyInviUrl"), stringMap.get("presExId"));
    }
    }
