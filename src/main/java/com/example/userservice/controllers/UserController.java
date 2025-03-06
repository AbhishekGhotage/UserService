package com.example.userservice.controllers;

import com.example.userservice.dtos.*;
import com.example.userservice.exceptions.*;
import com.example.userservice.models.Token;
import com.example.userservice.models.User;
import com.example.userservice.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users") // localhost:8080/users/
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto requestDto) throws InvalidEmailOrPasswordException, DeviceLimitExceededException {
        Token token = userService.login(
                requestDto.getEmail(),
                requestDto.getPassword()
        );

        LoginResponseDto responseDto = new LoginResponseDto();
        responseDto.setToken(token.getValue());
        return responseDto;
    }

    @PostMapping("/signup")
    public UserDto signUp(@RequestBody SignUpRequestDto requestDto) throws UserAlreadyExistsException {
        User user = userService.signUp(
                requestDto.getName(),
                requestDto.getEmail(),
                requestDto.getPassword()
        );

        return UserDto.from(user);
    }

    @GetMapping("/logout/{tokenValue}")
    public ResponseEntity<String> logOut(@PathVariable("tokenValue") String tokenValue) throws InvalidTokenException {
        userService.logout(tokenValue);
        return ResponseEntity.ok("Logout successful.");
    }

    //localhost:8080/users/validate/token
    @GetMapping("/validate/{token}")
    public ResponseEntity<UserDto> validateToken(@PathVariable("token") String tokenValue) throws InvalidTokenException {
        User user = userService.validateToken(tokenValue);
        return ResponseEntity.ok(UserDto.from(user));

//        ResponseEntity<UserDto> responseEntity = null;
//        if (user == null) {
//            responseEntity = new ResponseEntity<>(
//                    null,
//                    HttpStatus.UNAUTHORIZED
//            );
//        } else {
//            responseEntity = new ResponseEntity<>(
//                    UserDto.from(user),
//                    HttpStatus.OK
//            );
//        }
//
//        return responseEntity;
    }
}