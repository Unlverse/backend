package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.DailyStatsResponse;
import com.tikkeul.mote.dto.StatsResponse;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.ParkingHistory;
import com.tikkeul.mote.repository.ParkRepository;
import com.tikkeul.mote.repository.ParkingHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final ParkingHistoryRepository parkingHistoryRepository;
    private final ParkRepository parkRepository;

    public StatsResponse getDailyStats(Admin admin, LocalDate startDate, LocalDate endDate) {
        List<DailyStatsResponse> dailyStatsList = new ArrayList<>();
        long periodTotalRevenue = 0;
        long periodTotalEntries = 0;
        long periodTotalExits = 0;

        // 시작일부터 종료일까지 하루씩 반복
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            var startOfDay = date.atStartOfDay();
            var endOfDay = date.atTime(LocalTime.MAX);

            // 1. 해당일에 '출차'한 기록으로 매출과 출차 대수 계산
            List<ParkingHistory> historiesExitedToday = parkingHistoryRepository.findByAdminAndExitTimeBetween(admin, startOfDay, endOfDay);
            int dailyExits = historiesExitedToday.size();
            int dailyRevenue = historiesExitedToday.stream().mapToInt(ParkingHistory::getFee).sum();

            // 2. 해당일에 '입차'한 기록으로 입차 대수 계산
            // (과거에 입차했다가 오늘 나간 차량 + 오늘 입차해서 아직 주차 중인 차량)
            long entriesFromHistory = parkingHistoryRepository.countByAdminAndEntryTimeBetween(admin, startOfDay, endOfDay);
            long entriesFromPark = parkRepository.countByAdminAndTimestampBetween(admin, startOfDay, endOfDay);
            int dailyEntries = (int) (entriesFromHistory + entriesFromPark);

            // 3. 일별 통계 DTO 생성
            dailyStatsList.add(DailyStatsResponse.builder()
                    .date(date)
                    .totalRevenue(dailyRevenue)
                    .totalEntries(dailyEntries)
                    .totalExits(dailyExits)
                    .build());

            // 4. 기간 전체 통계에 합산
            periodTotalRevenue += dailyRevenue;
            periodTotalEntries += dailyEntries;
            periodTotalExits += dailyExits;
        }

        // 5. 최종 응답 DTO 생성
        return StatsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(periodTotalRevenue)
                .totalEntries(periodTotalEntries)
                .totalExits(periodTotalExits)
                .dailyStats(dailyStatsList)
                .build();
    }
}