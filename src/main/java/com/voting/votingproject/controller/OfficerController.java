package com.voting.votingproject.controller;

import com.voting.votingproject.model.ElectionOfficer;
import com.voting.votingproject.model.Voter;
import com.voting.votingproject.service.ElectionOfficerService;
import com.voting.votingproject.service.VoterService;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/officer")
public class OfficerController {

    @Autowired
    private ElectionOfficerService officerService;

    @Autowired
    private VoterService voterService;

    @PostMapping("/login")
    public ResponseEntity<HashMap<String, Object>> login(@RequestBody HashMap<String, String> loginData) {
        HashMap<String, Object> response = officerService.loginOfficer(loginData);

        if ((boolean) response.get("status")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    
}