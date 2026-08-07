package edu.bbte.daygig.backend.repository;

import java.util.UUID;

public interface JobApplicationCountProjection {

    UUID getJobId();

    long getApplicationCount();
}
