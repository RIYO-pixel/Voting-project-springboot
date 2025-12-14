package com.voting.votingproject.service;

import com.voting.votingproject.model.Voter;
import com.voting.votingproject.repository.VoterFaceDataRepository;
import com.voting.votingproject.repository.VoterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VoterService {

    @Autowired
    private VoterRepository repository;

    @Autowired
    private VoterFaceDataRepository voterFaceDataRepository;

    @Autowired
    private VoterRepository voterRepository;


    public HashMap<String, Object> verifyEpic(HashMap<String, String> data) {
        HashMap<String, Object> response = new HashMap<>();

        try {
            String epicNo = data.get("epicNo");
            String constituencyNo = data.get("constituencyNo");
            String partNo = data.get("partNo");

            // 1. Validate input
            if (epicNo == null || epicNo.isBlank()
                    || constituencyNo == null || constituencyNo.isBlank()
                    || partNo == null || partNo.isBlank()) {

                response.put("status", false);
                response.put("message", "epicNo, constituencyNo and partNo are required");
                return response;
            }

            // 2. Check if voter exists
            Optional<Voter> optionalVoter = voterRepository.findById(epicNo);
            if (optionalVoter.isEmpty()) {
                response.put("status", false);
                response.put("message", "Voter not found for given EPIC number");
                return response;
            }

            Voter voter = optionalVoter.get();

            // 3. Check constituency and part number match
            if (!constituencyNo.equals(voter.getConstituencyNo())
                    || !partNo.equals(voter.getPartNo())) {
                response.put("status", false);
                response.put("message", "Constituency number or Part number does not match");
                return response;
            }

            // 4. Check if voter has already voted
            if (voter.isHasVoted()) {
                response.put("status", false);
                response.put("message", "Voter has already voted");
                return response;
            }

            // 5. All good → return voter details
            response.put("status", true);
            response.put("message", "Voter verified successfully");
            response.put("voter", voter);
            return response;

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "An error occurred while verifying voter: " + e.getMessage());
            return response;
        }
    }


    

    public Voter addVoter(Voter voter) {

    
    if (repository.existsById(voter.getEpicNo())) {
        throw new IllegalArgumentException("Voter already exists with EPIC No: " + voter.getEpicNo());
    }

   
    if (repository.existsByAadharNo(voter.getAadharNo())) {
        throw new IllegalArgumentException("Aadhaar number already exists");
    }

    try {
        return repository.save(voter);
    } catch (Exception e) {
        throw new RuntimeException("Failed to save voter: " + e.getMessage());
    }
}


    public Voter getVoterByEpic(String epicNo) {

    return repository.findById(epicNo)
            .orElseThrow(() ->
                    new IllegalArgumentException("No voter found with EPIC No: " + epicNo)
            );
}


   public List<Voter> getAllVoters() {

    List<Voter> voters = repository.findAll();

    if (voters.isEmpty()) {
        throw new IllegalStateException("No voters found in the system");
    }

    return voters;
}

public Voter updateVoter(String epicNo, Voter updatedVoter) {

    Voter existing = repository.findById(epicNo)
            .orElseThrow(() -> new IllegalArgumentException("Voter not found with EPIC No: " + epicNo));

    existing.setName(updatedVoter.getName());
    existing.setAge(updatedVoter.getAge());
    existing.setPartNo(updatedVoter.getPartNo());
    existing.setConstituencyNo(updatedVoter.getConstituencyNo());
    existing.setAadharNo(updatedVoter.getAadharNo());
    existing.setHasVoted(updatedVoter.isHasVoted());

    return repository.save(existing);
}

public void deleteVoter(String epicNo) {

    Voter voter = repository.findById(epicNo)
            .orElseThrow(() -> new IllegalArgumentException("Voter not found with EPIC No: " + epicNo));

    
    // ✅ DELETE FACE DATA FIRST
    voterFaceDataRepository.deleteByEpicNo(voter.getEpicNo());

    // ✅ DELETE VOTER
    repository.delete(voter);
}
public Map<String, Object> updateVoter(String epicNo, Map<String, Object> updates) {
        Map<String, Object> response = new HashMap<>();

        if (epicNo == null || epicNo.isBlank()) {
            response.put("status", false);
            response.put("message", "epicNo is required");
            return response;
        }

        Optional<Voter> optionalVoter = voterRepository.findById(epicNo);
        if (optionalVoter.isEmpty()) {
            response.put("status", false);
            response.put("message", "Voter not found with EPIC No: " + epicNo);
            return response;
        }

        Voter voter = optionalVoter.get();

        try {
            // name
            if (updates.containsKey("name") && updates.get("name") != null) {
                voter.setName(updates.get("name").toString());
            }

            // constituencyNo
            if (updates.containsKey("constituencyNo") && updates.get("constituencyNo") != null) {
                voter.setConstituencyNo(updates.get("constituencyNo").toString());
            }

            // partNo
            if (updates.containsKey("partNo") && updates.get("partNo") != null) {
                voter.setPartNo(updates.get("partNo").toString());
            }

            // age
            if (updates.containsKey("age") && updates.get("age") != null) {
                try {
                    int age = Integer.parseInt(updates.get("age").toString());
                    if (age < 18) {
                        response.put("status", false);
                        response.put("message", "Voter must be at least 18 years old");
                        return response;
                    }
                    voter.setAge(age);
                } catch (NumberFormatException ex) {
                    response.put("status", false);
                    response.put("message", "age must be a valid integer");
                    return response;
                }
            }

            // aadharNo (check uniqueness if changed)
            if (updates.containsKey("aadharNo") && updates.get("aadharNo") != null) {
                String newAadhar = updates.get("aadharNo").toString().trim();
                if (!newAadhar.equals(voter.getAadharNo())) {
                    if (voterRepository.existsByAadharNo(newAadhar)) {
                        response.put("status", false);
                        response.put("message", "Another voter with this Aadhaar number already exists");
                        return response;
                    }
                    voter.setAadharNo(newAadhar);
                }
            }

            // hasVoted (optional)
            if (updates.containsKey("hasVoted") && updates.get("hasVoted") != null) {
                Object hv = updates.get("hasVoted");
                boolean hasVotedValue;
                if (hv instanceof Boolean) {
                    hasVotedValue = (Boolean) hv;
                } else {
                    hasVotedValue = Boolean.parseBoolean(hv.toString());
                }
                voter.setHasVoted(hasVotedValue);
            }

            Voter saved = voterRepository.save(voter);

            response.put("status", true);
            response.put("message", "Voter updated successfully");
            response.put("voter", saved);
            return response;

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Error while updating voter: " + e.getMessage());
            return response;
        }
    }

    public Map<String, Object> updateVoteStatus(String epicNo, boolean hasVoted) {

        Map<String, Object> response = new HashMap<>();

        // Only allow update if hasVoted is true
        if (!hasVoted) {
            response.put("status", false);
            response.put("message", "Invalid vote update request");
            return response;
        }

        Voter voter = voterRepository.findById(epicNo).orElse(null);

        if (voter == null) {
            response.put("status", false);
            response.put("message", "Voter not found");
            return response;
        }

        // Prevent double voting
        if (voter.isHasVoted()) {
            response.put("status", false);
            response.put("message", "Voter has already voted");
            return response;
        }

        voter.setHasVoted(true);
        voterRepository.save(voter);

        response.put("status", true);
        response.put("message", "Vote recorded successfully");
        response.put("epicNo", epicNo);

        return response;
    }





}