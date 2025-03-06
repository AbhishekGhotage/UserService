package com.example.userservice.services;

import com.example.userservice.exceptions.*;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.repositories.TokenRepository;
import com.example.userservice.repositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserRepository userRepository;
    private TokenRepository tokenRepository;


    public UserServiceImpl(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository, TokenRepository tokenRepository) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public Token login(String email, String password) throws InvalidEmailOrPasswordException, DeviceLimitExceededException{

        // Check if the user exists or not in the dB and if the user exists in dB, then check whether the user has entered the
        // correct password or not.
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty() || !bCryptPasswordEncoder.matches(password, optionalUser.get().getPassword())) {
            // Throw an Exception OR Redirect to signup
            throw new InvalidEmailOrPasswordException("Invalid email or password.");
        }

        // User found in the dB.
        // Match the password.
//        User user = optionalUser.get();
//
//        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
//            // Password mismatch
//            throw new InvalidEmailOrPasswordException("Invalid email or password.");
//        }

        // Valid user found in the dB.
        User user = optionalUser.get();

        // Check the number of active tokens for the user.
        int activeTokenCount = tokenRepository.countByDeletedAndUserId(false, user.getId());
        if (activeTokenCount >= 2) {
            // Device limit exceeded
            throw new DeviceLimitExceededException("Device limit exceeded. Only 2 devices allowed.");
        }

        // Login success -> Generate Token
        Token token = new Token();
        token.setValue(RandomStringUtils.randomAlphanumeric(128)); // RandomStringUtils: This came from apache commons lang dependency.
        token.setUser(user);

        LocalDate localDate = LocalDate.now().plusDays(30);
        Date expiryDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        token.setExpiryAt(expiryDate);

        return tokenRepository.save(token);
    }

    @Override
    public User signUp (String name, String email, String password) throws UserAlreadyExistsException {

        // Check if the user already exists or not in the dB.
        Optional<User> optionalUser = userRepository.findByEmailAndDeleted(email, false);
        if (optionalUser.isPresent()) {
            throw new UserAlreadyExistsException("User already exists. Please try again with another email.");
        }

        // i.e., User does not exist in the dB.
        //BCryptPassword
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(bCryptPasswordEncoder.encode(password)); // This "bCryptPasswordEncoder" came from spring-boot-starter-security dependency.

        return userRepository.save(user);
    }

    @Override
    public User validateToken(String tokenValue) throws InvalidTokenException{

        // Token value should be present in the dB, deleted should be false and expiry time > current time
        Optional<Token> optionalToken = tokenRepository.findByValueAndDeletedAndExpiryAtGreaterThan(
                tokenValue,
                false,
                new Date()
        );

        if(optionalToken.isPresent()) {
            Token token = optionalToken.get();
            return token.getUser();
        }
        else {
            throw new InvalidTokenException("Your session key is either invalid or expired. Kindly login again.");
        }

//        Optional<Token> optionalToken = tokenRepository.findByValueAndDeletedAndExpiryAtGreaterThan(
//                tokenValue,
//                false,
//                new Date()
//        );
//
//        if (optionalToken.isEmpty()) {
//            return null;
//        }
//
//        return optionalToken.get().getUser();
    }

    @Override
    public void logout(String tokenValue) throws InvalidTokenException{
        Optional<Token> optionalToken = tokenRepository.findByValueAndDeletedAndExpiryAtGreaterThan(
                tokenValue,
                false,
                new Date()
        );
        if(optionalToken.isPresent()) {
            Token token = optionalToken.get();
            token.setDeleted(true);
            tokenRepository.save(token); // Here JPA will update the existing token only instead of creating a new token row with "deleted value as true"
            // because the token that we got from optionalToken and the token that we are saving in the dB tokenRepository.save(token) have the same
            // primary key, hence JPA will update the existing record only instead of creating a new one.
        }
        else {
            throw new InvalidTokenException("Your session key is either invalid or expired. Kindly login again.");
        }
    }
}