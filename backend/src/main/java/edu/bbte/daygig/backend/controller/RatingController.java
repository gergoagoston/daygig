package edu.bbte.daygig.backend.controller;

import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.rating.RatingCreateInDto;
import edu.bbte.daygig.backend.dto.rating.RatingOutDto;
import edu.bbte.daygig.backend.mappers.RatingMapper;
import edu.bbte.daygig.backend.security.JwtPrincipal;
import edu.bbte.daygig.backend.service.rating.RatingService;

@RestController
@RequestMapping("/jobs/{jobId}/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final RatingMapper ratingMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RatingOutDto create(@PathVariable UUID jobId,
                               @Valid @RequestBody RatingCreateInDto ratingCreateInDto,
                               @AuthenticationPrincipal JwtPrincipal principal) {
        return ratingMapper.toDto(ratingService.create(jobId, ratingCreateInDto,
                UUID.fromString(principal.uuid())));
    }
}
