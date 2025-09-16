package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.ParkingHistoryResponse;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.ParkingHistory;
import com.tikkeul.mote.repository.ParkingHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryService {

    private final ParkingHistoryRepository parkingHistoryRepository;

    //  출차일 기준 조회
    public List<ParkingHistoryResponse> getParkingHistoryByExitDate(Admin admin, LocalDate startDate, LocalDate endDate, String carNumber) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<ParkingHistory> histories;
        if (carNumber != null && !carNumber.isBlank()) {
            // 차량 번호가 있으면 차량 번호로 검색
            histories = parkingHistoryRepository.findByAdminAndExitTimeBetweenAndHistoryPlateContainingOrderByHistoryIdDesc(admin, startDateTime, endDateTime, carNumber);
        } else {
            // 차량 번호가 없으면 전체 검색
            histories = parkingHistoryRepository.findByAdminAndExitTimeBetweenOrderByHistoryIdDesc(admin, startDateTime, endDateTime);
        }

        return histories.stream()
                .map(ParkingHistoryResponse::new)
                .collect(Collectors.toList());
    }

    //  입차일 기준 조회
    public List<ParkingHistoryResponse> getParkingHistoryByEntryDate(Admin admin, LocalDate startDate, LocalDate endDate, String carNumber) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<ParkingHistory> histories;
        if (carNumber != null && !carNumber.isBlank()) {
            // 차량 번호가 있으면 차량 번호로 검색
            histories = parkingHistoryRepository.findByAdminAndEntryTimeBetweenAndHistoryPlateContainingOrderByHistoryIdDesc(admin, startDateTime, endDateTime, carNumber);
        } else {
            // 차량 번호가 없으면 전체 검색
            histories = parkingHistoryRepository.findByAdminAndEntryTimeBetweenOrderByHistoryIdDesc(admin, startDateTime, endDateTime);
        }

        return histories.stream()
                .map(ParkingHistoryResponse::new)
                .collect(Collectors.toList());
    }
}