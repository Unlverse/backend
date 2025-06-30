package com.tikkeul.mote.dto;

import com.tikkeul.mote.entity.EntryRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryRequestResponse {

    private Long entryId;
    private String parkingLotName;
    private String newPlate;

    public static EntryRequestResponse fromEntity(EntryRequest entity, String parkingLotName) {
        EntryRequestResponse dto = new EntryRequestResponse();
        dto.setEntryId(entity.getEntryId());
        dto.setNewPlate(entity.getNewPlate());
        dto.setParkingLotName(parkingLotName);
        return dto;
    }
}
