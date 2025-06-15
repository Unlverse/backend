package com.tikkeul.mote.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tikkeul.mote.entity.Park;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkResponse {
    private Long parkId;
    private String plate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime timestamp;

    private String duration;
    private String fee;

    public static ParkResponse fromEntity(Park park, int pricePerMinute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime enterTime = park.getTimestamp();

        Duration duration = Duration.between(enterTime, now);
        long minutes = duration.toMinutes();
        long hours = minutes / 60;
        long remainMinutes = minutes % 60;

        String durationStr = String.format("%d시간 %d분", hours, remainMinutes);

        int rawFee = (int) minutes * pricePerMinute;

        String formattedFee = String.format("%,d원", rawFee);

        return ParkResponse.builder()
                .parkId(park.getParkId())
                .plate(park.getPlate())
                .timestamp(enterTime)
                .duration(durationStr)
                .fee(formattedFee)
                .build();
    }
}
