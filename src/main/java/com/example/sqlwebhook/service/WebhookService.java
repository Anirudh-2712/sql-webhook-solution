package com.example.sqlwebhook.service;

import com.example.sqlwebhook.dto.WebhookResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();

    public WebhookResponse generateWebhook(String name, String regNo, String email) {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String,String> body = Map.of(
                "name", name,
                "regNo", regNo,
                "email", email
        );

        HttpEntity<Map<String,String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<WebhookResponse> resp = restTemplate.postForEntity(url, entity, WebhookResponse.class);
        return resp.getBody();
    }

    public ResponseEntity<String> submitFinalQuery(String webhookUrl, String jwtToken, String finalQuery) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken); // Authorization: Bearer <token>

        Map<String, String> payload = Map.of("finalQuery", finalQuery);
        HttpEntity<Map<String,String>> req = new HttpEntity<>(payload, headers);

        return restTemplate.postForEntity(webhookUrl, req, String.class);
    }
}
