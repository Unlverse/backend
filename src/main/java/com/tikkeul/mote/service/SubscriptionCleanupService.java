package com.tikkeul.mote.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionCleanupService {

    private final SubscriptionService subscriptionService;

    // 매일 자정에 만료된 정기권을 삭제하도록 SubscriptionService를 호출

    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledCleanup() {
        subscriptionService.cleanupExpiredSubscriptions();
    }

    // 애플리케이션이 완전히 시작된 후, 만료된 정기권 정리를 위해 SubscriptionService를 호출
    @EventListener(ApplicationReadyEvent.class)
    public void startupCleanup() {
        subscriptionService.cleanupExpiredSubscriptions();
    }
}