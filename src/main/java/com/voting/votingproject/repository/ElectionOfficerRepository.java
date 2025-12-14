package com.voting.votingproject.repository;

import com.voting.votingproject.model.ElectionOfficer;
import com.voting.votingproject.model.Voter;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ElectionOfficerRepository extends MongoRepository<ElectionOfficer, String> {
    ElectionOfficer findByUsername(String username);
    ElectionOfficer findByEmailId(String emailId);
}