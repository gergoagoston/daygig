package edu.bbte.daygig.backend.service.user;

import java.util.UUID;
import edu.bbte.daygig.backend.dto.user.UserProfileOutDto;

public interface UserProfileService {

    UserProfileOutDto getProfile(UUID userId);
}
