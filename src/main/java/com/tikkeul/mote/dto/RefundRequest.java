package com.tikkeul.mote.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundRequest {
    private int amount; // 관리자가 입력할 환불 금액
}