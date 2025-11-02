package com.tikkeul.mote.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.exception.BlacklistConflictException;
import com.tikkeul.mote.security.AdminDetails;
import com.tikkeul.mote.service.ImageService;
import com.tikkeul.mote.service.ParkService;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.imaging.ImageReadException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ParkService parkService;
    private final ObjectMapper objectMapper;

    @PostMapping("/upload")
    public ResponseEntity<?> handleUpload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "force", defaultValue = "false") boolean force,
            @AuthenticationPrincipal AdminDetails adminDetails
    ) throws IOException {

        List<Map<String, Object>> results = new ArrayList<>();
        Admin admin = adminDetails.getAdmin();

        for (MultipartFile file : files) {
            // 1. 업로드 디렉토리 설정
            String projectRoot = System.getProperty("user.dir");
            String uploadDir = projectRoot + File.separator + "uploads";

            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }

            // 2. 파일명 생성 및 파일 객체 준비
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File savedFile = new File(uploadDir, filename);
            String imagePath = "/uploads/" + filename;

            try {
                // 3. 이미지 저장
                file.transferTo(savedFile);

                // 4. GPS 정보 추출
                Map<String, Object> gpsInfo = null;

                // Case 1: 프론트에서 좌표를 보낸 경우 (즉석 촬영)
                if (file == files[0] && latitude != null && longitude != null) {
                    gpsInfo = Map.of("latitude", latitude, "longitude", longitude);
                } else {
                    // Case 2: 프론트에서 좌표가 안 온 경우 (라이브러리)
                    gpsInfo = imageService.extractGpsInfo(savedFile);
                }

                // Case 3: Case 1, 2 모두 실패한 경우
                if (gpsInfo == null) {
                    throw new IllegalStateException("GPS 정보를 찾을 수 없습니다.");
                }

                // 5. OCR 정보 추출
                String ocrJson = imageService.sendToOcrServer(savedFile);
                Map<String, Object> ocrResult = objectMapper.readValue(ocrJson, new TypeReference<>() {});

                // 6. 주차 정보 저장
                parkService.savePark(admin, gpsInfo, ocrResult, imagePath, force);

                // 7. 성공 응답
                results.add(Map.of(
                        "fileName", file.getOriginalFilename(),
                        "status", "success",
                        "gps", gpsInfo,
                        "ocr", ocrResult,
                        "imagePath", imagePath
                ));

            } catch (BlacklistConflictException e) {
                if (savedFile.exists()) { savedFile.delete(); } // 실패 시 이미지 삭제

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("fileName", file.getOriginalFilename());
                errorResponse.put("status", "error");
                errorResponse.put("message", e.getMessage());
                errorResponse.put("plate", e.getPlate());
                results.add(errorResponse);

            } catch (Exception e) {
                // 그 외 모든 일반적인 예외 처리
                if (savedFile.exists()) { savedFile.delete(); } // 실패 시 이미지 삭제

                results.add(Map.of(
                        "fileName", file.getOriginalFilename(),
                        "status", "error",
                        "message", e.getMessage()
                ));
            }
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{parkId}")
    public ResponseEntity<Resource> getParkImage(
            @PathVariable("parkId") Long parkId,
            @AuthenticationPrincipal AdminDetails adminDetails) {

        Resource image = parkService.loadParkImageForAdmin(parkId, adminDetails.getAdmin());

        // 동적 타입 추정 예제
        String contentType = "image/jpeg"; // 기본값
        try {
            contentType = Files.probeContentType(image.getFile().toPath());
        } catch (IOException ignored) {}

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(image);
    }

    @GetMapping("/check")
    public String check() {
        return "이미지 업로드 API 작동 중";
    }
}