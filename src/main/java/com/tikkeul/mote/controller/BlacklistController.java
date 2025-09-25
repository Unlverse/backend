package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.BlacklistPlateRequest;
import com.tikkeul.mote.dto.BlacklistFromParkRequest;
import com.tikkeul.mote.dto.BlacklistPlateResponse;
import com.tikkeul.mote.dto.DeleteItemsRequest;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.security.AdminDetails;
import com.tikkeul.mote.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    // 차량번호로 블랙리스트 등록
    @PostMapping("/plate")
    public ResponseEntity<?> addBlacklistByPlate(
            @RequestBody BlacklistPlateRequest request,
            @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            if (adminDetails == null || adminDetails.getAdmin() == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            Admin admin = adminDetails.getAdmin();
            blacklistService.addByPlate(admin, request.getPlate(), request.getReason());
            return ResponseEntity.ok("블랙리스트에 추가되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

    // ParkId로 블랙리스트 등록
    @PostMapping("/{parkId}")
    public ResponseEntity<?> addBlacklistFromPark(
            @PathVariable(name = "parkId") Long parkId,
            @RequestBody BlacklistFromParkRequest request,
            @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            if (adminDetails == null || adminDetails.getAdmin() == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            Admin admin = adminDetails.getAdmin();
            blacklistService.addFromPark(admin, parkId, request.getReason());
            return ResponseEntity.ok("블랙리스트에 추가되었습니다.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

    // 블랙리스트 조회
    @GetMapping
    public ResponseEntity<?> getBlacklist(@AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            if (adminDetails == null || adminDetails.getAdmin() == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            Admin admin = adminDetails.getAdmin();
            List<BlacklistPlateResponse> blacklist = blacklistService.getBlacklist(admin);
            return ResponseEntity.ok(blacklist);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

    @PatchMapping("/{blackId}")
    public ResponseEntity<?> updateBlacklistReason(
            @PathVariable(name = "blackId") Long blackId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal AdminDetails adminDetails) {

        try {
            String newReason = request.get("reason");
            blacklistService.updateReason(adminDetails.getAdmin(), blackId, newReason);
            return ResponseEntity.ok("사유가 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // 블랙리스트 차량 삭제
    @DeleteMapping("/{blackId}")
    public ResponseEntity<?> deleteBlacklist(
            @PathVariable(name = "blackId") Long blackId,
            @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            if (adminDetails == null || adminDetails.getAdmin() == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            Admin admin = adminDetails.getAdmin();
            blacklistService.delete(admin, blackId);
            return ResponseEntity.ok("삭제 완료");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("해당 차량이 블랙리스트에 존재하지 않습니다.");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body("본인 블랙리스트의 항목만 삭제할 수 있습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

    @DeleteMapping("/selected")
    public ResponseEntity<String> deleteSelectedBlacklists(
            @RequestBody DeleteItemsRequest request,
            @AuthenticationPrincipal AdminDetails adminDetails) {
        try {
            blacklistService.deleteSelectedBlacklists(adminDetails.getAdmin(), request.getIds());
            return ResponseEntity.ok("선택된 항목이 삭제되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAll(
            @AuthenticationPrincipal AdminDetails adminDetails
    ) {
        try {
            if (adminDetails == null || adminDetails.getAdmin() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
            }
            Map<String, Object> result = blacklistService.deleteAllBlacklist(adminDetails.getAdmin());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류: " + e.getMessage()));
        }
    }
}
