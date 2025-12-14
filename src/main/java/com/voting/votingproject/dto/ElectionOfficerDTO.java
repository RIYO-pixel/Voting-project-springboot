package com.voting.votingproject.dto;

import lombok.Data;

@Data
public class ElectionOfficerDTO {
    private String emailId;
    private String constituencyNo;
    private String officerConstituency;
    private String partNo;
    private String officerName;
    private String username;
    private String password;
}