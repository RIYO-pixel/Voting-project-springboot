package com.voting.votingproject.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Document(collection = "election_officers")
@Data
public class ElectionOfficer {

    @Id
    private String emailId;   // maps to _id

    @Field("constituency_no")
    private String constituencyNo;

    @Field("officer_constituency")
    private String officerConstituency;

    @Field("part_no")
    private String partNo;

    @Field("officer_name")
    private String officerName;

    private String username;

    private String password;
}
