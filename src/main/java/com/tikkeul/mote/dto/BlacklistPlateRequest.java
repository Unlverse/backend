package com.tikkeul.mote.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlacklistPlateRequest {
    private String plate;
    private String reason;
}
