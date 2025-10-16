package com.tikkeul.mote.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class AdminSignupRequest {

    @NotBlank(message = "아이디는 필수 항목입니다.")
    private String username;        // ID

    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    private String password;        // 비밀번호

    @NotBlank(message = "비밀번호 확인은 필수 항목입니다.")
    private String confirmPassword; // 비밀번호 재확인

    // @NotBlank(message = "사업자등록번호는  필수 항목입니다.")
    // private String businessNo;   // 사업자등록번호

    @NotBlank(message = "이름은 필수 항목입니다.")
    private String name;            // 관리자 이름

    @NotBlank(message = "전화번호는 필수 항목입니다.")
    private String phoneNumber;     // 전화번호

    @NotBlank(message = "인증번호는 필수 항목입니다.")
    private String phoneAuthCode;   // 인증번호

    @NotBlank(message = "주차장 이름은 필수 항목입니다.")
    private String parkingLotName;  // 주차장 이름

    @NotBlank(message = "주소는 필수 항목입니다.")
    private String address;         // 주차장 주소

    @NotNull(message = "기본 가격은 필수 항목입니다.")
    private Integer basePrice;      // 기본 요금

    @NotNull(message = "분 당 가격은 필수 항목입니다.")
    private Integer pricePerMinute; // 분 당 가격

    @NotNull(message = "주차 면 수는 필수 항목입니다.")
    private Integer totalLot;       // 주차면 수

}
