package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.ParkListResponse;
import com.tikkeul.mote.dto.ParkManualRequest;
import com.tikkeul.mote.dto.ParkResponse;
import com.tikkeul.mote.dto.ParkUpdateRequest;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.entity.Park;
import com.tikkeul.mote.exception.FullParkingLotException;
import com.tikkeul.mote.security.AdminDetails;
import com.tikkeul.mote.service.ParkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/park")
@RequiredArgsConstructor
public class ParkController {

    private final ParkService parkService;

    @GetMapping
    public ResponseEntity<ParkListResponse> getMyParkLogs(@AuthenticationPrincipal AdminDetails adminDetails) {
        Admin admin = adminDetails.getAdmin();
        ParkListResponse response = parkService.getParkListWithStatus(admin);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/manual")
    public ResponseEntity<?> manualSavePark(
            @AuthenticationPrincipal AdminDetails adminDetails,
            @RequestBody ParkManualRequest request,
            @RequestParam(value = "force", defaultValue = "false") boolean force) {

        Admin admin = adminDetails.getAdmin();

        try {
            parkService.manualSavePark(admin, request.getPlate(), force);
            return ResponseEntity.ok(Map.of("message", "차량이 등록되었습니다."));
        } catch (IllegalArgumentException | IllegalStateException | FullParkingLotException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/{parkId}")
    public ResponseEntity<?> deletePark(@PathVariable("parkId") Long parkId,
                                        @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            if (adminDetails == null || adminDetails.getAdmin() == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            parkService.deletePark(parkId, adminDetails.getAdmin());
            return ResponseEntity.ok("삭제 완료");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("해당 주차 정보를 찾을 수 없습니다.");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body("본인이 올린 주차 정보만 삭제할 수 있습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

    @PatchMapping("/{parkId}")
    public ResponseEntity<?> updatePlate(@PathVariable("parkId") Long parkId,
                                         @RequestBody ParkUpdateRequest request,
                                         @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            parkService.updatePlate(parkId, request.getPlate(), adminDetails.getAdmin());
            return ResponseEntity.ok("차량번호가 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("해당 주차 정보를 찾을 수 없습니다.");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body("본인의 주차 정보만 수정할 수 있습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }
}