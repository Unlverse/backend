package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.*;
import com.tikkeul.mote.security.AdminDetails;
import com.tikkeul.mote.service.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.tikkeul.mote.service.AdminService;
import com.tikkeul.mote.service.BusinessVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    //private final BusinessVerificationService businessVerificationService;
    private final AuthenticationManager authenticationManager;
    private final PhoneVerificationService verificationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest request, HttpServletRequest httpRequest) {
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 세션에 SecurityContext 저장
            httpRequest.getSession(true)
                    .setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            return ResponseEntity.ok("로그인 성공");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("로그인 실패: 아이디 또는 비밀번호 오류");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AdminSignupRequest request) {
        try {
            adminService.signup(request);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
/*
    @PostMapping("/verify-business")
    public ResponseEntity<?> verifyBusiness(@RequestBody BusinessVerificationRequest request) {
        boolean verified = businessVerificationService.verifyAndStore(request.getBusinessNo());

        if (verified) {
            return ResponseEntity.ok("사업자 인증에 성공했습니다.");
        } else {
            return ResponseEntity.badRequest().body("유효하지 않은 사업자등록번호입니다.");
        }
    }

 */

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsernameAvailability(
            @RequestParam("username") String username
    ) {
        boolean isAvailable = adminService.isUsernameAvailable(username);
        return ResponseEntity.ok(Map.of("isAvailable", isAvailable));
    }

    @GetMapping("/info")
    public ResponseEntity<AdminInfoResponse> getAdminInfo(
            @AuthenticationPrincipal AdminDetails adminDetails
    ) {
        try {
            AdminInfoResponse response = adminService.getAdminInfo(adminDetails.getAdmin());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // 주차장 정보가 없는 경우 등
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 혹은 적절한 에러 응답
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null); // 혹은 적절한 에러 응답
        }
    }

    @PatchMapping("/update-info")
    public ResponseEntity<?> updateAdminInfo(
            @AuthenticationPrincipal AdminDetails adminDetails,
            @RequestBody AdminInfoUpdateRequest request
    ) {
        try {
            adminService.updateAdminInfo(adminDetails.getAdmin(), request);
            return ResponseEntity.ok("회원 정보가 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("수정 실패: " + e.getMessage());
        }
    }

    @PostMapping("/phone-verification/send")
    public ResponseEntity<?> send(@RequestBody PhoneSendRequest request) {
        boolean result = verificationService.sendVerificationCode(request.getPhoneNumber());
        if (result) return ResponseEntity.ok("인증번호 전송 성공");
        return ResponseEntity.status(500).body("전송 실패");
    }

    @PostMapping("/phone-verification/verify")
    public ResponseEntity<?> check(@RequestBody PhoneVerifyRequest request) {
        boolean result = verificationService.verifyCode(request.getPhoneNumber(), request.getCode());
        if (result) return ResponseEntity.ok("인증 성공");
        return ResponseEntity.status(400).body("인증 실패");
    }

    @PostMapping("/find-id/send")
    public ResponseEntity<?> sendCodeForFindId(@RequestBody FindIdSendCodeRequest request) {
        try {
            adminService.sendVerificationCodeForFindId(request.getName(), request.getPhoneNumber());
            return ResponseEntity.ok(Map.of("message", "인증번호가 발송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/find-id/verify")
    public ResponseEntity<?> verifyForFindId(@RequestBody FindIdVerifyRequest request) {
        try {
            String username = adminService.verifyCodeAndFindId(request.getPhoneNumber(), request.getCode());
            return ResponseEntity.ok(Map.of(
                    "message", "인증에 성공했습니다.",
                    "username", username
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password/send")
    public ResponseEntity<?> sendCodeForPasswordReset(@RequestBody PasswordResetSendCodeRequest request) {
        try {
            adminService.sendVerificationCodeForPasswordReset(request.getUsername(), request.getPhoneNumber());
            return ResponseEntity.ok(Map.of("message", "인증번호가 발송되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password/verify")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
        try {
            adminService.resetPassword(request.getPhoneNumber(), request.getCode(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
