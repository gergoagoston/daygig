package edu.bbte.daygig.backend.service.avatar;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import edu.bbte.daygig.backend.model.UserAvatar;

public interface UserAvatarService {

    String getAvatarUrl(UUID userId);

    UserAvatar getAvatar(UUID userId);

    void updateAvatar(UUID userId,
                      MultipartFile file);

    void deleteAvatar(UUID userId);
}
