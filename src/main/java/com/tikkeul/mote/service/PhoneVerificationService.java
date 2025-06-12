package com.tikkeul.mote.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final CoolSmsService smsService;
    private final StringRedisTemplate redisTemplate;

    // 인증번호 생성
    private String createCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public boolean sendVerificationCode(String phoneNumber) {
        String code = createCode();
        boolean result = smsService.sendSms(phoneNumber, code);
        if (result) {
            redisTemplate.opsForValue()
                    .set("verify:" + phoneNumber, code, Duration.ofMinutes(5));
        }
        return result;
    }

    public boolean verifyCode(String phoneNumber, String code) {
        String savedCode = redisTemplate.opsForValue().get("verify:" + phoneNumber);
        return savedCode != null && savedCode.equals(code);
    }
}

