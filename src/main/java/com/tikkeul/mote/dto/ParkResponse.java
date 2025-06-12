package com.tikkeul.mote.dto;

import com.tikkeul.mote.entity.Park;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkResponse {
    private Long parkId;
    private String plate;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;

    public static ParkResponse fromEntity(Park park) {
        return ParkResponse.builder()
                .parkId(park.getParkId())
                .plate(park.getPlate())
                .latitude(park.getLatitude())
                .longitude(park.getLongitude())
                .timestamp(park.getTimestamp())
                .build();
    }
}
