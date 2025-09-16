package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.DeleteItemsRequest;
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
            entryRequestService.createRequest(request.getAdminId(), request.getNewPlate());
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

    @PostMapping("/accept/{entryId}")
    public ResponseEntity<String> acceptEntryRequest(
            @PathVariable("entryId") Long entryId,
            @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            entryRequestService.acceptRequest(entryId, adminDetails.getAdmin());
            return ResponseEntity.ok("입차 되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("요청 처리 실패: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("입차 처리 실패: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    @PostMapping("/accept/all")
    public ResponseEntity<?> acceptAllEntryRequests(
            @AuthenticationPrincipal AdminDetails adminDetails
    ) {
        try {
            entryRequestService.acceptAllRequests(adminDetails.getAdmin());
            return ResponseEntity.ok(Map.of("message", "모든 차량이 입차 되었습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("입차 처리 실패: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류: " + e.getMessage());
        }
    }

    @PostMapping("/accept/selected")
    public ResponseEntity<String> acceptSelectedEntryRequests(
            @RequestBody DeleteItemsRequest request,
            @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            entryRequestService.acceptSelectedRequests(adminDetails.getAdmin(), request.getIds());
            return ResponseEntity.ok("선택된 차량이 입차 되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("요청 처리 실패: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("입차 실패: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }
}
