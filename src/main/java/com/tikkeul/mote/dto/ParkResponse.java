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
    private String imagePath;

    public static ParkResponse fromEntity(Park park, int basePrice, int pricePerMinute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime enterTime = park.getTimestamp(); // 입차 시간

        Duration duration = Duration.between(enterTime, now);
        long minutes = duration.toMinutes();
        long hours = minutes / 60;
        long remainMinutes = minutes % 60;

        String durationStr = String.format("%d시간 %d분", hours, remainMinutes);

        // 요금 계산: 기본요금 + 분당요금 * 분
        int rawFee = basePrice + (int) minutes * pricePerMinute;
        String formattedFee = String.format("%,d원", rawFee);

        return ParkResponse.builder()
                .parkId(park.getParkId())
                .plate(park.getPlate())
                .timestamp(enterTime)
                .duration(durationStr)
                .fee(formattedFee)
                .imagePath(park.getImagePath())
                .build();
    }
}
