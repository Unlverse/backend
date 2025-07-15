package com.tikkeul.mote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class VisitorNotFoundResponse {
    private Long parkId;
    private String timestamp;
    private String duration;
}
