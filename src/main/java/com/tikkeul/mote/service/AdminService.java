package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.AdminSignupRequest;
import com.tikkeul.mote.dto.ParkingLotUpdateRequest;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.ParkingLot;
import com.tikkeul.mote.repository.AdminRepository;
import com.tikkeul.mote.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public void signup(AdminSignupRequest request) {
        String userName = request.getUsername();
        String businessNo = request.getBusinessNo();
        String phoneNumber = request.getPhoneNumber();
        String phoneAuthCode = request.getPhoneAuthCode();

        //  1. Redis 인증 여부 확인
        String verified = redisTemplate.opsForValue().get("business_verified:" + businessNo);
        if (!"true".equals(verified)) {
            throw new IllegalStateException("사업자번호 인증을 먼저 완료해 주세요.");
        }

        // 2. 전화번호 인증번호 검증
        String redisCode = redisTemplate.opsForValue().get("verify:" + phoneNumber);
        if (redisCode == null || !redisCode.equals(phoneAuthCode)) {
            throw new IllegalStateException("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        //  3. ID 중복 체크
        if (adminRepository.existsByUsername(userName)) {
            throw new IllegalArgumentException("이미 사용 중인 ID입니다.");
        }

        //  4. 사업자번호 중복 체크
        if (adminRepository.existsByBusinessNo(businessNo)) {
            throw new IllegalArgumentException("이미 등록된 사업자등록번호입니다.");
        }

        //  5. 비밀번호 확인
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        //  6. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        //  7. Admin, ParkingLot 저장
        Admin admin = Admin.builder()
                .username(userName)
                .password(encodedPassword)
                .businessNo(businessNo)
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        adminRepository.save(admin);

        ParkingLot lot = ParkingLot.builder()
                .admin(admin)
                .basePrice(request.getBasePrice())
                .pricePerMinute(request.getPricePerMinute())
                .totalLot(request.getTotalLot())
                .build();

        parkingLotRepository.save(lot);

        //  7. Redis 키 삭제 (선택)
        redisTemplate.delete("business_verified:" + businessNo);
        redisTemplate.delete("verify:" + phoneNumber);
    }

    public void updateParkingLot(Admin admin, ParkingLotUpdateRequest request) {
        ParkingLot parkingLot = parkingLotRepository.findByAdmin(admin)
                .orElseThrow(() -> new IllegalStateException("주차장 정보를 찾을 수 없습니다."));

        if (request.getBasePrice() != null) {
            parkingLot.setBasePrice(request.getBasePrice()); // ✅ 기본요금 업데이트
        }

        if (request.getPricePerMinute() != null) {
            parkingLot.setPricePerMinute(request.getPricePerMinute());
        }
        if (request.getTotalLot() != null) {
            parkingLot.setTotalLot(request.getTotalLot());
        }

        parkingLotRepository.save(parkingLot);
    }
}
