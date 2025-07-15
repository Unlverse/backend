package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.ParkListResponse;
import com.tikkeul.mote.dto.ParkResponse;
import com.tikkeul.mote.dto.VisitorNotFoundResponse;
import com.tikkeul.mote.dto.VisitorParkInfoResponse;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Park;
import com.tikkeul.mote.entity.ParkingLot;
import com.tikkeul.mote.exception.FullParkingLotException;
import com.tikkeul.mote.repository.ParkRepository;
import com.tikkeul.mote.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ParkService {

    private final KakaoMapService KakaoMapService;
    private final ParkRepository parkRepository;
    private final ParkingLotRepository parkingLotRepository;

    public void savePark(Admin admin, Map<String, Object> gps, Map<String, Object> ocr, String imagePath) {

        // 1. plate 추출
        String plate = (String) ocr.get("plate");

        // 2. plate 중복 체크
        if (!"NOT_FOUND".equalsIgnoreCase(plate)) {
            boolean exists = parkRepository.existsByPlate(plate);
            if (exists) {
                throw new IllegalStateException("이미 입차 중인 차량입니다.");
            }
        }

        // 3. 주차장 정보 가져오기
        ParkingLot lot = parkingLotRepository.findByAdmin(admin)
                .orElseThrow(() -> new IllegalStateException("해당 관리자의 주차장 정보가 없습니다."));

        // 4. 현재 차량 수 조회
        long currentCount = parkRepository.countByAdmin(admin);

        // 5. totalLot 초과 여부 확인
        if (currentCount >= lot.getTotalLot()) {
            throw new FullParkingLotException("주차장이 가득 찼습니다. 더 이상 차량을 등록할 수 없습니다.");
        }

        // 6. 차량 등록
        Park park = Park.builder()
                .admin(admin)
                .plate((String) ocr.get("plate"))
                .latitude(((Number) gps.get("latitude")).doubleValue())
                .longitude(((Number) gps.get("longitude")).doubleValue())
                .imagePath(imagePath)
                .build();

        parkRepository.save(park);
    }

    public void deletePark(Long parkId, Admin admin) {
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new IllegalArgumentException("주차 정보가 존재하지 않습니다."));

        if (!park.getAdmin().getAdminId().equals(admin.getAdminId())) {
            throw new SecurityException("본인 주차장의 주차 정보만 삭제할 수 있습니다.");
        }

        // 이미지 삭제
        String imagePath = park.getImagePath();
        if (imagePath != null && !imagePath.isBlank()) {
            String filename = imagePath.startsWith("/uploads/")
                    ? imagePath.substring("/uploads/".length())
                    : imagePath;

            File file = new File("uploads", filename);

            if (file.exists()) {
                file.delete();
            }
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

    public ParkListResponse getParkListWithStatus(Admin admin) {

        // 1. 차량 목록 조회
        List<Park> parks = parkRepository.findByAdmin(admin);

        // 2. 주차장 정보 조회 (단가, 총 대수 등)
        ParkingLot lot = parkingLotRepository.findByAdmin(admin)
                .orElseThrow(() -> new IllegalStateException("주차장 정보가 없습니다."));

        int pricePerMinute = lot.getPricePerMinute();
        int currentCount = parks.size();

        // 3. DTO로 변환
        List<ParkResponse> parkResponses = parks.stream()
                .map(park -> ParkResponse.fromEntity(park, lot.getBasePrice(), lot.getPricePerMinute()))
                .toList();

        // 4. 응답 객체 생성
        return ParkListResponse.builder()
                .currentCount(currentCount)
                .totalLot(lot.getTotalLot())
                .parkLogs(parkResponses)
                .build();
    }

    public Resource loadParkImageForAdmin(Long parkId, Admin admin) {
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new IllegalArgumentException("해당 차량 정보가 없습니다."));

        // 관리자가 자신의 차량 정보만 조회 가능
        if (!park.getAdmin().getAdminId().equals(admin.getAdminId())) {
            throw new SecurityException("본인 주차장의 차량 정보만 조회할 수 있습니다.");
        }

        String imagePath = park.getImagePath();
        if (imagePath == null || imagePath.isBlank()) {
            throw new IllegalStateException("이미지 경로가 없습니다.");
        }

        File file = new File("uploads", imagePath.replace("/uploads/", ""));
        if (!file.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "이미지 파일을 찾을 수 없습니다.");
        }

        return new FileSystemResource(file);
    }

    // 방문자 - 내 차량번호로 바로 조회
    public VisitorParkInfoResponse getParkInfoByPlate(String plate) {
        Park park = parkRepository.findByPlate(plate)
                .orElseThrow(() -> new IllegalArgumentException("해당 차량은 현재 주차되어 있지 않습니다."));

        return buildVisitorParkInfoResponse(park);
    }

    // 방문자 - 선택한 ParkID로 상세 조회
    public VisitorParkInfoResponse getParkInfoById(Long parkId) {
        Park park = parkRepository.findById(parkId)
                .orElseThrow(() -> new IllegalArgumentException("차량 정보가 존재하지 않습니다."));

        return buildVisitorParkInfoResponse(park);
    }

    // 방문자 - 주차장ID로 NOT_FOUND 리스트 조회
    public List<VisitorNotFoundResponse> getNotFoundByParkingLotName(String parkingLotName) {
        ParkingLot lot = parkingLotRepository.findByParkingLotName(parkingLotName)
                .orElseThrow(() -> new IllegalArgumentException("주차장 이름이 잘못되었습니다."));

        Admin admin = lot.getAdmin();

        List<Park> parks = parkRepository.findByAdminAndPlate(admin, "NOT_FOUND");

        return parks.stream()
                .map(park -> new VisitorNotFoundResponse(
                        park.getParkId(),
                        park.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        calculateDuration(park.getTimestamp())
                ))
                .toList();
    }

    // 공통 - 방문자 상세 정보 응답 생성
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
        String timestampStr = entered.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        String address = KakaoMapService.getAddressFromCoordinates(park.getLatitude(), park.getLongitude());

        return VisitorParkInfoResponse.builder()
                .plate(park.getPlate())
                .timestamp(timestampStr)
                .duration(durationStr)
                .fee(feeStr)
                .address(address)
                .adminPhone(park.getAdmin().getPhoneNumber())
                .build();
    }

    // 공통 - 경과 시간 계산
    private String calculateDuration(LocalDateTime enteredAt) {
        long minutes = Duration.between(enteredAt, LocalDateTime.now()).toMinutes();
        long hours = minutes / 60;
        long remain = minutes % 60;
        return String.format("%d시간 %d분", hours, remain);
    }
}
