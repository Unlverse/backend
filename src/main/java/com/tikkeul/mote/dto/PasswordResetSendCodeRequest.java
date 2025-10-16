package com.tikkeul.mote.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetSendCodeRequest {
    private String username;
    private String phoneNumber;
}