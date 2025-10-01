package com.tikkeul.mote.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class SubscriptionRequest {
    private String subPlate;
    private LocalDate startDate;
    private LocalDate endDate;
    private int subPrice;
    private String memo;
}