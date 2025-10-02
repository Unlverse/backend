package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.SubscriptionHistoryResponse;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.SubscriptionHistory;
import com.tikkeul.mote.repository.SubscriptionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionHistoryService {

    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    public List<SubscriptionHistoryResponse> getHistories(Admin admin, LocalDate startDate, LocalDate endDate, String keyword) {
        List<SubscriptionHistory> histories;

        // 차량 번호 검색어가 있는지 없는지에 따라 다른 메소드 호출
        if (keyword == null || keyword.isBlank()) {
            // 기간으로만 조회
            histories = subscriptionHistoryRepository.findByAdminAndHistoryStartDateBetweenOrderByHistoryStartDateDesc(admin, startDate, endDate);
        } else {
            // 기간 + 차량 번호로 조회
            histories = subscriptionHistoryRepository.findByAdminAndHistoryStartDateBetweenAndSubHistoryPlateContainingOrderByHistoryStartDateDesc(admin, startDate, endDate, keyword);
        }

        return histories.stream()
                .map(SubscriptionHistoryResponse::new)
                .collect(Collectors.toList());
    }
}