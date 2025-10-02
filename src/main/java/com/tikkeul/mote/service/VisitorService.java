package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.NearbyParkingLotResponse;
import com.tikkeul.mote.dto.VisitorNotFoundResponse;
import com.tikkeul.mote.dto.VisitorParkInfoResponse;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Park;
import com.tikkeul.mote.entity.ParkingLot;
import com.tikkeul.mote.repository.AdminRepository;
import com.tikkeul.mote.repository.ParkRepository;
import com.tikkeul.mote.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitorService {

    private final KakaoMapService kakaoMapService;
    private final ParkRepository parkRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final AdminRepository adminRepository;

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 1. 내 차량번호로 바로 조회
    public VisitorParkInfoResponse getParkInfoByPlate(String plate) {
        Park park = parkRepository.findByPlate(plate)
                .orElseThrow(() -> new IllegalArgumentException("해당 차량은 현재 주차되어 있지 않습니다."));
        return buildVisitorParkInfoResponse(park);
    }

    // 2. 선택한 ParkID로 상세 조회
    public VisitorParkInfoResponse getParkInfoById(Long parkId) {
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new IllegalArgumentException("차량 정보가 존재하지 않습니다."));
        return buildVisitorParkInfoResponse(park);
    }

    // 3. 주차장 이름으로 NOT_FOUND 리스트
    public List<VisitorNotFoundResponse> getNotFoundByParkingLotName(String parkingLotName) {
        ParkingLot lot = parkingLotRepository.findByParkingLotName(parkingLotName)
                .orElseThrow(() -> new IllegalArgumentException("주차장 이름이 잘못되었습니다."));
        Admin admin = lot.getAdmin();

        List<Park> parks = parkRepository.findByAdminAndPlate(admin, "NOT_FOUND");
        return parks.stream()
                .sorted(Comparator.comparing(Park::getTimestamp).reversed())
                .map(park -> new VisitorNotFoundResponse(
                        park.getParkId(), // 네 엔티티가 getId()면 바꿔줘
                        park.getTimestamp().format(TS_FMT),
                        calculateDuration(park.getTimestamp())
                ))
                .toList();
    }

    // 4. 좌표 기반 주변 주차장 조회 → 가까운 순 (네이티브 결과 List<Object[]>를 페이지로 가공)
    public Page<NearbyParkingLotResponse> getNearbyLots(double lat, double lng, double radiusKm, Pageable pageable) {
        List<Object[]> rows = parkingLotRepository.findNearbyLotsRaw(lat, lng, radiusKm);

        List<NearbyParkingLotResponse> mapped = rows.stream().map(row -> {
            Long adminId         = row[0] != null ? ((Number) row[0]).longValue()  : null;
            String name          = row[1] != null ? (String) row[1]                : null;
            String address       = row[2] != null ? (String) row[2]                : null;
            double latitude      = row[3] != null ? ((Number) row[3]).doubleValue(): 0.0;
            double longitude     = row[4] != null ? ((Number) row[4]).doubleValue(): 0.0;
            double distanceKm    = row[5] != null ? ((Number) row[5]).doubleValue(): 0.0;

            return NearbyParkingLotResponse.builder()
                    .adminId(adminId)
                    .name(name)
                    .address(address)
                    .latitude(latitude)
                    .longitude(longitude)
                    .distanceKm(distanceKm)
                    .build();
        }).toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), mapped.size());
        List<NearbyParkingLotResponse> slice = start > mapped.size() ? List.of() : mapped.subList(start, end);

        return new PageImpl<>(slice, pageable, mapped.size());
    }

    // 5. adminId 기준 NOT_FOUND 리스트
    public List<VisitorNotFoundResponse> getNotFoundByAdmin(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));

        return parkRepository.findByAdminAndPlate(admin, "NOT_FOUND").stream()
                .sorted(Comparator.comparing(Park::getTimestamp).reversed())
                .map(park -> new VisitorNotFoundResponse(
                        park.getParkId(), // 네 엔티티가 getId()면 바꿔줘
                        park.getTimestamp().format(TS_FMT),
                        calculateDuration(park.getTimestamp())
                ))
                .toList();
    }

    // 방문자 상세 정보 응답 생성
    private VisitorParkInfoResponse buildVisitorParkInfoResponse(Park park) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime entered = park.getTimestamp();
        Duration duration = Duration.between(entered, now);
        long minutes = duration.toMinutes();
        long hours = minutes / 60;
        long remain = minutes % 60;

        ParkingLot lot = parkingLotRepository.findByAdmin(park.getAdmin())
                .orElseThrow(() -> new IllegalStateException("주차장 정보가 없습니다."));

        int pricePerMinute = lot.getPricePerMinute();
        int basePrice = lot.getBasePrice();
        int totalFee = basePrice + (pricePerMinute * (int) minutes);

        String durationStr = String.format("%d시간 %d분", hours, remain);
        String feeStr = String.format("%,d원", totalFee);
        String timestampStr = entered.format(TS_FMT);

        String address = "주소 정보 없음"; // 기본값을 설정합니다.
        if (park.getLatitude() != null && park.getLongitude() != null) {
            address = kakaoMapService.getAddressFromCoordinates(park.getLatitude(), park.getLongitude());
        }

        return VisitorParkInfoResponse.builder()
                .plate(park.getPlate())
                .timestamp(timestampStr)
                .duration(durationStr)
                .fee(feeStr)
                .address(address)
                .adminPhone(formatPhoneNumber(park.getAdmin().getPhoneNumber()))
                .build();
    }

    // 경과 시간 계산
    private String calculateDuration(LocalDateTime enteredAt) {
        long minutes = Duration.between(enteredAt, LocalDateTime.now()).toMinutes();
        long hours = minutes / 60;
        long remain = minutes % 60;
        return String.format("%d시간 %d분", hours, remain);
    }

    // 전화번호 포맷 변환 메서드
    private String formatPhoneNumber(String phone) {
        if (phone == null) return "";
        phone = phone.replaceAll("[^0-9]", ""); // 숫자만 남기기
        if (phone.length() == 11) {
            return phone.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        } else if (phone.length() == 10) {
            return phone.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }
        return phone; // 포맷 불가 시 원본 반환
    }
}
