package com.tikkeul.mote.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikkeul.mote.entity.Admin;
import com.tikkeul.mote.security.AdminDetails;
import com.tikkeul.mote.service.ImageService;
import com.tikkeul.mote.service.ParkService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.imaging.ImageReadException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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

        File tempFile = File.createTempFile("upload-", file.getOriginalFilename());

        try {
            file.transferTo(tempFile);

            Map<String, Object> gpsInfo = imageService.extractGpsInfo(tempFile);
            String ocrJson = imageService.sendToOcrServer(tempFile);
            Map<String, Object> ocrResult = objectMapper.readValue(ocrJson, new TypeReference<>() {});

            Admin admin = adminDetails.getAdmin();
            parkService.savePark(admin, gpsInfo, ocrResult);

            return ResponseEntity.ok(Map.of(
                    "gps", gpsInfo,
                    "ocr", ocrResult
            ));
        } finally {
            tempFile.delete();
        }
    }

    @GetMapping("/check")
    public String check() {
        return "이미지 업로드 API 작동 중";
    }
}