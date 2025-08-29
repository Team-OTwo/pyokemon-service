package com.pyokemon.did.service;

public interface IssuedProofService {

    /**
     * 발급된 증명을 취소합니다.
     * 
     * @param presExId 취소할 증명 제시 ID
     */
    public void revokeIssuedProof(String presExId);
    
    /**
     * 증명 제시 ID로 챌린지 값을 조회합니다.
     * 
     * @param presExId 조회할 증명 제시 ID
     * @return 챌린지 값
     */
    public String getChallenge(String presExId);

}
