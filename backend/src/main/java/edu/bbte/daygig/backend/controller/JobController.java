package edu.bbte.daygig.backend.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.dto.common.PageOutDto;
import edu.bbte.daygig.backend.dto.job.JobDetailedOutDto;
import edu.bbte.daygig.backend.dto.job.JobOutDto;
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
    public PageOutDto<JobOutDto> getOpenJobs(@PageableDefault(size = DEFAULT_PAGE_SIZE,
                                                              sort = "createdAt",
                                                              direction = Sort.Direction.DESC) Pageable pageable) {
        return PageOutDto.from(jobService
                .getOpenJobs(pageable)
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

    @GetMapping("/{id}")
    public JobDetailedOutDto getById(@PathVariable UUID id) {
        return jobMapper.toDetailsDto(jobService.getById(id));
    }

    private UUID currentUserId(JwtPrincipal principal) {
        return UUID.fromString(principal.uuid());
    }
}
