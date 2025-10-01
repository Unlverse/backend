package com.tikkeul.mote.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // 생성일자 자동 기입을 위해 추가
public class Notice {

    @Id
    @Column(name = "notice_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob // 긴 텍스트를 저장할 수 있도록 설정
    @Column(nullable = false)
    private String content;

    @CreatedDate // 엔터티 생성 시 시각 자동 저장
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
