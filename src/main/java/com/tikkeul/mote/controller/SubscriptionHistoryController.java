package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.SubscriptionHistoryResponse;
import com.tikkeul.mote.security.AdminDetails;
import com.tikkeul.mote.service.SubscriptionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sub-history")
@RequiredArgsConstructor
public class SubscriptionHistoryController {

    private final SubscriptionHistoryService subscriptionHistoryService;

    @GetMapping
    public ResponseEntity<List<SubscriptionHistoryResponse>> getHistories(
            @AuthenticationPrincipal AdminDetails adminDetails,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            @RequestParam(value = "plate", required = false) String plate) {

        List<SubscriptionHistoryResponse> histories = subscriptionHistoryService.getHistories(adminDetails.getAdmin(), startDate, endDate, plate);
        return ResponseEntity.ok(histories);
    }
}