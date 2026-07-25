package edu.bbte.daygig.backend.controller;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.common.PageOutDto;
import edu.bbte.daygig.backend.dto.job.JobCompletionInDto;
import edu.bbte.daygig.backend.dto.job.JobCreateInDto;
import edu.bbte.daygig.backend.dto.job.JobDetailedOutDto;
import edu.bbte.daygig.backend.dto.job.JobOutDto;
import edu.bbte.daygig.backend.dto.job.JobSearchInDto;
import edu.bbte.daygig.backend.mappers.JobMapper;
import edu.bbte.daygig.backend.security.JwtPrincipal;
import edu.bbte.daygig.backend.service.job.JobService;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final JobService jobService;
    private final JobMapper jobMapper;

    @GetMapping
    public PageOutDto<JobOutDto> getOpenJobs(
            @Valid @ModelAttribute JobSearchInDto search,
            @PageableDefault(size = DEFAULT_PAGE_SIZE,
                             sort = "createdAt",
                             direction = Sort.Direction.DESC) Pageable pageable) {
        return PageOutDto.from(jobService
                .getOpenJobs(search, pageable)
                .map(jobMapper::toListDto));
    }

    @GetMapping("/mine")
    public List<JobOutDto> getMine(@AuthenticationPrincipal JwtPrincipal principal) {
        return jobService
                .getMine(currentUserId(principal))
                .stream()
                .map(jobMapper::toListDto)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobOutDto create(@Valid @RequestBody JobCreateInDto jobCreateInDto,
                            @AuthenticationPrincipal JwtPrincipal principal) {
        return jobMapper.toListDto(jobService.create(jobCreateInDto, currentUserId(principal)));
    }

    @GetMapping("/{id}")
    public JobDetailedOutDto getById(@PathVariable UUID id) {
        return jobMapper.toDetailsDto(jobService.getById(id));
    }

    @PutMapping("/{id}/completion")
    public JobDetailedOutDto complete(@PathVariable UUID id,
                                      @Valid @RequestBody JobCompletionInDto completionInDto,
                                      @AuthenticationPrincipal JwtPrincipal principal) {
        return jobMapper.toDetailsDto(jobService.complete(id, completionInDto.applicationId(),
                currentUserId(principal)));
    }

    private UUID currentUserId(JwtPrincipal principal) {
        return UUID.fromString(principal.uuid());
    }
}
