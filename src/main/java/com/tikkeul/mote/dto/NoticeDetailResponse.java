package com.tikkeul.mote.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tikkeul.mote.entity.Notice;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeDetailResponse {

    private final Long noticeId; // 프론트엔드와 호환성을 위해 필드명은 id로 유지
    private final String title;
    private final String content;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDateTime createdAt;

    public NoticeDetailResponse(Notice notice) {
        this.noticeId = notice.getNoticeId(); // 엔터티의 noticeId를 가져와서 id에 할당
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.createdAt = notice.getCreatedAt();
    }
}