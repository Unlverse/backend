package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.AdminInfoResponse;
import com.tikkeul.mote.dto.AdminSignupRequest;
import com.tikkeul.mote.dto.AdminInfoUpdateRequest;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.ParkingLot;
import com.tikkeul.mote.repository.AdminRepository;
import com.tikkeul.mote.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final KakaoMapService kakaoMapService;
    private final PhoneVerificationService phoneVerificationService;

    @Transactional(rollbackFor = Exception.class)
    public void signup(AdminSignupRequest request) {
        String userName = request.getUsername();
        // String businessNo = request.getBusinessNo();
        String phoneNumber = request.getPhoneNumber();
        String phoneAuthCode = request.getPhoneAuthCode();

        /*  1. Redis 인증 여부 확인
        String verified = redisTemplate.opsForValue().get("business_verified:" + businessNo);
        if (!"true".equals(verified)) {
             throw new IllegalStateException("사업자번호 인증을 먼저 완료해 주세요.");
        }
         */

        // 2. 전화번호 인증번호 검증
        String redisCode = redisTemplate.opsForValue().get("verify:" + phoneNumber);
        if (redisCode == null || !redisCode.equals(phoneAuthCode)) {
            throw new IllegalStateException("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        //  3. ID 중복 체크
        if (adminRepository.existsByUsername(userName)) {
            throw new IllegalArgumentException("이미 사용 중인 ID입니다.");
        }

        /*  4. 사업자번호 중복 체크
        if (adminRepository.existsByBusinessNo(businessNo)) {
            throw new IllegalArgumentException("이미 등록된 사업자등록번호입니다.");
        }
         */

        //  5. 비밀번호 확인
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        //  6. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        //  7. Admin 저장
        Admin admin = Admin.builder()
                .username(userName)
                .password(encodedPassword)
                // .businessNo(businessNo)
                .businessNo("0000000000")
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        adminRepository.save(admin);

        // 8) 주소 → 좌표 변환 (필수 값 검증)
        if (request.getAddress() == null || request.getAddress().isBlank()) {
            throw new IllegalArgumentException("주차장 주소를 입력해 주세요.");
        }

        var coord = kakaoMapService.geocodeAddress(request.getAddress());

        //  9. parking_lot 저장
        ParkingLot lot = ParkingLot.builder()
                .admin(admin)
                .parkingLotName(request.getParkingLotName())
                .address(request.getAddress())
                .parkingLotLatitude(coord.lat())
                .parkingLotLongitude(coord.lon())
                .basePrice(request.getBasePrice())
                .pricePerMinute(request.getPricePerMinute())
                .totalLot(request.getTotalLot())
                .build();

        parkingLotRepository.save(lot);

        //  10. Redis 키 삭제 (선택)
        // redisTemplate.delete("business_verified:" + businessNo);
        redisTemplate.delete("verify:" + phoneNumber);
    }

    public void updateParkingLot(Admin admin, AdminInfoUpdateRequest request) {
        ParkingLot parkingLot = parkingLotRepository.findByAdmin(admin)
                .orElseThrow(() -> new IllegalStateException("주차장 정보를 찾을 수 없습니다."));

        if (request.getBasePrice() != null) {
            parkingLot.setBasePrice(request.getBasePrice()); // 기본요금 업데이트
        }

        if (request.getPricePerMinute() != null) {
            parkingLot.setPricePerMinute(request.getPricePerMinute());
        }
        if (request.getTotalLot() != null) {
            parkingLot.setTotalLot(request.getTotalLot());
        }

        parkingLotRepository.save(parkingLot);
    }

    public AdminInfoResponse getAdminInfo(Admin admin) {
        ParkingLot parkingLot = parkingLotRepository.findByAdmin(admin)
                .orElseThrow(() -> new IllegalStateException("주차장 정보를 찾을 수 없습니다."));

        return AdminInfoResponse.builder()
                .phoneNumber(admin.getPhoneNumber())
                .basePrice(parkingLot.getBasePrice())
                .pricePerMinute(parkingLot.getPricePerMinute())
                .totalLot(parkingLot.getTotalLot())
                .build();
    }

    public void updateAdminInfo(Admin admin, AdminInfoUpdateRequest request) {
        // 관리자 전화번호 업데이트
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            admin.setPhoneNumber(request.getPhoneNumber());
            adminRepository.save(admin);
        }

        // 주차장 정보 업데이트
        ParkingLot parkingLot = parkingLotRepository.findByAdmin(admin)
                .orElseThrow(() -> new IllegalStateException("주차장 정보를 찾을 수 없습니다."));

        if (request.getBasePrice() != null) {
            parkingLot.setBasePrice(request.getBasePrice());
        }

        if (request.getPricePerMinute() != null) {
            parkingLot.setPricePerMinute(request.getPricePerMinute());
        }

        if (request.getTotalLot() != null) {
            parkingLot.setTotalLot(request.getTotalLot());
        }

        parkingLotRepository.save(parkingLot);
    }

    public void sendVerificationCodeForFindId(String name, String phoneNumber) {
        adminRepository.findByNameAndPhoneNumber(name, phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 정보로 가입된 계정을 찾을 수 없습니다."));
        phoneVerificationService.sendVerificationCode(phoneNumber);
    }

    public String verifyCodeAndFindId(String phoneNumber, String code) {
        if (!phoneVerificationService.verifyCode(phoneNumber, code)) {
            throw new IllegalArgumentException("인증번호가 올바르지 않습니다.");
        }
        Admin admin = adminRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾는 데 실패했습니다."));

        redisTemplate.delete("verify:" + phoneNumber);
        return admin.getUsername();
    }

    public void sendVerificationCodeForPasswordReset(String username, String phoneNumber) {
        adminRepository.findByUsernameAndPhoneNumber(username, phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 정보로 가입된 계정을 찾을 수 없습니다."));
        phoneVerificationService.sendVerificationCode(phoneNumber);
    }

    @Transactional
    public void resetPassword(String phoneNumber, String code, String newPassword) {
        if (!phoneVerificationService.verifyCode(phoneNumber, code)) {
            throw new IllegalArgumentException("인증번호가 올바르지 않습니다.");
        }
        Admin admin = adminRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾는 데 실패했습니다."));

        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
        redisTemplate.delete("verify:" + phoneNumber);
    }

}
