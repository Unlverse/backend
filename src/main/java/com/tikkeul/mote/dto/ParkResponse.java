package com.tikkeul.mote.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tikkeul.mote.entity.Park;
import com.tikkeul.mote.entity.ParkingLot;
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

    public static ParkResponse fromEntity(Park park, ParkingLot parkingLot, boolean isSubscription) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime enterTime = park.getTimestamp();

        // 주차 시간 계산
        Duration duration = Duration.between(enterTime, now);
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        String durationStr = String.format("%d시간 %d분", hours, minutes);
        String formattedFee;

        // 정기권 차량 여부에 따라 요금 포맷팅
        if (isSubscription) {
            formattedFee = "0원"; // 정기권 차량은 "정기권"으로 표시
        } else {
            // 요금 계산: 기본요금 + 분당요금 * 분
            if (totalMinutes > 0) {
                int rawFee = parkingLot.getBasePrice() + ((int) totalMinutes * parkingLot.getPricePerMinute());
                formattedFee = String.format("%,d원", rawFee);
            } else {
                formattedFee = String.format("%,d원", parkingLot.getBasePrice());
            }
        }

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
