package com.tikkeul.mote.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessVerificationService {

    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;

    @Value("${odcloud.api.key}")
    private String serviceKey;

    public boolean verifyAndStore(String businessNo) {
        try {
            String apiUrl = UriComponentsBuilder
                    .fromHttpUrl("https://api.odcloud.kr/api/nts-businessman/v1/status")
                    .queryParam("serviceKey", "{serviceKey}")
                    .buildAndExpand(serviceKey) // 인코딩 없이 치환
                    .toUriString();

            // 요청 바디
            Map<String, Object> body = Map.of("b_no", List.of(businessNo));

            // 요청 헤더
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            String statusCode = (String) data.get(0).get("b_stt_cd");

            if ("01".equals(statusCode)) {
                redisTemplate.opsForValue().set("business_verified:" + businessNo, "true", Duration.ofMinutes(10));
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
