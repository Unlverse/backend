package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.DeleteItemsRequest;
import com.tikkeul.mote.dto.RefundRequest;
import com.tikkeul.mote.dto.SubscriptionRequest;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Subscription;
import com.tikkeul.mote.security.AdminDetails;
import com.tikkeul.mote.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<?> addSubscription(@AuthenticationPrincipal AdminDetails adminDetails, @RequestBody SubscriptionRequest request) {
        try {
            Admin admin = adminDetails.getAdmin();
            Subscription subscription = subscriptionService.addSubscription(admin, request);
            // 성공 시 201 Created 상태 코드와 함께 메시지 및 데이터 반환
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "정기권이 등록되었습니다.", "subscription", subscription));
        } catch (IllegalStateException e) {
            // 중복 등록과 같은 비즈니스 로직 오류
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Subscription>> getSubscriptions(@AuthenticationPrincipal AdminDetails adminDetails) {
        Admin admin = adminDetails.getAdmin();
        return ResponseEntity.ok(subscriptionService.getSubscriptions(admin));
    }

    @PostMapping("/refund/{id}")
    public ResponseEntity<?> refundSubscription(
            @PathVariable Long id,
            @RequestBody RefundRequest request,
            @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            subscriptionService.refundSubscription(id, request.getAmount(), adminDetails.getAdmin());
            return ResponseEntity.ok(Map.of("message", id + "번 정기권이 " + String.format("%,d", request.getAmount()) + "원 환불 처리되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}