package edu.bbte.daygig.backend.service.job;

import edu.bbte.daygig.backend.model.Job;

public record OwnerJobSummary(Job job, long applicationCount) {
}
