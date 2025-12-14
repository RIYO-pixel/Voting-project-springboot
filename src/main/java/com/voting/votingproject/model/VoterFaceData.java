package com.voting.votingproject.model;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Document(collection = "voter_face_data")
@Data
public class VoterFaceData {

    @Id
    private String _id;      // Mongo internal _id

    @Field("id")
    private Long id;         // REQUIRED by schema

    @Field("epic_no")
    private String epicNo;

    @Field("face_data")
    private Map<String, Object> faceData;  // ✅ OBJECT, not List
}

