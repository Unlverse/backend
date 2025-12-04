package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.DailyStatsResponse;
import com.tikkeul.mote.dto.StatsResponse;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.repository.ParkRepository;
import com.tikkeul.mote.repository.ParkingHistoryRepository;
import com.tikkeul.mote.repository.SubscriptionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final ParkingHistoryRepository parkingHistoryRepository;
    private final ParkRepository parkRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    public StatsResponse getDailyStats(Admin admin, LocalDate startDate, LocalDate endDate) {

        // 1. 결과 담을 맵 초기화 (모든 날짜를 0으로 채움)
        Map<LocalDate, DailyStatsBuilder> statsMap = new HashMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            statsMap.put(date, new DailyStatsBuilder(date));
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 2. DB에서 그룹화된 데이터 한 번에 조회
        // [일반 주차] 매출 및 출차 수
        List<Object[]> exitStats = parkingHistoryRepository.findDailyExitStats(admin, startDateTime, endDateTime);
        exitStats.forEach(row -> {
            LocalDate date = convertToLocalDate(row[0]);
            if (statsMap.containsKey(date)) {
                statsMap.get(date).addParkingRevenue(((Number) row[1]).intValue());
                statsMap.get(date).addExits(((Number) row[2]).intValue());
            }
        });

        // [일반 주차] 입차 수 (ParkingHistory + Park)
        List<Object[]> historyEntries = parkingHistoryRepository.findDailyEntryStats(admin, startDateTime, endDateTime);
        historyEntries.forEach(row -> {
            LocalDate date = convertToLocalDate(row[0]);
            if (statsMap.containsKey(date)) statsMap.get(date).addEntries(((Number) row[1]).intValue());
        });

        List<Object[]> parkEntries = parkRepository.findDailyParkEntryStats(admin, startDateTime, endDateTime);
        parkEntries.forEach(row -> {
            LocalDate date = convertToLocalDate(row[0]);
            if (statsMap.containsKey(date)) statsMap.get(date).addEntries(((Number) row[1]).intValue());
        });

        // [정기권] 매출
        List<Object[]> subStats = subscriptionHistoryRepository.findDailySubscriptionStats(admin, startDate, endDate);
        subStats.forEach(row -> {
            LocalDate date = (LocalDate) row[0]; // 여긴 이미 LocalDate
            if (statsMap.containsKey(date)) statsMap.get(date).addSubscriptionRevenue(((Number) row[1]).intValue());
        });

        // [정기권] 환불
        List<Object[]> refundStats = subscriptionHistoryRepository.findDailyRefundStats(admin, startDateTime, endDateTime);
        refundStats.forEach(row -> {
            LocalDate date = convertToLocalDate(row[0]);
            if (statsMap.containsKey(date)) statsMap.get(date).addRefund(((Number) row[1]).intValue());
        });

        // 3. 결과 집계 및 DTO 변환
        List<DailyStatsResponse> dailyStatsList = statsMap.values().stream()
                .sorted((a, b) -> a.date.compareTo(b.date))
                .map(DailyStatsBuilder::build)
                .collect(Collectors.toList());

        // 전체 합계 계산
        long totalParkingRevenue = 0;
        long totalSubscriptionRevenue = 0;
        long totalRefundAmount = 0;
        long totalEntries = 0;
        long totalExits = 0;

        for (DailyStatsResponse ds : dailyStatsList) {
            totalParkingRevenue += ds.getParkingRevenue();
            totalSubscriptionRevenue += ds.getSubscriptionRevenue();
            totalRefundAmount += ds.getRefundAmount();
            totalEntries += ds.getTotalEntries();
            totalExits += ds.getTotalExits();
        }

        long totalRevenue = totalParkingRevenue + totalSubscriptionRevenue - totalRefundAmount;

        return StatsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue)
                .totalParkingRevenue(totalParkingRevenue)
                .totalSubscriptionRevenue(totalSubscriptionRevenue)
                .totalRefundAmount(totalRefundAmount)
                .totalEntries(totalEntries)
                .totalExits(totalExits)
                .dailyStats(dailyStatsList)
                .build();
    }

    // DB 결과 타입 변환 헬퍼 (java.sql.Date -> java.time.LocalDate)
    private LocalDate convertToLocalDate(Object dbDate) {
        if (dbDate instanceof Date) {
            return ((Date) dbDate).toLocalDate();
        } else if (dbDate instanceof String) {
            return LocalDate.parse((String) dbDate); // MySQL 버전에 따라 문자열로 올 수도 있음
        }
        return (LocalDate) dbDate; // LocalDate로 바로 오면 그대로 반환
    }

    // 통계 집계를 위한 내부 빌더 클래스
    private static class DailyStatsBuilder {
        LocalDate date;
        int parkingRevenue = 0;
        int subscriptionRevenue = 0;
        int refundAmount = 0;
        int entries = 0;
        int exits = 0;

        public DailyStatsBuilder(LocalDate date) { this.date = date; }

        void addParkingRevenue(int val) { this.parkingRevenue += val; }
        void addSubscriptionRevenue(int val) { this.subscriptionRevenue += val; }
        void addRefund(int val) { this.refundAmount += val; }
        void addEntries(int val) { this.entries += val; }
        void addExits(int val) { this.exits += val; }

        public DailyStatsResponse build() {
            return DailyStatsResponse.builder()
                    .date(date)
                    .totalRevenue(parkingRevenue + subscriptionRevenue - refundAmount)
                    .parkingRevenue(parkingRevenue)
                    .subscriptionRevenue(subscriptionRevenue)
                    .refundAmount(refundAmount)
                    .totalEntries(entries)
                    .totalExits(exits)
                    .build();
        }
    }
}