package edu.bbte.daygig.backend.controller;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.application.ApplicationOutDto;
import edu.bbte.daygig.backend.dto.application.ApplicationStatusUpdateInDto;
import edu.bbte.daygig.backend.dto.application.OwnerApplicationOutDto;
import edu.bbte.daygig.backend.mappers.ApplicationMapper;
import edu.bbte.daygig.backend.security.JwtPrincipal;
import edu.bbte.daygig.backend.service.application.ApplicationService;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;

    @PostMapping("/jobs/{jobId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationOutDto apply(@PathVariable UUID jobId,
                                   @AuthenticationPrincipal JwtPrincipal principal) {
        return applicationMapper.toDto(applicationService.apply(jobId, currentUserId(principal)));
    }

    @GetMapping("/applications/mine")
    public List<ApplicationOutDto> getMine(@AuthenticationPrincipal JwtPrincipal principal) {
        return applicationService
                .getMine(currentUserId(principal))
                .stream()
                .map(applicationMapper::toDto)
                .toList();
    }

    @GetMapping("/jobs/{jobId}/applications")
    public List<OwnerApplicationOutDto> getByJob(@PathVariable UUID jobId,
                                                 @AuthenticationPrincipal JwtPrincipal principal) {
        return applicationService
                .getByJobForOwner(jobId, currentUserId(principal))
                .stream()
                .map(applicationMapper::toOwnerDto)
                .toList();
    }

    @PatchMapping("/applications/{applicationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStatus(@PathVariable UUID applicationId,
                             @Valid @RequestBody ApplicationStatusUpdateInDto statusUpdateInDto,
                             @AuthenticationPrincipal JwtPrincipal principal) {
        applicationService.updateStatus(applicationId, statusUpdateInDto.getStatus(), currentUserId(principal));
    }

    private UUID currentUserId(JwtPrincipal principal) {
        return UUID.fromString(principal.uuid());
    }
}
