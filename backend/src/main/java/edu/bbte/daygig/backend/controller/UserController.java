package edu.bbte.daygig.backend.controller;

import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.user.BioUpdateInDto;
import edu.bbte.daygig.backend.dto.user.UserOutDto;
import edu.bbte.daygig.backend.dto.user.UserProfileOutDto;
import edu.bbte.daygig.backend.mappers.UserMapper;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.model.UserAvatar;
import edu.bbte.daygig.backend.security.JwtPrincipal;
import edu.bbte.daygig.backend.service.avatar.UserAvatarService;
import edu.bbte.daygig.backend.service.user.UserProfileService;
import edu.bbte.daygig.backend.service.user.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserAvatarService userAvatarService;
    private final UserMapper userMapper;
    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public UserOutDto getCurrentUser(@AuthenticationPrincipal JwtPrincipal principal) {
        UUID userId = currentUserId(principal);
        return userMapper.toDto(userService.getById(userId), userAvatarService.getAvatarUrl(userId));
    }

    @PatchMapping("/me")
    public UserOutDto updateBio(@Valid @RequestBody BioUpdateInDto bioUpdateInDto,
                                @AuthenticationPrincipal JwtPrincipal principal) {
        UUID userId = currentUserId(principal);
        User updatedUser = userService.updateBio(userId, bioUpdateInDto.bio());
        return userMapper.toDto(updatedUser, userAvatarService.getAvatarUrl(userId));
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserOutDto updateAvatar(@RequestPart("file") MultipartFile file,
                                   @AuthenticationPrincipal JwtPrincipal principal) {
        UUID userId = currentUserId(principal);
        userAvatarService.updateAvatar(userId, file);
        return userMapper.toDto(userService.getById(userId), userAvatarService.getAvatarUrl(userId));
    }

    @DeleteMapping("/me/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvatar(@AuthenticationPrincipal JwtPrincipal principal) {
        userAvatarService.deleteAvatar(currentUserId(principal));
    }

    @GetMapping("/{id}/profile")
    public UserProfileOutDto getProfile(@PathVariable UUID id) {
        return userProfileService.getProfile(id);
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> getAvatar(@PathVariable UUID id) {
        UserAvatar avatar = userAvatarService.getAvatar(id);
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(avatar.getContentType()))
                .cacheControl(CacheControl.noCache())
                .body(avatar.getImageData());
    }

    private UUID currentUserId(JwtPrincipal principal) {
        return UUID.fromString(principal.uuid());
    }
}
