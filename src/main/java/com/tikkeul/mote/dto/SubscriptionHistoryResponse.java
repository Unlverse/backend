package com.tikkeul.mote.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tikkeul.mote.entity.SubscriptionHistory;
import com.tikkeul.mote.entity.SubscriptionStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class SubscriptionHistoryResponse {

    private final Long id;
    private final String plate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate endDate;
    private final String price;
    private final String memo;
    private final SubscriptionStatus status;
    private final String refundAmount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime refundedAt;

    public SubscriptionHistoryResponse(SubscriptionHistory history) {
        this.id = history.getSubHistoryId();
        this.plate = history.getSubHistoryPlate();
        this.startDate = history.getHistoryStartDate();
        this.endDate = history.getHistoryEndDate();
        this.price = String.format("%,d원", history.getSubHistoryPrice());
        this.memo = history.getHistoryMemo();
        this.status = history.getStatus();
        // 환불 금액이 0이 아니면 포맷팅, 아니면 null
        this.refundAmount = (history.getRefundAmount() != null && history.getRefundAmount() > 0)
                ? String.format("%,d원", history.getRefundAmount())
                : null;
        this.refundedAt = history.getRefundedAt();
    }
}