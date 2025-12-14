package com.voting.votingproject.controller;

import com.voting.votingproject.service.VoterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/officer")
public class VoterVerificationController {

    @Autowired
    private VoterService voterService;

    // -------------------- /verify-epic --------------------
    @PostMapping("/verify-epic")
    public ResponseEntity<HashMap<String, Object>> verifyEpic(@RequestBody HashMap<String, String> request) {

        HashMap<String, Object> response = voterService.verifyEpic(request);

        boolean status = (boolean) response.get("status");

        if (status) {
            // 200 OK with voter details
            return ResponseEntity.ok(response);
        } else {
            // 400 Bad Request with error message
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update-vote")
public ResponseEntity<Map<String, Object>> updateVote(
        @RequestBody Map<String, Object> request) {

    String epicNo = (String) request.get("epicNo");
    Boolean hasVoted = (Boolean) request.get("hasVoted"); // ✅ FIX

    Map<String, Object> response =
            voterService.updateVoteStatus(epicNo, hasVoted);

    if (Boolean.TRUE.equals(response.get("status"))) {
        return ResponseEntity.ok(response);
    } else {
        return ResponseEntity.badRequest().body(response);
    }
}

    
}
