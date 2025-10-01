package com.tikkeul.mote.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class StatsResponse {
    private final LocalDate startDate;     // 시작일
    private final LocalDate endDate;       // 종료일
    private final long totalRevenue;     // 기간 내 총 매출
    private final long totalEntries;     // 기간 내 총 입차
    private final long totalExits;       // 기간 내 총 출차
    private final List<DailyStatsResponse> dailyStats; // 일자별 상세 통계
}