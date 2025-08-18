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
    private String address;
    private double latitude;     // parking_lot_latitude;
    private double longitude;    // parking_lot_longitude;
    private double distanceKm;   // DB 계산값(km)
}
