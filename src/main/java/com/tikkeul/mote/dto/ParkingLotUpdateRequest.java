package com.tikkeul.mote.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParkingLotUpdateRequest {
    private Integer basePrice;
    private Integer pricePerMinute;
    private Integer totalLot;
}
