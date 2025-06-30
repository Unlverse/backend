package com.tikkeul.mote.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryCreateRequest {
    private String parkingLotName;
    private String newPlate;
}
