package edu.bbte.daygig.backend.controller;

import edu.bbte.daygig.backend.dto.auth.AuthResponseDto;
import edu.bbte.daygig.backend.dto.user.UserLoginInDto;
import edu.bbte.daygig.backend.dto.user.UserRegisterInDto;
import edu.bbte.daygig.backend.mappers.AuthMapper;
import edu.bbte.daygig.backend.mappers.UserMapper;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.security.JwtService;
import edu.bbte.daygig.backend.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody UserLoginInDto loginInDto) {
        User authenticatedUser = authService.login(authMapper.fromLoginDto(loginInDto));
        return new AuthResponseDto(jwtService.generateToken(authenticatedUser), userMapper.toDto(authenticatedUser));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@Valid @RequestBody UserRegisterInDto registerInDto) {
        User registeredUser = authService.register(userMapper.fromRegisterDto(registerInDto));
        return new AuthResponseDto(jwtService.generateToken(registeredUser), userMapper.toDto(registeredUser));
    }
}
