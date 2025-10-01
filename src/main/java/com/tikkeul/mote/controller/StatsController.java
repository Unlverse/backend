package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.StatsResponse;
import com.tikkeul.mote.security.AdminDetails;
import com.tikkeul.mote.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<StatsResponse> getStats(
            @AuthenticationPrincipal AdminDetails adminDetails,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {

        StatsResponse stats = statsService.getDailyStats(adminDetails.getAdmin(), startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}