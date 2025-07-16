package com.tikkeul.mote.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tikkeul.mote.entity.Blacklist;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BlacklistPlateResponse {

    private Long blackId;
    private String blackPlate;
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime blackTimestamp;

    public static BlacklistPlateResponse fromEntity(Blacklist entity) {
        return BlacklistPlateResponse.builder()
                .blackId(entity.getBlackId())
                .blackPlate(entity.getBlackPlate())
                .reason(entity.getReason())
                .blackTimestamp(entity.getBlackTimestamp())
                .build();
    }
}
