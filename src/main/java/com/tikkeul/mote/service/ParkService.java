package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.ParkListResponse;
import com.tikkeul.mote.dto.ParkResponse;
import com.tikkeul.mote.dto.VisitorParkInfoResponse;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Park;
import com.tikkeul.mote.entity.ParkingLot;
import com.tikkeul.mote.exception.FullParkingLotException;
import com.tikkeul.mote.repository.ParkRepository;
import com.tikkeul.mote.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParkService {

    private final KakaoMapService KakaoMapService;
    private final ParkRepository parkRepository;
    private final ParkingLotRepository parkingLotRepository;

    public void savePark(Admin admin, Map<String, Object> gps, Map<String, Object> ocr) {

        // 1. 주차장 정보 가져오기
        ParkingLot lot = parkingLotRepository.findByAdmin(admin)
                .orElseThrow(() -> new IllegalStateException("해당 관리자의 주차장 정보가 없습니다."));

        // 2. 현재 차량 수 조회
        long currentCount = parkRepository.countByAdmin(admin);

        // 3. totalLot 초과 여부 확인
        if (currentCount >= lot.getTotalLot()) {
            throw new FullParkingLotException("주차장이 가득 찼습니다. 더 이상 차량을 등록할 수 없습니다.");
        }

        // 4. 차량 등록
        Park park = Park.builder()
                .admin(admin)
                .plate((String) ocr.get("plate"))
                .latitude(((Number) gps.get("latitude")).doubleValue())
                .longitude(((Number) gps.get("longitude")).doubleValue())
                .build();

        parkRepository.save(park);
    }

    public ParkListResponse getParkListWithStatus(Admin admin) {
        // 1. 차량 목록 조회
        List<Park> parks = parkRepository.findByAdmin(admin);

        // 2. 주차장 정보 조회 (단가, 총 대수 등)
        ParkingLot lot = parkingLotRepository.findByAdmin(admin)
                .orElseThrow(() -> new IllegalStateException("주차장 정보가 없습니다."));

        int pricePerMinute = lot.getPricePerMinute(); //
        // 3. 차량 수
        int currentCount = parks.size();

        // 4. DTO로 변환
        List<ParkResponse> parkResponses = parks.stream()
                .map(park -> ParkResponse.fromEntity(park, lot.getBasePrice(), lot.getPricePerMinute()))
                .toList();

        // 5. 응답 객체 생성
        return ParkListResponse.builder()
                .currentCount(currentCount)
                .totalLot(lot.getTotalLot())
                .parkLogs(parkResponses)
                .build();
    }

    public void deletePark(Long parkId, Admin admin) {
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new IllegalArgumentException("주차 정보가 존재하지 않습니다."));

        if (!park.getAdmin().getAdminId().equals(admin.getAdminId())) {
            throw new SecurityException("본인 주차장의 주차 정보만 삭제할 수 있습니다.");
        }

        parkRepository.delete(park);
    }

    public void updatePlate(Long parkId, String newPlate, Admin admin) {
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new IllegalArgumentException("주차 정보가 존재하지 않습니다."));

        if (!park.getAdmin().getAdminId().equals(admin.getAdminId())) {
            throw new SecurityException("본인 주차장의 주차 정보만 수정할 수 있습니다.");
        }

        park.setPlate(newPlate);
        parkRepository.save(park);
    }

    public VisitorParkInfoResponse getParkInfoByPlate(String plate) {
        Park park = parkRepository.findByPlate(plate)
                .orElseThrow(() -> new IllegalArgumentException("해당 차량은 현재 주차되어 있지 않습니다."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime entered = park.getTimestamp();
        Duration duration = Duration.between(entered, now);
        long minutes = duration.toMinutes();
        long hours = minutes / 60;
        long remain = minutes % 60;

        // 요금 정보 가져오기
        ParkingLot lot = parkingLotRepository.findByAdmin(park.getAdmin())
                .orElseThrow(() -> new IllegalStateException("주차장 정보가 없습니다."));

        int pricePerMinute = lot.getPricePerMinute();
        int basePrice = lot.getBasePrice();

        // 요금 계산 = 기본요금 + (분당요금 × 경과 분)
        int totalFee = basePrice + (pricePerMinute * (int) minutes);

        // 문자열로 포맷
        String durationStr = String.format("%d시간 %d분", hours, remain);
        String feeStr = String.format("%,d원", totalFee);
        String timestampStr = entered.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        String address = KakaoMapService.getAddressFromCoordinates(park.getLatitude(), park.getLongitude());

        return VisitorParkInfoResponse.builder()
                .plate(plate)
                .timestamp(timestampStr)
                .duration(durationStr)
                .fee(feeStr)
                .address(address)
                .build();
    }
}
