package com.tikkeul.mote.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NearbyParkingLotResponse {
    private Long adminId;        // parking_lot PK (= admin_id)
    private String name;         // parking_lot_name
    private double latitude;     // parking_lot_latitude;
    private double longitude;    // parking_lot_longitude;
    private int totalLot;
    private int pricePerMinute;
    private double distanceKm;   // DB 계산값(km)
}
