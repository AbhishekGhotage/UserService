package com.example.userservice.services;

import com.example.userservice.exceptions.*;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;

public interface UserService {
    Token login(String email, String password) throws InvalidEmailOrPasswordException, DeviceLimitExceededException;

    User signUp(String name, String email, String password) throws UserAlreadyExistsException;

    User validateToken(String tokenValue) throws InvalidTokenException;

    void logout(String tokenValue) throws InvalidTokenException;
}