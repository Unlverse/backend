package com.tikkeul.mote.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetConfirmRequest {
    private String phoneNumber;
    private String code;
    private String newPassword;
}