package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.SubscriptionRequest;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Subscription;
import com.tikkeul.mote.entity.SubscriptionHistory;
import com.tikkeul.mote.entity.SubscriptionStatus;
import com.tikkeul.mote.repository.SubscriptionHistoryRepository;
import com.tikkeul.mote.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    @Transactional
    public Subscription addSubscription(Admin admin, SubscriptionRequest request) {
        // 중복 등록 확인
        subscriptionRepository.findByAdminAndSubPlate(admin, request.getSubPlate()).ifPresent(s -> {
            throw new IllegalStateException("해당차량은 이미 정기주차 차량입니다.");
        });

        Subscription subscription = Subscription.builder()
                .admin(admin)
                .subPlate(request.getSubPlate())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .subPrice(request.getSubPrice())
                .memo(request.getMemo())
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        saveSubscriptionHistory(savedSubscription, SubscriptionStatus.PAID);
        return savedSubscription;
    }

    public List<Subscription> getSubscriptions(Admin admin) {
        return subscriptionRepository.findByAdmin(admin);
    }

    @Transactional
    public void refundSubscription(Long subscriptionId, int refundAmount, Admin admin) {
        // 1. 활성 정기권을 찾음
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("이미 처리되었거나 존재하지 않는 정기권입니다."));

        if (!subscription.getAdmin().getAdminId().equals(admin.getAdminId())) {
            throw new SecurityException("환불을 처리할 권한이 없습니다.");
        }

        // 2. 이력 테이블에서 해당 기록을 찾아 'REFUNDED'로 상태 변경
        SubscriptionHistory history = subscriptionHistoryRepository
                .findByAdminAndSubHistoryPlateAndHistoryStartDate(admin, subscription.getSubPlate(), subscription.getStartDate())
                .orElseThrow(() -> new IllegalStateException("환불할 정기권의 구매 이력을 찾을 수 없습니다."));

        if (refundAmount > history.getSubHistoryPrice() || refundAmount < 0) {
            throw new IllegalArgumentException("환불 금액이 유효하지 않습니다.");
        }

        history.setStatus(SubscriptionStatus.REFUNDED);
        history.setRefundAmount(refundAmount);
        history.setRefundedAt(LocalDateTime.now());
        subscriptionHistoryRepository.save(history);

        // 3. 활성 목록에서 최종적으로 삭제
        subscriptionRepository.delete(subscription);
    }


    @Transactional
    public void cleanupExpiredSubscriptions() {
        LocalDate today = LocalDate.now();
        subscriptionRepository.deleteByEndDateBefore(today);
        System.out.println("만료된 정기권을 삭제했습니다.");
    }

    private void saveSubscriptionHistory(Subscription subscription, SubscriptionStatus status) {
        SubscriptionHistory history = SubscriptionHistory.builder()
                .admin(subscription.getAdmin())
                .subHistoryPlate(subscription.getSubPlate())
                .historyStartDate(subscription.getStartDate())
                .historyEndDate(subscription.getEndDate())
                .subHistoryPrice(subscription.getSubPrice())
                .historyMemo(subscription.getMemo())
                .status(status)
                .refundAmount(0)
                .build();
        subscriptionHistoryRepository.save(history);
    }
}