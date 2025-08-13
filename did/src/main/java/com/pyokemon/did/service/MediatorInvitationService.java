package com.pyokemon.did.service;

import com.pyokemon.did.domain.dto.response.MediatorInvitationResponse;
import com.pyokemon.did.domain.dto.response.MediatorInvitationResponse.CreateMediatorInvitationResponse;
import com.pyokemon.did.remote.mediatoracapy.dto.response.InvitationResponse;
import com.pyokemon.did.remote.mediatoracapy.dto.response.InvitationResponse.AcaPyCreateMediatorInvitationResponse;

public interface MediatorInvitationService {

    CreateMediatorInvitationResponse getMediatorInvitation();
}
