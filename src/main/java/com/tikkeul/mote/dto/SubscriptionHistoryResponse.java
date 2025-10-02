package com.tikkeul.mote.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tikkeul.mote.entity.SubscriptionHistory;
import lombok.Getter;

import java.time.LocalDate;

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

    public SubscriptionHistoryResponse(SubscriptionHistory history) {
        this.id = history.getSubHistoryId();
        this.plate = history.getSubHistoryPlate();
        this.startDate = history.getHistoryStartDate();
        this.endDate = history.getHistoryEndDate();
        this.price = String.format("%,d원", history.getSubHistoryPrice());
        this.memo = history.getHistoryMemo();
    }
}