package com.tikkeul.mote.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sub_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionHistory {

    @Id
    @Column(name = "sub_history_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "sub_history_plate", nullable = false, length = 50)
    private String subHistoryPlate;

    @Column(name = "history_startDate", nullable = false)
    private LocalDate historyStartDate;

    @Column(name = "history_endDate", nullable = false)
    private LocalDate historyEndDate;

    @Column(name = "sub_history_price")
    private int subHistoryPrice;

    @Column(name = "history_memo", length = 500)
    private String historyMemo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status; // 거래 상태 (PAID 또는 REFUNDED)

    @Column(name = "refund_amount")
    private Integer refundAmount; // 환불 금액

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt; // 환불 처리 일시
}