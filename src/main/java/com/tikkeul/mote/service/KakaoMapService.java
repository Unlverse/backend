package com.tikkeul.mote.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoMapService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate;

    public String getAddressFromCoordinates(double latitude, double longitude) {
        String url = String.format(
                "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=%f&y=%f",
                longitude, latitude
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey.trim());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, JsonNode.class
            );

            JsonNode documents = response.getBody().get("documents");
            if (documents.isArray() && documents.size() > 0) {
                return documents.get(0).get("address").get("address_name").asText();
            }
        } catch (Exception e) {
            return "주소 변환 실패";
        }

        return "주소 없음";
    }

    // 1) 보이지 않는 문자/공백 정리
    private static String sanitize(String s) {
        if (s == null) return "";
        return s
                .replace('\u00A0',' ')   // NBSP → 일반 공백
                .replace("\u200B","")    // zero width space
                .replace("\u200C","")
                .replace("\u200D","")
                .replace("\uFEFF","")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // 2) 카카오 헤더 (공백 포함 주의)
    private HttpHeaders kakaoHeaders() {
        String key = kakaoApiKey == null ? "" : kakaoApiKey.trim();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + key);   // "KakaoAK " + 키 (공백 필수)
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    public Coord geocodeAddress(String address) {
        String q = sanitize(address);
        if (q.isBlank()) throw new IllegalArgumentException("주소를 입력해주세요.");

        var uri = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", q)
                .queryParam("analyze_type", "similar") // 유사 매칭 허용
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        // 요청 로그
        log.info("[Kakao][addr] URI={}", uri);
        String auth = kakaoHeaders().getFirst("Authorization");
        log.info("[Kakao][addr] Auth(head)={}*** (len={})",
                auth == null ? "null" : auth.substring(0, Math.min(10, auth.length())),
                auth == null ? 0 : auth.length());

        try {
            ResponseEntity<JsonNode> res = restTemplate.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(kakaoHeaders()), JsonNode.class);

            log.info("[Kakao][addr] status={} body={}",
                    res.getStatusCode(),
                    res.getBody() == null ? "null" : res.getBody().toPrettyString());

            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
                throw new IllegalStateException("카카오 주소 API 호출 실패: status=" + res.getStatusCode());
            }

            JsonNode docs = res.getBody().path("documents");
            if (!docs.isArray() || docs.size() == 0) {
                // 주소 검색 0건이면 키워드 검색으로 폴백
                Coord fallback = searchKeyword(q);
                if (fallback != null) return fallback;
                throw new IllegalStateException("해당 주소로 검색된 결과가 없습니다: " + q);
            }

            JsonNode first = docs.get(0);
            JsonNode road = first.get("road_address");
            if (road != null && road.hasNonNull("x") && road.hasNonNull("y")) {
                return new Coord(road.get("y").asDouble(), road.get("x").asDouble());
            }
            JsonNode addr = first.get("address");
            if (addr != null && addr.hasNonNull("x") && addr.hasNonNull("y")) {
                return new Coord(addr.get("y").asDouble(), addr.get("x").asDouble());
            }
            if (first.hasNonNull("x") && first.hasNonNull("y")) {
                return new Coord(first.get("y").asDouble(), first.get("x").asDouble());
            }
            throw new IllegalStateException("응답에 좌표가 없습니다: " + first.toPrettyString());

        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            log.error("[Kakao][addr] HTTP={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("카카오 API 호출 실패(HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("[Kakao][addr] 예외: {}", e.toString(), e);
            throw new IllegalStateException("주소 좌표 변환 실패: " + q, e);
        }
    }

    private Coord searchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;

        var uri = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", sanitize(keyword))
                .encode(StandardCharsets.UTF_8)  //
                .build()
                .toUri();

        try {
            ResponseEntity<JsonNode> res = restTemplate.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(kakaoHeaders()), JsonNode.class);

            log.info("[Kakao][keyword] status={} body={}",
                    res.getStatusCode(),
                    res.getBody() == null ? "null" : res.getBody().toPrettyString());

            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) return null;

            JsonNode docs = res.getBody().path("documents");
            if (!docs.isArray() || docs.size() == 0) return null;

            JsonNode first = docs.get(0);
            if (first.hasNonNull("x") && first.hasNonNull("y")) {
                double lon = first.get("x").asDouble();
                double lat = first.get("y").asDouble();
                return new Coord(lat, lon);
            }
            return null;

        } catch (Exception e) {
            log.error("[Kakao][keyword] 예외: {}", e.toString(), e);
            return null;
        }
    }

    public record Coord(double lat, double lon) {}
}
