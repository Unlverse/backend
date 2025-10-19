package com.tikkeul.mote.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminInfoResponse {
    private String phoneNumber;
    private Integer basePrice;
    private Integer pricePerMinute;
    private Integer totalLot;
}