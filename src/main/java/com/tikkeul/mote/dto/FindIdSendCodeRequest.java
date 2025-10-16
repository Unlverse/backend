package com.tikkeul.mote.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindIdSendCodeRequest {
    private String name;
    private String phoneNumber;
}