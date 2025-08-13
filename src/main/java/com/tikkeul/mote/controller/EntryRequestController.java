package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.EntryCreateRequest;
import com.tikkeul.mote.dto.EntryRequestResponse;
import com.tikkeul.mote.entity.EntryRequest;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.service.EntryRequestService;
import com.tikkeul.mote.security.AdminDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/entry-request")
public class EntryRequestController {

    private final EntryRequestService entryRequestService;

    @PostMapping
    public ResponseEntity<String> createEntryRequest(@RequestBody EntryCreateRequest request) {
        try {
            entryRequestService.createRequest(request.getParkingLotName(), request.getNewPlate());
            return ResponseEntity.ok("입차 요청이 전달되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("요청 실패: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<EntryRequestResponse>> getMyEntryRequests(
            @AuthenticationPrincipal AdminDetails adminDetails) {
        Admin admin = adminDetails.getAdmin();
        List<EntryRequestResponse> list = entryRequestService.getRequestsForAdmin(admin);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<String> deleteEntryRequest(
            @PathVariable("entryId") Long entryId,
            @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            entryRequestService.deleteRequest(entryId, adminDetails.getAdmin());
            return ResponseEntity.ok("요청이 처리되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("삭제 실패: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류: " + e.getMessage());
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllEntryRequests(
            @AuthenticationPrincipal AdminDetails adminDetails
    ) {
        try {
            if (adminDetails == null || adminDetails.getAdmin() == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }
            Map<String, Object> result = entryRequestService.deleteAllRequests(adminDetails.getAdmin());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류: " + e.getMessage());
        }
    }
}
