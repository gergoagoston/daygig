package edu.bbte.daygig.backend.controller;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.rating.RatingCreateInDto;
import edu.bbte.daygig.backend.dto.rating.RatingEligibilityOutDto;
import edu.bbte.daygig.backend.dto.rating.RatingOutDto;
import edu.bbte.daygig.backend.model.enums.RatingContext;
import edu.bbte.daygig.backend.security.JwtPrincipal;
import edu.bbte.daygig.backend.service.rating.RatingService;

@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/jobs/{jobId}/ratings")
    @ResponseStatus(HttpStatus.CREATED)
    public RatingOutDto create(@PathVariable UUID jobId,
                               @Valid @RequestBody RatingCreateInDto ratingCreateInDto,
                               @AuthenticationPrincipal JwtPrincipal principal) {
        return ratingService.create(jobId, ratingCreateInDto, currentUserId(principal));
    }

    @GetMapping("/jobs/{jobId}/ratings/me")
    public RatingEligibilityOutDto getEligibility(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        return ratingService.getEligibility(jobId, currentUserId(principal));
    }

    @GetMapping("/users/{userId}/ratings")
    public List<RatingOutDto> getReceivedRatings(@PathVariable UUID userId,
                                                 @RequestParam RatingContext context) {
        return ratingService.getReceivedRatings(userId, context);
    }

    private UUID currentUserId(JwtPrincipal principal) {
        return UUID.fromString(principal.uuid());
    }
}
