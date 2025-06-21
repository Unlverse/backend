package com.tikkeul.mote.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSignupRequest {

    private String username;        // ID
    private String password;        // 비밀번호
    private String confirmPassword; // 비밀번호 재확인
    private String businessNo;      // 사업자등록번호
    private String name;            // 관리자 이름
    private String phoneNumber;     // 전화번호
    private String phoneAuthCode;   // 인증번호
    private Integer pricePerMinute; // 분당 가격
    private Integer totalLot;     // 주차면 수

}
