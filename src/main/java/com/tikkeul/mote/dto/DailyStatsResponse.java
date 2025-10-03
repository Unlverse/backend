package com.tikkeul.mote.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DailyStatsResponse {
    private final LocalDate date;          // 날짜
    private final int totalRevenue;        // 해당일의 총 매출
    private final int parkingRevenue;      // 일반주차 매출
    private final int subscriptionRevenue; // 정기권 매출
    private final int refundAmount;        // 정기권 환불 금액
    private final int totalEntries;        // 해당일의 총 입차 대수
    private final int totalExits;          // 해당일의 총 출차 대수
}
