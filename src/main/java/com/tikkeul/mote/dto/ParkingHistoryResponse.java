package com.tikkeul.mote.dto;

import com.tikkeul.mote.entity.ParkingHistory;
import lombok.Getter;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

@Getter
public class ParkingHistoryResponse {


    private final Long historyId;
    private final String historyPlate;
    private final String entryTime;
    private final String exitTime;
    private final String parkingDuration;
    private final int fee;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ParkingHistoryResponse(ParkingHistory history) {
        this.historyId = history.getHistoryId();
        this.historyPlate = history.getHistoryPlate();
        this.entryTime = history.getEntryTime().format(FORMATTER);
        this.exitTime = history.getExitTime().format(FORMATTER);
        this.fee = history.getFee();

        Duration duration = Duration.between(history.getEntryTime(), history.getExitTime());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        this.parkingDuration = String.format("%d시간 %d분", hours, minutes);
    }
}
