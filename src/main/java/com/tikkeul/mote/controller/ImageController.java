package com.tikkeul.mote.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.security.AdminDetails;
import com.tikkeul.mote.service.ImageService;
import com.tikkeul.mote.service.ParkService;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.imaging.ImageReadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AdminDetails adminDetails
    ) throws IOException, ImageReadException {

        // 서버에 이미지 저장
        String projectRoot = System.getProperty("user.dir");
        String uploadDir = projectRoot + File.separator + "uploads";

        // 디렉토리가 없으면 생성
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        // 파일명 생성
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File savedFile = new File(uploadDir, filename);

        // 저장할 이미지 경로 문자열 (예: 서버 경로 or public URL)
        String imagePath = "/uploads/" + filename; // 프론트가 접근할 수 있는 경로로 설정

        try {
            // 1. 이미지 저장
            file.transferTo(savedFile);

            // 2. GPS 추출
            Map<String, Object> gpsInfo = imageService.extractGpsInfo(savedFile);

            // 3. OCR 추출
            String ocrJson = imageService.sendToOcrServer(savedFile);
            Map<String, Object> ocrResult = objectMapper.readValue(ocrJson, new TypeReference<>() {});

            // 4. DB 저장
            Admin admin = adminDetails.getAdmin();
            parkService.savePark(admin, gpsInfo, ocrResult, imagePath);

            // 5. 성공 응답
            return ResponseEntity.ok(Map.of(
                    "gps", gpsInfo,
                    "ocr", ocrResult,
                    "imagePath", imagePath
            ));

        } catch (Exception e) {
            // 실패 시 이미지 삭제
            if (savedFile.exists()) {
                savedFile.delete();
            }

            // 에러 메시지 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
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