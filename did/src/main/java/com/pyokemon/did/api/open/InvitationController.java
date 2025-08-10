package com.pyokemon.did.api.open;

import com.pyokemon.did.service.EventInvitationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pyokemon.did.remote.mediator.dto.response.CreateMediatorInvitationResponse;
import com.pyokemon.did.service.EventInvitationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/invitations")
@AllArgsConstructor
public class InvitationController {

    private final EventInvitationService eventInvitationService;

    @PostMapping("/mediator")
    public ResponseEntity<CreateMediatorInvitationResponse> createMediatorInvitation(
            @RequestHeader("X-Auth-AccountId") Long accountId,
            @RequestHeader("X-Auth-Role") String role) {

        CreateMediatorInvitationResponse response = eventInvitationService.getMediatorInvitation();
        return ResponseEntity.ok(response);
        
    }

}
