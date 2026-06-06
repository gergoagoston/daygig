package edu.bbte.daygig.backend.dto.auth;

import edu.bbte.daygig.backend.dto.user.UserOutDto;

public record AuthResponseDto(String token, UserOutDto user) {
}
