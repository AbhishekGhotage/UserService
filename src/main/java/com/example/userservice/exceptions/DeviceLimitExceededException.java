package com.example.userservice.exceptions;

public class DeviceLimitExceededException extends Exception {
    public DeviceLimitExceededException(String message) {
        super(message);
    }
}