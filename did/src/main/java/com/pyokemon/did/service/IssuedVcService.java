package com.pyokemon.did.service;

public interface IssuedVcService {

    public void issueVC(Long userId,Long tenantId,Long bookingId);
    
    public Boolean isIssuedVC(Long bookingId);
}
