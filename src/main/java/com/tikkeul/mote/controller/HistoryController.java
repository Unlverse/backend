package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.ParkingHistoryResponse;
import com.tikkeul.mote.security.AdminDetails;
import com.tikkeul.mote.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    //  출차일 기준 조회
    @GetMapping("/by-exit-date")
    public ResponseEntity<List<ParkingHistoryResponse>> getExitHistoryByExitDate(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "plate", required = false) String plate,
            @AuthenticationPrincipal AdminDetails adminDetails) {

        List<ParkingHistoryResponse> history = historyService.getParkingHistoryByExitDate(
                adminDetails.getAdmin(),
                startDate,
                endDate,
                plate // 서비스로 전달
        );
        return ResponseEntity.ok(history);
    }

    //  입차일 기준 조회 API
    @GetMapping("/by-entry-date")
    public ResponseEntity<List<ParkingHistoryResponse>> getExitHistoryByEntryDate(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "plate", required = false) String plate,
            @AuthenticationPrincipal AdminDetails adminDetails) {

        List<ParkingHistoryResponse> history = historyService.getParkingHistoryByEntryDate(
                adminDetails.getAdmin(),
                startDate,
                endDate,
                plate // 서비스로 전달
        );
        return ResponseEntity.ok(history);
    }
}
