package com.tikkeul.mote.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingInfoResponse {
    private String carNumber;
    private String entryTime;
    private String currentParkingDuration;
    private int parkingFee;
    private String address;
    private String adminPhoneNumber;
}
