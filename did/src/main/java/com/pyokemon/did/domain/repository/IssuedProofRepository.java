package com.pyokemon.did.domain.repository;

import org.springframework.data.repository.CrudRepository;

import com.pyokemon.did.domain.IssuedProof;

public interface IssuedProofRepository extends CrudRepository<IssuedProof, String> {
}
