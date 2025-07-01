package com.tikkeul.mote.dto;

import com.tikkeul.mote.entity.EntryRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class EntryRequestResponse {

    private Long entryId;
    private String parkingLotName;
    private String newPlate;
    private String createdAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static EntryRequestResponse fromEntity(EntryRequest entity, String parkingLotName) {
        EntryRequestResponse dto = new EntryRequestResponse();
        dto.setEntryId(entity.getEntryId());
        dto.setNewPlate(entity.getNewPlate());
        dto.setParkingLotName(parkingLotName);
        dto.setCreatedAt(entity.getCreatedAt().format(FORMATTER));
        return dto;
    }
}
