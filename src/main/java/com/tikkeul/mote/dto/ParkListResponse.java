package com.tikkeul.mote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ParkListResponse {
    private int currentCount;              // 현재 주차된 차량 수
    private int totalLot;                  // 총 주차 가능 수
    private List<ParkResponse> parkLogs;   // 차량 목록
}
