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
    private final String pythonBaseUrl;

    public PythonAIClient(
            RestTemplate restTemplate,
            @Value("${ai.service.base-url}") String pythonBaseUrl) {

        this.restTemplate = restTemplate;

        // Remove trailing slash if present
        this.pythonBaseUrl = pythonBaseUrl.endsWith("/")
                ? pythonBaseUrl.substring(0, pythonBaseUrl.length() - 1)
                : pythonBaseUrl;

        System.out.println("==========================================");
        System.out.println("PYTHON AI BASE URL: " + this.pythonBaseUrl);
        System.out.println("==========================================");
    }

    // =========================================================
    // PERFORMANCE
    // =========================================================
    public Object callPerformancePrediction(
            Map<String, Object> request) {

        return postJson(
                "/api/ai/performance/predict",
                request
        );
    }

    // =========================================================
    // ATTRITION
    // =========================================================
    public Object callAttritionPrediction(
            Map<String, Object> request) {

        return postJson(
                "/api/ai/attrition/predict",
                request
        );
    }

    // =========================================================
    // ATTENDANCE AI
    // =========================================================
    public Object callAttendanceInsights(
            Map<String, Object> request) {

        return postJson(
                "/api/ai/attendance/analyze",
                request
        );
    }

    // =========================================================
    // CHATBOT
    // =========================================================
    public Object callChatbot(
            Map<String, Object> request) {

        return postJson(
                "/api/ai/chatbot/chat",
                request
        );
    }

    // =========================================================
    // RESUME SCREENING
    // =========================================================
    public Object callResumeScreening(
            MultipartFile resume,
            String jobDescription,
            String role) {

        String url = pythonBaseUrl + "/api/ai/resume/screen";

        System.out.println("Calling Python AI: " + url);

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

            return restTemplate.postForObject(
                    url,
                    entity,
                    Object.class
            );

        } catch (HttpStatusCodeException e) {

            System.err.println(
                    "Python AI HTTP Error: " + e.getStatusCode()
            );

            System.err.println(
                    "Python AI Response: " +
                    e.getResponseBodyAsString()
            );

            throw new RuntimeException(
                    "Resume screening failed: " +
                    e.getResponseBodyAsString()
            );

        } catch (ResourceAccessException e) {

            System.err.println(
                    "Python AI connection error: " +
                    e.getMessage()
            );

            throw new RuntimeException(
                    "Python AI service is unavailable."
            );

        } catch (Exception e) {

            System.err.println(
                    "Resume AI error: " + e.getMessage()
            );

            throw new RuntimeException(
                    "Unable to process resume: " +
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // COMMON JSON POST
    // =========================================================
    private Object postJson(
            String endpoint,
            Map<String, Object> request) {

        String url = pythonBaseUrl + endpoint;

        System.out.println("==========================================");
        System.out.println("Calling Python AI: " + url);
        System.out.println("Request: " + request);
        System.out.println("==========================================");

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(request, headers);

        try {

            Object response =
                    restTemplate.postForObject(
                            url,
                            entity,
                            Object.class
                    );

            System.out.println(
                    "Python AI Response: " + response
            );

            return response;

        } catch (HttpStatusCodeException e) {

            System.err.println(
                    "Python AI HTTP Status: " +
                    e.getStatusCode()
            );

            System.err.println(
                    "Python AI Error Response: " +
                    e.getResponseBodyAsString()
            );

            throw new RuntimeException(
                    "AI request failed: " +
                    e.getResponseBodyAsString()
            );

        } catch (ResourceAccessException e) {

            System.err.println(
                    "Python AI connection error: " +
                    e.getMessage()
            );

            throw new RuntimeException(
                    "Python AI service is unavailable."
            );
        }
    }
}