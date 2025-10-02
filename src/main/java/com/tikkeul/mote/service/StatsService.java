package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.DailyStatsResponse;
import com.tikkeul.mote.dto.StatsResponse;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.ParkingHistory;
import com.tikkeul.mote.entity.SubscriptionHistory;
import com.tikkeul.mote.repository.ParkRepository;
import com.tikkeul.mote.repository.ParkingHistoryRepository;
import com.tikkeul.mote.repository.SubscriptionHistoryRepository;
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
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    public StatsResponse getDailyStats(Admin admin, LocalDate startDate, LocalDate endDate) {
        List<DailyStatsResponse> dailyStatsList = new ArrayList<>();
        long periodTotalParkingRevenue = 0; // 기간 총 일반 주차 매출
        long periodTotalSubscriptionRevenue = 0; // 기간 총 정기권 매출
        long periodTotalEntries = 0;
        long periodTotalExits = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            var startOfDay = date.atStartOfDay();
            var endOfDay = date.atTime(LocalTime.MAX);

            // 일반 주차 매출 및 출차 대수
            List<ParkingHistory> historiesExitedToday = parkingHistoryRepository.findByAdminAndExitTimeBetween(admin, startOfDay, endOfDay);
            int dailyExits = historiesExitedToday.size();
            int dailyParkingRevenue = historiesExitedToday.stream()
                    .mapToInt(ParkingHistory::getFee).sum();

            // 입차 대수
            long entriesFromHistory = parkingHistoryRepository.countByAdminAndEntryTimeBetween(admin, startOfDay, endOfDay);
            long entriesFromPark = parkRepository.countByAdminAndTimestampBetween(admin, startOfDay, endOfDay);
            int dailyEntries = (int) (entriesFromHistory + entriesFromPark);

            // 정기권 매출
            List<SubscriptionHistory> subscriptionsStartedToday = subscriptionHistoryRepository.findByAdminAndHistoryStartDateBetween(admin, date, date);
            int dailySubscriptionRevenue = subscriptionsStartedToday.stream().mapToInt(SubscriptionHistory::getSubHistoryPrice).sum();

            dailyStatsList.add(DailyStatsResponse.builder()
                    .date(date)
                    .totalRevenue(dailyParkingRevenue) // 분리된 매출로 저장
                    .subscriptionRevenue(dailySubscriptionRevenue) // 분리된 매출로 저장
                    .totalEntries(dailyEntries)
                    .totalExits(dailyExits)
                    .build());

            // 기간별 총합에도 분리하여 합산
            periodTotalParkingRevenue += dailyParkingRevenue;
            periodTotalSubscriptionRevenue += dailySubscriptionRevenue;
            periodTotalEntries += dailyEntries;
            periodTotalExits += dailyExits;
        }

        return StatsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(periodTotalParkingRevenue) // 분리된 총매출로 응답
                .totalSubscriptionRevenue(periodTotalSubscriptionRevenue) // 분리된 총매출로 응답
                .totalEntries(periodTotalEntries)
                .totalExits(periodTotalExits)
                .dailyStats(dailyStatsList)
                .build();
    }
}