package com.tikkeul.mote.exception;

import lombok.Getter;

@Getter
public class BlacklistConflictException extends RuntimeException {
    private final String plate;

    public BlacklistConflictException(String message, String plate) {
        super(message);
        this.plate = plate;
    }
}