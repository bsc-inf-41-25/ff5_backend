package com.ticketmaster.ticketmaster.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PayChanguService {

    private static final Logger log = LoggerFactory.getLogger(PayChanguService.class);

    @Value("${paychangu.secret-key}")
    private String secretKey;

    @Value("${paychangu.base-url}")
    private String baseUrl;

    @Value("${paychangu.callback-url}")
    private String callbackUrl;

    @Value("${paychangu.return-url}")
    private String returnUrl;

    private final RestTemplate restTemplate;

    public PayChanguService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    public String initiateCheckout(Double amount, String email, Long orderId) {
        String txRef = "order_" + orderId + "_" + UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", String.valueOf(amount));
        body.put("currency", "MWK");
        body.put("email", email);
        body.put("first_name", "Customer");
        body.put("last_name", "User");
        body.put("tx_ref", txRef);
        body.put("callback_url", callbackUrl);
        body.put("return_url", returnUrl);

        Map<String, String> custom = new HashMap<>();
        custom.put("title", "Event Ticket Purchase");
        custom.put("description", "Event ticket payment");
        body.put("customization", custom);

        Map<String, String> meta = new HashMap<>();
        meta.put("order_id", String.valueOf(orderId));
        body.put("meta", meta);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            log.info("Initiating PayChangu checkout for order ID: {}", orderId);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/payment", request, Map.class);

            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return (String) data.get("checkout_url");

        } catch (Exception e) {
            log.error("PayChangu checkout failed: {}", e.getMessage());
            throw new RuntimeException("PayChangu checkout failed: " + e.getMessage());
        }
    }
}