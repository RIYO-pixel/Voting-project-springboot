package com.voting.votingproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.voting.votingproject.model.Voter;
import com.voting.votingproject.model.VoterFaceData;
import com.voting.votingproject.repository.VoterFaceDataRepository;
import com.voting.votingproject.repository.VoterRepository;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;


import java.util.*;

@Service
public class VoterFaceService {

    @Autowired
    private VoterRepository voterRepository;

    @Autowired
    private VoterFaceDataRepository voterFaceDataRepository;

    @Autowired
private SequenceGeneratorService sequenceGeneratorService;

    
    public Map<String, Object> registerFace(Map<String, Object> faceDataMap) {

    Map<String, Object> response = new HashMap<>();

    try {
        // 1️⃣ Extract input
        String epicNo = String.valueOf(faceDataMap.get("epicNo"));
        List<String> base64Images = (List<String>) faceDataMap.get("face_data");

        if (epicNo == null || epicNo.trim().isEmpty()
                || base64Images == null || base64Images.isEmpty()) {
            throw new IllegalArgumentException("Missing epicNo or face_data");
        }

        // 2️⃣ Check voter exists
        Voter voter = voterRepository.findByEpicNo(epicNo);
        if (voter == null) {
            throw new IllegalArgumentException(
                "Voter not found with EPIC No: " + epicNo
            );
        }

        // 3️⃣ Prepare Flask request
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("user_id", epicNo);
        requestPayload.put("face_data", base64Images);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(requestPayload, headers);

        RestTemplate restTemplate = new RestTemplate();
        String flaskEndpoint =
                System.getenv("PYTHON_BACKEND_URL") + "/api/register_face";

        ResponseEntity<Map> flaskResponse = restTemplate.exchange(
                flaskEndpoint,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        // 4️⃣ Validate Flask response
        if (flaskResponse.getStatusCode() != HttpStatus.OK ||
            flaskResponse.getBody() == null ||
            !"success".equals(flaskResponse.getBody().get("status"))) {

            throw new RuntimeException(
                "Error from Python service: " +
                (flaskResponse.getBody() != null
                    ? flaskResponse.getBody().get("message")
                    : "Unknown error")
            );
        }

        // 5️⃣ Safely convert embeddings (OBJECT, not String)
        ObjectMapper mapper = new ObjectMapper();

        List<List<Double>> processedEmbeddings =
            mapper.convertValue(
                flaskResponse.getBody().get("embeddings"),
                new TypeReference<List<List<Double>>>() {}
            );

        if (processedEmbeddings == null || processedEmbeddings.isEmpty()) {
            throw new IllegalStateException("No face embeddings received");
        }

        // 6️⃣ Generate LONG id (MANDATORY for your schema)
        long faceId =
    sequenceGeneratorService.getNextSequence("voter_face_data_seq");

Map<String, Object> faceDataObject = new HashMap<>();
faceDataObject.put("embeddings", processedEmbeddings);

VoterFaceData faceData = new VoterFaceData();
faceData.setId(faceId);
faceData.setEpicNo(epicNo);
faceData.setFaceData(faceDataObject);

voterFaceDataRepository.save(faceData);


        // 8️⃣ Success response
        response.put("status", "success");
        response.put("message", "Face data saved successfully");
        response.put("epicNo", epicNo);
        response.put("faceId", faceId);

        return response;

    } catch (IllegalArgumentException e) {
        response.put("status", "error");
        response.put("message", e.getMessage());
        return response;

    } catch (Exception e) {
        e.printStackTrace(); // 🔥 keep for debugging
        response.put("status", "error");
        response.put("message", e.getMessage());
        return response;
    }
}

}
