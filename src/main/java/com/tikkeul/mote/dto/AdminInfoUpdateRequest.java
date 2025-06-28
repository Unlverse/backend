package com.tikkeul.mote.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminInfoUpdateRequest {
    private String phoneNumber;
    private Integer basePrice;
    private Integer pricePerMinute;
    private Integer totalLot;
}
