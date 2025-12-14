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
import org.springframework.http.client.SimpleClientHttpRequestFactory;

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
        /* =====================
           1️⃣ Validate input
           ===================== */
        String epicNo = String.valueOf(faceDataMap.get("epicNo"));
        List<String> base64Images =
                (List<String>) faceDataMap.get("face_data");

        if (epicNo == null || epicNo.isBlank()) {
            throw new IllegalArgumentException("EPIC number is required");
        }

        if (base64Images == null || base64Images.isEmpty()) {
            throw new IllegalArgumentException("Face images are required");
        }

        /* =====================
           2️⃣ Check voter
           ===================== */
        Voter voter = voterRepository.findByEpicNo(epicNo);
        if (voter == null) {
            throw new IllegalArgumentException(
                    "Voter not found for EPIC No: " + epicNo);
        }

        /* =====================
           3️⃣ Prepare Flask request
           ===================== */
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("user_id", epicNo);
        requestPayload.put("face_data", base64Images);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(requestPayload, headers);

        /* =====================
           4️⃣ RestTemplate with timeout
           ===================== */
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15_000);
        factory.setReadTimeout(90_000);

        RestTemplate restTemplate = new RestTemplate(factory);

        String baseUrl = System.getenv("PYTHON_BACKEND_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Python backend URL is not configured");
        }

        String flaskEndpoint = baseUrl + "/api/register_face";

        /* =====================
           5️⃣ Call Flask (with retry for cold start)
           ===================== */
        ResponseEntity<Map> flaskResponse;

        try {
            flaskResponse = restTemplate.exchange(
                    flaskEndpoint,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
        } catch (Exception firstFail) {
            // 🔁 retry once (Render cold start)
            Thread.sleep(2000);
            flaskResponse = restTemplate.exchange(
                    flaskEndpoint,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
        }

        /* =====================
           6️⃣ Validate Flask response
           ===================== */
        if (flaskResponse.getStatusCode() != HttpStatus.OK ||
            flaskResponse.getBody() == null) {

            throw new RuntimeException(
                    "Python service unavailable (502 / timeout)");
        }

        if (!"success".equals(flaskResponse.getBody().get("status"))) {
            throw new RuntimeException(
                    String.valueOf(
                        flaskResponse.getBody().get("message")));
        }

        /* =====================
           7️⃣ Convert embeddings safely
           ===================== */
        ObjectMapper mapper = new ObjectMapper();

        List<List<Double>> embeddings =
                mapper.convertValue(
                        flaskResponse.getBody().get("embeddings"),
                        new TypeReference<List<List<Double>>>() {}
                );

        if (embeddings == null || embeddings.isEmpty()) {
            throw new IllegalStateException(
                    "No face embeddings received from Python");
        }

        /* =====================
           8️⃣ Save face data
           ===================== */
        long faceId =
                sequenceGeneratorService.getNextSequence(
                        "voter_face_data_seq");

        Map<String, Object> faceDataObject = new HashMap<>();
        faceDataObject.put("embeddings", embeddings);

        VoterFaceData faceData = new VoterFaceData();
        faceData.setId(faceId);
        faceData.setEpicNo(epicNo);
        faceData.setFaceData(faceDataObject);

        voterFaceDataRepository.save(faceData);

        /* =====================
           9️⃣ Success response
           ===================== */
        response.put("status", "success");
        response.put("code", "FACE_REGISTERED");
        response.put("message", "Face data saved successfully");
        response.put("epicNo", epicNo);
        response.put("faceId", faceId);

        return response;

    } catch (IllegalArgumentException e) {
        response.put("status", "error");
        response.put("code", "VALIDATION_ERROR");
        response.put("message", e.getMessage());
        return response;

    } catch (IllegalStateException e) {
        response.put("status", "error");
        response.put("code", "CONFIG_ERROR");
        response.put("message", e.getMessage());
        return response;

    } catch (Exception e) {
        e.printStackTrace();
        response.put("status", "error");
        response.put("code", "PYTHON_SERVICE_ERROR");
        response.put(
                "message",
                "Face processing service is currently unavailable. Please try again."
        );
        return response;
    }
}


}
