package com.voting.votingproject.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.ToString;

@Document(collection = "voters")
@Data
public class Voter {

    @Id
    private String epicNo;   // maps to _id

    @Field("constituency_no")
    private String constituencyNo;

    @Field("part_no")
    private String partNo;

    private String name;

    private int age;

    @Field("aadhar_no")
    @Indexed(unique = true)
    private String aadharNo;

    @Field("has_voted")
    private boolean hasVoted = false;

    @ToString.Exclude
    @DBRef
    private List<VoterFaceData> faceDataList;
}
