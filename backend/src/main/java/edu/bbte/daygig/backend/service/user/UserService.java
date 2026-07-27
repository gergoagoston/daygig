package edu.bbte.daygig.backend.service.user;

import java.util.UUID;
import edu.bbte.daygig.backend.model.User;

public interface UserService {

    User getById(UUID id);

    User updateBio(UUID id,
                   String bio);
}
