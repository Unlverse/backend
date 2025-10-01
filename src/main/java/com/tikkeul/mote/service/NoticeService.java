package com.tikkeul.mote.service;

import com.tikkeul.mote.dto.NoticeDetailResponse; // DTO import 추가
import com.tikkeul.mote.dto.NoticeListResponse;   // DTO import 추가
import com.tikkeul.mote.entity.Notice;
import com.tikkeul.mote.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public List<NoticeListResponse> getAllNotices(String keyword) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Notice> notices;

        if (keyword == null || keyword.isBlank()) {
            notices = noticeRepository.findAll(sort);
        } else {
            notices = noticeRepository.findByTitleContainingOrContentContaining(keyword, keyword, sort);
        }

        // Notice 엔터티를 NoticeListResponse DTO로 변환하여 반환
        return notices.stream()
                .map(NoticeListResponse::new)
                .collect(Collectors.toList());
    }

    public NoticeDetailResponse getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항을 찾을 수 없습니다."));

        // Notice 엔터티를 NoticeDetailResponse DTO로 변환하여 반환
        return new NoticeDetailResponse(notice);
    }
}