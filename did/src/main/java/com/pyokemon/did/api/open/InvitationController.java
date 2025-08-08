package com.pyokemon.did.controller;

import com.pyokemon.did.remote.tenant.dto.response.CreateTenantInvitationResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.did.remote.mediator.dto.response.CreateMediatorInvitationResponse;
import com.pyokemon.did.service.InvitationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/invitations")
@AllArgsConstructor
public class InvitationController {
    
    private final InvitationService invitationService;

    @PostMapping("/mediator")
    public ResponseEntity<CreateMediatorInvitationResponse> createMediatorInvitation(
            @RequestHeader("X-Auth-AccountId") Long accountId,
            @RequestHeader("X-Auth-Role") String role) {

        CreateMediatorInvitationResponse response = invitationService.getMediatorInvitation();
        return ResponseEntity.ok(response);
        
    }

    @PostMapping("/tenant")
    public ResponseEntity<CreateTenantInvitationResponse> createTenantInvitation(
            @RequestHeader("X-Auth-AccountId") Long accountId,
            @RequestHeader("X-Auth-Role") String role) {

        CreateTenantInvitationResponse response = invitationService.getTenantInvitation();
        return ResponseEntity.ok(response);
    }
}
