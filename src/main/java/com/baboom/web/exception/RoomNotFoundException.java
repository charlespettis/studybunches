package com.baboom.web.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String code) {
        super("No room found with code: " + code);
    }
}
