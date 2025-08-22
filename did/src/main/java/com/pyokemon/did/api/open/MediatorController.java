package com.pyokemon.did.api.open;

import com.pyokemon.did.domain.dto.response.MediatorInvitationResponse.CreateMediatorInvitationResponse;
import com.pyokemon.did.service.MediatorInvitationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/mediator")
@AllArgsConstructor
public class MediatorController {

    private final MediatorInvitationService mediatorInvitationService;

    @PostMapping("/invitations")
    public ResponseEntity<CreateMediatorInvitationResponse> createMediatorInvitation() {
        CreateMediatorInvitationResponse response = mediatorInvitationService.getMediatorInvitation();
        return ResponseEntity.ok(response);
    }
}
