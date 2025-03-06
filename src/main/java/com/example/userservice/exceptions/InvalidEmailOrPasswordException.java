package com.example.userservice.exceptions;

public class InvalidEmailOrPasswordException extends Exception {
    public InvalidEmailOrPasswordException(String message) {
        super(message);
    }
}