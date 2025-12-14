package com.voting.votingproject.repository;

import com.voting.votingproject.model.Voter;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VoterRepository extends MongoRepository<Voter, String> {
    Voter findByEpicNo(String epicNo);
    boolean existsByAadharNo(String aadharNo);

    boolean existsByEpicNo(String epicNo);
    
}