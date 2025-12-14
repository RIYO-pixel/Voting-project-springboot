package com.voting.votingproject.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voting.votingproject.config.JwtUtil;
import com.voting.votingproject.dto.ElectionOfficerDTO;
import com.voting.votingproject.dto.VoterDTO;
import com.voting.votingproject.model.ElectionOfficer;
import com.voting.votingproject.model.Voter;
import com.voting.votingproject.service.ElectionOfficerService;
import com.voting.votingproject.service.VoterFaceService;
import com.voting.votingproject.service.VoterService;
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private ElectionOfficerService officerService;

    @Autowired
    private VoterService voterService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private VoterFaceService voterFaceService;

    

    @PostMapping("/create-officer")
public ResponseEntity<?> createOfficer(@RequestBody ElectionOfficerDTO dto) {
    // Map DTO to ElectionOfficer
    ElectionOfficer officer = new ElectionOfficer();
    officer.setEmailId(dto.getEmailId());
    officer.setConstituencyNo(dto.getConstituencyNo());
    officer.setOfficerConstituency(dto.getOfficerConstituency());
    officer.setPartNo(dto.getPartNo());
    officer.setOfficerName(dto.getOfficerName());
    officer.setUsername(dto.getUsername());
    officer.setPassword(dto.getPassword()); // include password if applicable

    HashMap<String, Object> response = officerService.registerOfficer(officer);

    if ((boolean) response.get("status")) {
        return ResponseEntity.ok(response);
    } else {
        return ResponseEntity.badRequest().body(response);
    }
}


    @PostMapping("/add-voter")
    public ResponseEntity<String> addVoter(@RequestBody VoterDTO dto) {
        try {
            Voter voter = new Voter();
            voter.setEpicNo(dto.getEpicNo());
            voter.setConstituencyNo(dto.getConstituencyNo());
            voter.setPartNo(dto.getPartNo());
            voter.setName(dto.getName());
            voter.setAge(dto.getAge());
            voter.setAadharNo(dto.getAadharNo());
            voter.setHasVoted(false);

            voterService.addVoter(voter);
            return ResponseEntity.ok("Voter Added Successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/send-otp")
public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody Map<String, Object> entity) {

    Map<String, Object> response = new HashMap<>();

    try {
        String email = (String) entity.get("email");

        if (email == null || email.trim().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }

        // ✅ Service sends OTP to email
        Map<String, Object> otpData = officerService.sendOtp(email);

        if (!otpData.get("status").equals("success")) {
            return ResponseEntity.badRequest().body(otpData); // directly return service error
        }

        String otp = otpData.get("otp").toString();

        // ✅ JWT STORES ONLY OTP (NOT EMAIL)
        String token = jwtUtil.generateOtpToken(otp, 360); // 6 minutes expiry

        response.put("status", "success");
        response.put("token", token);
        response.put("message", "OTP sent successfully to registered email");

        return ResponseEntity.ok(response);

    } catch (Exception e) {

        response.put("status", "error");
        response.put("message", "An error occurred while sending OTP");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

@PostMapping("/verify-otp")
public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, Object> entity) {

    Map<String, Object> response = new HashMap<>();

    try {
        String userEnteredOtp = (String) entity.get("otp");
        String token = (String) entity.get("token");

        if (userEnteredOtp == null || token == null) {
            response.put("status", "error");
            response.put("message", "OTP and token are required");
            return ResponseEntity.badRequest().body(response);
        }

        // ✅ Extract OTP stored inside JWT (ONLY OTP is stored)
        String sessionOtp = jwtUtil.extractOtpFromToken(token);

        if (sessionOtp.equals(userEnteredOtp)) {

            response.put("status", "success");
            response.put("message", "OTP verified successfully");

            return ResponseEntity.ok(response);

        } else {
            response.put("status", "failure");
            response.put("message", "Invalid OTP");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    } catch (io.jsonwebtoken.ExpiredJwtException e) {

        response.put("status", "error");
        response.put("message", "OTP has expired");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

    } catch (Exception e) {

        response.put("status", "error");
        response.put("message", "Error verifying OTP");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

    @GetMapping("/all-officers")
    public ResponseEntity<List<ElectionOfficer>> getAllOfficers() {
        return ResponseEntity.ok(officerService.getAllOfficers());
    }

    @GetMapping("/all-voters")
    public ResponseEntity<List<Voter>> getAllVoters() {
        return ResponseEntity.ok(voterService.getAllVoters());
    }

    @PostMapping("/register-face")
public ResponseEntity<Map<String, Object>> registerVoterFace(@RequestBody Map<String, Object> entity) {

    Map<String, Object> response = new HashMap<>();

    try {
        Map<String, Object> result = voterFaceService.registerFace(entity);
        return ResponseEntity.ok(result);

    } catch (IllegalArgumentException e) {
        response.put("status", "error");
        response.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(response);

    } catch (Exception e) {
        response.put("status", "error");
        response.put("message", "Error registering face data");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
    @PutMapping("/update-officer/{emailId}")
    public ResponseEntity<Map<String, Object>> updateOfficer(
            @PathVariable String emailId,
            @RequestBody Map<String, Object> updates) {

        Map<String, Object> response = officerService.updateOfficer(emailId, updates);

        if (Boolean.TRUE.equals(response.get("status"))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/delete-officer/{emailId}")
    public ResponseEntity<Map<String, Object>> deleteOfficer(@PathVariable String emailId) {
        Map<String, Object> response = officerService.deleteOfficer(emailId);

        if (Boolean.TRUE.equals(response.get("status"))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/delete-voter/{epicNo}")
    public ResponseEntity<Map<String, Object>> deleteVoter(@PathVariable String epicNo) {
        Map<String, Object> response = new HashMap<>();

        try {
            voterService.deleteVoter(epicNo);

            response.put("status", true);
            response.put("message", "Voter deleted successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // thrown by service when voter not found
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Error while deleting voter: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    @PutMapping("/update-voter/{epicNo}")
    public ResponseEntity<Map<String, Object>> updateVoter(
            @PathVariable String epicNo,
            @RequestBody Map<String, Object> updates) {

        Map<String, Object> response = voterService.updateVoter(epicNo, updates);

        if (Boolean.TRUE.equals(response.get("status"))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

}