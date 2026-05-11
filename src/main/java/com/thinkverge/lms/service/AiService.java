package com.thinkverge.lms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAiReply(String courseContext, String question) {

        try {
           
            Map<String, Object> body = new HashMap<>();

            body.put("model" , "llama-3.3-70b-versatile");

            List<Map<String, String>> messages = new ArrayList<>();

            messages.add(Map.of(
                    "role", "system",
                    "content", "You are a helpful AI tutor for course: " + courseContext
            ));

            messages.add(Map.of(
                    "role", "user",
                    "content", question
            ));

            body.put("messages", messages);
            body.put("temperature", 0.5);

           
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    apiUrl,
                    request,
                    Map.class
            );

          
            Map responseBody = response.getBody();

            if (responseBody == null) {
                return "No response from AI";
            }

            List choices = (List) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "Invalid AI response";
            }

            Map choice = (Map) choices.get(0);
            Map message = (Map) choice.get("message");

            return message.get("content").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "AI service error: " + e.getMessage();
        }
    }
}