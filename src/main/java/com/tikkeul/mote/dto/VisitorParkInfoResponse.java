package com.tikkeul.mote.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VisitorParkInfoResponse {
    private String plate;       // 차량 번호
    private String timestamp;   // 입차 시간
    private String duration;    // 주차 시간
    private String fee;         // 요금
    private String address;     // 주차 위치 주소
}
