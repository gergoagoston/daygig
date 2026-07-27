package edu.bbte.daygig.backend.controller;

import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.user.BioUpdateInDto;
import edu.bbte.daygig.backend.dto.user.UserOutDto;
import edu.bbte.daygig.backend.dto.user.UserProfileOutDto;
import edu.bbte.daygig.backend.mappers.UserMapper;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.security.JwtPrincipal;
import edu.bbte.daygig.backend.service.user.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public UserOutDto getCurrentUser(@AuthenticationPrincipal JwtPrincipal principal) {
        return userMapper.toDto(userService.getById(currentUserId(principal)));
    }

    @PatchMapping("/me")
    public UserOutDto updateBio(@Valid @RequestBody BioUpdateInDto bioUpdateInDto,
                                @AuthenticationPrincipal JwtPrincipal principal) {
        User updatedUser = userService.updateBio(currentUserId(principal), bioUpdateInDto.bio());
        return userMapper.toDto(updatedUser);
    }

    @GetMapping("/{id}/profile")
    public UserProfileOutDto getProfile(@PathVariable UUID id) {
        return userMapper.toProfileDto(userService.getById(id));
    }

    private UUID currentUserId(JwtPrincipal principal) {
        return UUID.fromString(principal.uuid());
    }
}
