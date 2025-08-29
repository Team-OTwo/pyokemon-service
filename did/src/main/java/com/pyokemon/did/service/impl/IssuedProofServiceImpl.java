package com.pyokemon.did.service.impl;

import com.pyokemon.common.exception.BusinessException;
import com.pyokemon.did.domain.IssuedProof;
import com.pyokemon.did.domain.repository.IssuedProofRepository;
import com.pyokemon.did.service.IssuedProofService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.pyokemon.common.exception.code.DidErrorCodes.VP_VERIFICATION_FAILED;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IssuedProofServiceImpl implements IssuedProofService {

    IssuedProofRepository issuedProofRepository;

    @Override
    public void revokeIssuedProof(String presExId) {
        issuedProofRepository.deleteByPresExId(presExId);
    }

    @Override
    public String getChallenge(String presExId) {
        IssuedProof issuedProof = issuedProofRepository.findByPresExId(presExId).orElseThrow(() -> new BusinessException("VP 요청을 찾을 수 없습니다", VP_VERIFICATION_FAILED));
        return issuedProof.getChallenge();
    }

}
