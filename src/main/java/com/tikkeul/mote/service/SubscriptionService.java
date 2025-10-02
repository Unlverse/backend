package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.SubscriptionRequest;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Subscription;
import com.tikkeul.mote.entity.SubscriptionHistory;
import com.tikkeul.mote.repository.SubscriptionHistoryRepository;
import com.tikkeul.mote.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        saveSubscriptionHistory(savedSubscription); // 등록 시 이력 테이블에 저장
        return savedSubscription;
    }

    public List<Subscription> getSubscriptions(Admin admin) {
        return subscriptionRepository.findByAdmin(admin);
    }

    @Transactional
    public Optional<Subscription> updateSubscription(Long id, SubscriptionRequest request, Admin admin) {
        return subscriptionRepository.findById(id).map(subscription -> {
            // 권한 확인
            if (!subscription.getAdmin().getAdminId().equals(admin.getAdminId())) {
                throw new SecurityException("해당 항목을 수정할 권한이 없습니다.");
            }
            subscription.setSubPlate(request.getSubPlate());
            subscription.setStartDate(request.getStartDate());
            subscription.setEndDate(request.getEndDate());
            subscription.setSubPrice(request.getSubPrice());
            subscription.setMemo(request.getMemo());
            return subscriptionRepository.save(subscription);
        });
    }

    @Transactional
    public void removeSubscription(Long id, Admin admin) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 항목이 존재하지 않습니다."));

        // 권한 확인
        if (!subscription.getAdmin().getAdminId().equals(admin.getAdminId())) {
            throw new SecurityException("해당 항목을 삭제할 권한이 없습니다.");
        }
        subscriptionRepository.delete(subscription);
    }

    @Transactional
    public void deleteSelectedSubscriptions(Admin admin, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("삭제할 항목을 선택해주세요.");
        }
        List<Subscription> subscriptions = subscriptionRepository.findAllById(ids);
        for (Subscription subscription : subscriptions) {
            if (!subscription.getAdmin().getAdminId().equals(admin.getAdminId())) {
                throw new SecurityException("해당 항목을 삭제할 권한이 없습니다.");
            }
        }
        subscriptionRepository.deleteAllByIdInBatch(ids);
    }

    @Transactional
    public void deleteAllSubscriptions(Admin admin) {
        long count = subscriptionRepository.countByAdmin(admin);
        if (count == 0) {
            throw new IllegalStateException("삭제할 항목이 없습니다.");
        }
        subscriptionRepository.deleteByAdmin(admin);
    }

    @Transactional
    public void cleanupExpiredSubscriptions() {
        LocalDate today = LocalDate.now();
        subscriptionRepository.deleteByEndDateBefore(today);
        System.out.println("만료된 정기권 정리 완료.");
    }

    private void saveSubscriptionHistory(Subscription subscription) {
        SubscriptionHistory history = SubscriptionHistory.builder()
                .admin(subscription.getAdmin())
                .subHistoryPlate(subscription.getSubPlate())
                .historyStartDate(subscription.getStartDate())
                .historyEndDate(subscription.getEndDate())
                .subHistoryPrice(subscription.getSubPrice())
                .historyMemo(subscription.getMemo())
                .build();
        subscriptionHistoryRepository.save(history);
    }
}