package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.DeleteItemsRequest;
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
            return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
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

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateSubscription(@PathVariable Long id, @RequestBody SubscriptionRequest request, @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            Admin admin = adminDetails.getAdmin();
            // 수정된 서비스 메소드 호출
            return subscriptionService.updateSubscription(id, request, admin)
                    .map(sub -> ResponseEntity.ok(Map.of("message", "해당 항목이 수정되었습니다.", "subscription", sub)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "해당 항목을 찾을 수 없습니다.")));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeSubscription(@PathVariable Long id, @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            Admin admin = adminDetails.getAdmin();
            // 수정된 서비스 메소드 호출
            subscriptionService.removeSubscription(id, admin);
            return ResponseEntity.ok(Map.of("message", "해당 항목이 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @DeleteMapping("/selected")
    public ResponseEntity<?> deleteSelectedSubscriptions(@RequestBody DeleteItemsRequest request, @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            subscriptionService.deleteSelectedSubscriptions(adminDetails.getAdmin(), request.getIds());
            return ResponseEntity.ok(Map.of("message", "선택된 항목이 삭제되었습니다."));
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllSubscriptions(@AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            subscriptionService.deleteAllSubscriptions(adminDetails.getAdmin());
            return ResponseEntity.ok(Map.of("message", "모든 항목이 삭제되었습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}