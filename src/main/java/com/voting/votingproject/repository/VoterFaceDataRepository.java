package com.voting.votingproject.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.voting.votingproject.model.VoterFaceData;

import java.util.Optional;

public interface VoterFaceDataRepository
        extends MongoRepository<VoterFaceData, String> {

    boolean existsByEpicNo(String epicNo);

    Optional<VoterFaceData> findByEpicNo(String epicNo);

    void deleteByEpicNo(String epicNo);
}
