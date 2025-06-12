package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.PhoneSendRequest;
import com.tikkeul.mote.dto.PhoneVerifyRequest;
import com.tikkeul.mote.service.PhoneVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/phone")
@RequiredArgsConstructor
public class PhoneVerificationController {

    private final PhoneVerificationService verificationService;

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody PhoneSendRequest request) {
        boolean result = verificationService.sendVerificationCode(request.getPhoneNumber());
        if (result) return ResponseEntity.ok("인증번호 전송 성공");
        return ResponseEntity.status(500).body("전송 실패");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> check(@RequestBody PhoneVerifyRequest request) {
        boolean result = verificationService.verifyCode(request.getPhoneNumber(), request.getCode());
        if (result) return ResponseEntity.ok("인증 성공");
        return ResponseEntity.status(400).body("인증 실패");
    }
}