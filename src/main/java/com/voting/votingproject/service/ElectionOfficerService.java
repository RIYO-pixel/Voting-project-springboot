package com.voting.votingproject.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.voting.votingproject.model.ElectionOfficer;
import com.voting.votingproject.repository.ElectionOfficerRepository;

@Service
public class ElectionOfficerService {

    @Autowired
    private ElectionOfficerRepository officerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;


    public List<ElectionOfficer> getAllOfficers() {
        return officerRepository.findAll();
    }
    //-----------------------sign-in----------------------------------------------------
    public HashMap<String, Object> loginOfficer(HashMap<String, String> loginData) {
        HashMap<String, Object> response = new HashMap<>();

        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            String constituencyNo = loginData.get("constituencyNo");
            String partNo = loginData.get("partNo");

            if (username == null || username.isBlank() ||
                password == null || password.isBlank() ||
                constituencyNo == null || constituencyNo.isBlank() ||
                partNo == null || partNo.isBlank()) {
                response.put("status", false);
                response.put("message", "All fields are required");
                return response;
            }

            ElectionOfficer officer = officerRepository.findByUsername(username);
            if (officer == null) {
                response.put("status", false);
                response.put("message", "Officer not found");
                return response;
            }

            if (!passwordEncoder.matches(password, officer.getPassword())) {
                response.put("status", false);
                response.put("message", "Invalid password");
                return response;
            }

            if (!officer.getConstituencyNo().equals(constituencyNo) ||
                !officer.getPartNo().equals(partNo)) {
                response.put("status", false);
                response.put("message", "Constituency number or Part number mismatch");
                return response;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("constituencyNo", constituencyNo);
            data.put("partNo", partNo);
            data.put("username", username);

            response.put("status", true);
            response.put("message", "Login successful");
            response.put("officer", data);
            return response;

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "An error occurred: " + e.getMessage());
            return response;
        }
    }
    //-------------------------Signup------------------------------------------
    public HashMap<String, Object> registerOfficer(ElectionOfficer officer) {
        HashMap<String, Object> response = new HashMap<>();

        // 1. Email already exists
        if (officerRepository.existsById(officer.getEmailId())) {
            response.put("status", false);
            response.put("message", "Email already registered");
            return response;
        }

        // 2. Username already taken
        ElectionOfficer existingUser = officerRepository.findByUsername(officer.getUsername());
        if (existingUser != null) {   // check for null instead of isPresent()
            response.put("status", false);
            response.put("message", "Username already taken");
            return response;
        }


        // 3. Field validations
        if (officer.getEmailId() == null || officer.getEmailId().isBlank()) {
            response.put("status", false);
            response.put("message", "Email cannot be empty");
            return response;
        }

        if (officer.getUsername() == null || officer.getUsername().isBlank()) {
            response.put("status", false);
            response.put("message", "Username cannot be empty");
            return response;
        }

        if (officer.getPassword() == null || officer.getPassword().isBlank()) {
            response.put("status", false);
            response.put("message", "Password cannot be empty");
            return response;
        }

        if (officer.getConstituencyNo() == null || officer.getConstituencyNo().isBlank()) {
            response.put("status", false);
            response.put("message", "Constituency number cannot be empty");
            return response;
        }
        String hashedPassword = passwordEncoder.encode(officer.getPassword());
        
        officer.setPassword(hashedPassword);
         officerRepository.save(officer);
         Map<String, Object> data = new HashMap<>();
         data.put("username", officer.getUsername());
         data.put("password", officer.getPassword());


        response.put("status", true);
        response.put("message", "Officer registered successfully");
        response.put("data", data);
        

        return response;

    }

    private String generateOtp() {
        int otpValue = (int)(Math.random() * 900000) + 100000;
        return String.valueOf(otpValue);
    }

    public Map<String, Object> sendOtp(String email) {
        Map<String, Object> response = new HashMap<>();

        try {

            String otp = generateOtp();

            // Send the OTP using your EmailService
            
            emailService.sendEmail(email, "Your OTP Code", "Your OTP is: " + otp);

            response.put("status", "success");
            response.put("otp", otp); // For demo purposes. Remove this in production!
            response.put("message", "OTP sent to " + email);
            
            
            return response;
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to send OTP: " + e.getMessage());
            return response; // <-- This is important! You need to return something here
        }
    }
    public Map<String, Object> updateOfficer(String emailId, Map<String, Object> updates) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (emailId == null || emailId.isBlank()) {
                response.put("status", false);
                response.put("message", "emailId is required");
                return response;
            }

            Optional<ElectionOfficer> optionalOfficer = officerRepository.findById(emailId);
            if (optionalOfficer.isEmpty()) {
                response.put("status", false);
                response.put("message", "Officer not found for given emailId");
                return response;
            }

            ElectionOfficer officer = optionalOfficer.get();

            // username change (+unique check)
            if (updates.containsKey("username")) {
                String newUsername = updates.get("username") != null
                        ? updates.get("username").toString().trim()
                        : null;

                if (newUsername != null && !newUsername.isEmpty()
                        && !newUsername.equals(officer.getUsername())) {

                    ElectionOfficer existing = officerRepository.findByUsername(newUsername);
                    if (existing != null && !existing.getEmailId().equals(emailId)) {
                        response.put("status", false);
                        response.put("message", "Username already taken");
                        return response;
                    }

                    officer.setUsername(newUsername);
                }
            }

            if (updates.containsKey("constituencyNo") && updates.get("constituencyNo") != null) {
                officer.setConstituencyNo(updates.get("constituencyNo").toString());
            }
            if (updates.containsKey("officerConstituency") && updates.get("officerConstituency") != null) {
                officer.setOfficerConstituency(updates.get("officerConstituency").toString());
            }
            if (updates.containsKey("partNo") && updates.get("partNo") != null) {
                officer.setPartNo(updates.get("partNo").toString());
            }
            if (updates.containsKey("officerName") && updates.get("officerName") != null) {
                officer.setOfficerName(updates.get("officerName").toString());
            }
            if (updates.containsKey("password") && updates.get("password") != null) {
                officer.setPassword(updates.get("password").toString());
            }

            ElectionOfficer saved = officerRepository.save(officer);

            response.put("status", true);
            response.put("message", "Officer updated successfully");
            response.put("officer", saved);
            return response;

        } catch (Exception e) {
            response.put("status", false);
            response.put("message", "Error while updating officer: " + e.getMessage());
            return response;
        }
    }
    public Map<String, Object> deleteOfficer(String emailId) {
    Map<String, Object> response = new HashMap<>();

    try {
        if (emailId == null || emailId.isBlank()) {
            response.put("status", false);
            response.put("message", "emailId is required");
            return response;
        }

        // Find officer by _id (emailId)
        Optional<ElectionOfficer> optionalOfficer = officerRepository.findById(emailId);
        if (optionalOfficer.isEmpty()) {
            response.put("status", false);
            response.put("message", "Officer not found with emailId: " + emailId);
            return response;
        }

        ElectionOfficer officer = optionalOfficer.get();

        // If in future you link officers to voters, you could handle that here
        // e.g. unassign voters from this officer, etc.

        officerRepository.delete(officer);

        response.put("status", true);
        response.put("message", "Officer deleted successfully");
        return response;

    } catch (Exception e) {
        response.put("status", false);
        response.put("message", "Error while deleting officer: " + e.getMessage());
        return response;
    }
}
}
