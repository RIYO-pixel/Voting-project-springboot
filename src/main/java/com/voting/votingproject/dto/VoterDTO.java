package com.voting.votingproject.dto;

import lombok.Data;

@Data
public class VoterDTO {
    private String epicNo;
    private String constituencyNo;
    private String partNo;
    private String name;
    private int age;
    private String aadharNo;
    // We usually exclude 'hasVoted' in the DTO used for adding voters
    // because a new voter hasn't voted by default.
}
