package edu.bbte.daygig.backend.controller;

import edu.bbte.daygig.backend.dto.user.UserLoginInDto;
import edu.bbte.daygig.backend.dto.user.UserOutDto;
import edu.bbte.daygig.backend.dto.user.UserRegisterInDto;
import edu.bbte.daygig.backend.mappers.UserMapper;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.security.AuthCookieService;
import edu.bbte.daygig.backend.security.JwtPrincipal;
import edu.bbte.daygig.backend.security.JwtService;
import edu.bbte.daygig.backend.service.auth.AuthService;
import edu.bbte.daygig.backend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthCookieService authCookieService;
    private final UserMapper userMapper;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<UserOutDto> login(@Valid @RequestBody UserLoginInDto loginInDto) {
        User authenticatedUser = authService.login(loginInDto.getEmail(), loginInDto.getPassword());
        return buildAuthResponse(authenticatedUser, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody UserRegisterInDto registerInDto) {
        authService.register(userMapper.fromRegisterDto(registerInDto));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity
                .noContent()
                .header(HttpHeaders.SET_COOKIE, authCookieService.clearAccessTokenCookie())
                .build();
    }

    @GetMapping("/me")
    public UserOutDto me(@AuthenticationPrincipal JwtPrincipal principal) {
        return userMapper.toDto(userService.getById(UUID.fromString(principal.uuid())));
    }

    private ResponseEntity<UserOutDto> buildAuthResponse(User user,
            HttpStatus status) {
        String token = jwtService.generateToken(user);

        return ResponseEntity
                .status(status)
                .header(HttpHeaders.SET_COOKIE, authCookieService.createAccessTokenCookie(token))
                .body(userMapper.toDto(user));
    }
}
