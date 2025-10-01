package com.tikkeul.mote.controller;

import com.tikkeul.mote.dto.NoticeDetailResponse; // DTO import 추가
import com.tikkeul.mote.dto.NoticeListResponse;   // DTO import 추가
import com.tikkeul.mote.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // PathVariable import 추가

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<List<NoticeListResponse>> getAllNotices(
            @RequestParam(value = "keyword", required = false) String keyword) {

        List<NoticeListResponse> notices = noticeService.getAllNotices(keyword);
        return ResponseEntity.ok(notices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeDetailResponse> getNoticeById(@PathVariable Long id) {
        NoticeDetailResponse notice = noticeService.getNoticeById(id);
        return ResponseEntity.ok(notice);
    }
}