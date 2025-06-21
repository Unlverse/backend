package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.ParkingInfoResponse;
import com.tikkeul.mote.entity.Park;
import com.tikkeul.mote.entity.ParkingLot;
import com.tikkeul.mote.repository.ParkRepository;
import com.tikkeul.mote.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ParkingInfoService {

    private final ParkRepository parkRepository;
    private final ParkingLotRepository parkingLotRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public ParkingInfoResponse getParkingInfo(String carNumber) {
        Park park = parkRepository.findTopByPlateOrderByTimestampDesc(carNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 차량번호의 주차 기록이 없습니다."));

        ParkingLot parkingLot = parkingLotRepository.findByAdminId(park.getAdmin().getAdminId())
                .orElseThrow(() -> new IllegalArgumentException("주차장 정보가 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime entryTime = park.getTimestamp();
        Duration duration = Duration.between(entryTime, now);
        long minutes = duration.toMinutes();

        int fee = (int)(minutes * parkingLot.getPricePerMinute());
        //주차 기본요금 있다는 전제 하에는 아래구문 사용
        //int fee = parkingLot.getBasePrice() + (int)(minutes * parkingLot.getPricePerMinute());

        String address = reverseGeocode(park.getLatitude(), park.getLongitude());

        return ParkingInfoResponse.builder()
                .carNumber(carNumber)
                .entryTime(entryTime.toString())
                .currentParkingDuration(formatDuration(duration))
                .parkingFee(fee)
                .address(address)
                .adminPhoneNumber(park.getAdmin().getPhoneNumber())
                .build();
    }

    private String reverseGeocode(double lat, double lng) {
        String url = String.format(
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f", lat, lng);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "MoteParkingService/1.0 (contact@tikkeul.com)");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                org.json.JSONObject json = new org.json.JSONObject(response.getBody());
                org.json.JSONObject address = json.getJSONObject("address");
                String road = address.optString("road", "");
                String city = address.optString("city", "");
                String state = address.optString("state", "");
                String fullAddress = String.join(" ", road, city, state).trim();
                return fullAddress.isEmpty() ? "주소 정보 없음" : fullAddress;
            } catch (Exception e) {
                System.err.println("역지오코딩 오류: " + e.getMessage());
                return "주소 변환 실패";
            }
        } else {
            return "주소 변환 실패";
        }
    }
    private String formatDuration(Duration d) {
        long h = d.toHours();
        long m = d.toMinutes() % 60;
        return h + "시간 " + m + "분";
    }
}
