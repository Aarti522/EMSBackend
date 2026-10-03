package com.example.ems.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PythonAIClient {

    private final RestTemplate restTemplate;

    @Value("${ai.service.base-url:http://localhost:8000}")
    private String pythonBaseUrl;

    public PythonAIClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // =========================================================
    // 1. PERFORMANCE PREDICTION
    // Spring:  /ai/performance
    // Python:  /api/ai/performance/predict
    // =========================================================
    public Object callPerformancePrediction(Map<String, Object> request) {
        return postJson(
                "/api/ai/performance/predict",
                request
        );
    }

    // =========================================================
    // 2. ATTRITION PREDICTION
    // Spring:  /ai/attrition
    // Python:  /api/ai/attrition/predict
    // =========================================================
    public Object callAttritionPrediction(Map<String, Object> request) {
        return postJson(
                "/api/ai/attrition/predict",
                request
        );
    }

    // =========================================================
    // 3. ATTENDANCE AI INSIGHTS
    // Spring:  /ai/ai-attendance
    // Python:  /api/ai/attendance/analyze
    // =========================================================
    public Object callAttendanceInsights(Map<String, Object> request) {
        return postJson(
                "/api/ai/attendance/analyze",
                request
        );
    }

    // =========================================================
    // 4. HR CHATBOT
    // Spring:  /ai/chatbot
    // Python:  /api/ai/chatbot/chat
    // =========================================================
    public Object callChatbot(Map<String, Object> request) {
        return postJson(
                "/api/ai/chatbot/chat",
                request
        );
    }

    // =========================================================
    // 5. RESUME SCREENING
    // Spring:  /ai/resume
    // Python:  /api/ai/resume/screen
    //
    // This endpoint uses multipart/form-data because a PDF
    // file is being sent.
    // =========================================================
    public Object callResumeScreening(
            MultipartFile resume,
            String jobDescription,
            String role) {

        try {

            MultiValueMap<String, Object> body =
                    new LinkedMultiValueMap<>();

            ByteArrayResource resumeResource =
                    new ByteArrayResource(resume.getBytes()) {

                        @Override
                        public String getFilename() {
                            return resume.getOriginalFilename();
                        }
                    };

            body.add("resume", resumeResource);
            body.add("job_description", jobDescription);
            body.add("role", role);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.MULTIPART_FORM_DATA
            );

            HttpEntity<MultiValueMap<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            String url =
                    pythonBaseUrl +
                    "/api/ai/resume/screen";

            return restTemplate.postForObject(
                    url,
                    entity,
                    Object.class
            );

        } catch (HttpStatusCodeException e) {

            throw new RuntimeException(
                    "Resume screening failed: "
                    + e.getResponseBodyAsString()
            );

        } catch (ResourceAccessException e) {

            throw new RuntimeException(
                    "Python AI service is unavailable."
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Unable to process resume: "
                    + e.getMessage()
            );
        }
    }

    // =========================================================
    // COMMON JSON POST METHOD
    // Used by:
    // Performance
    // Attrition
    // Attendance
    // Chatbot
    // =========================================================
    private Object postJson(
            String endpoint,
            Map<String, Object> request) {

        String url = pythonBaseUrl + endpoint;

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(
                        request,
                        headers
                );

        try {

            return restTemplate.postForObject(
                    url,
                    entity,
                    Object.class
            );

        } catch (HttpStatusCodeException e) {

            throw new RuntimeException(
                    "AI request failed: "
                    + e.getResponseBodyAsString()
            );

        } catch (ResourceAccessException e) {

            throw new RuntimeException(
                    "Python AI service is unavailable."
            );
        }
    }
}