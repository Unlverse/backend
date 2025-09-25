package com.tikkeul.mote.controller;

import com.tikkeul.mote.security.AdminDetails;
import lombok.RequiredArgsConstructor;
import com.tikkeul.mote.dto.AdminLoginRequest;
import com.tikkeul.mote.dto.AdminSignupRequest;
import com.tikkeul.mote.dto.BusinessVerificationRequest;
import com.tikkeul.mote.dto.AdminInfoUpdateRequest;
import com.tikkeul.mote.service.AdminService;
import com.tikkeul.mote.service.BusinessVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.AuthenticationException;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final BusinessVerificationService businessVerificationService;
    private final AuthenticationManager authenticationManager;

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
    public ResponseEntity<?> signup(@RequestBody AdminSignupRequest request) {
        try {
            adminService.signup(request);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-business")
    public ResponseEntity<?> verifyBusiness(@RequestBody BusinessVerificationRequest request) {
        boolean verified = businessVerificationService.verifyAndStore(request.getBusinessNo());

        if (verified) {
            return ResponseEntity.ok("사업자 인증에 성공했습니다.");
        } else {
            return ResponseEntity.badRequest().body("유효하지 않은 사업자등록번호입니다.");
        }
    }

    @PatchMapping("/update-admin-info")
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
}
