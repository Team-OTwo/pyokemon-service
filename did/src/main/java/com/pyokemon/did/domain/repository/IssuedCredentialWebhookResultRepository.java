package com.pyokemon.did.domain.repository;

import com.pyokemon.did.domain.IssuedCredentialWebhookResult;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IssuedCredentialWebhookResultRepository extends CrudRepository<IssuedCredentialWebhookResult, Long> {

    Optional<IssuedCredentialWebhookResult> findByCredExId(String credExId);
}


