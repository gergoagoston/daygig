package edu.bbte.daygig.backend.service.avatar;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.exception.ResourceNotFoundException;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.model.UserAvatar;
import edu.bbte.daygig.backend.repository.UserAvatarRepository;
import edu.bbte.daygig.backend.service.user.UserService;

@Service
@RequiredArgsConstructor
public class UserAvatarServiceImpl implements UserAvatarService {

    private static final String AVATAR_URL_TEMPLATE = "/api/users/%s/avatar";

    private final UserAvatarRepository userAvatarRepository;
    private final UserService userService;
    private final AvatarImageProcessor imageProcessor;

    @Override
    @Transactional(readOnly = true)
    public String getAvatarUrl(UUID userId) {
        if (!userAvatarRepository.existsById(userId)) {
            return null;
        }

        return AVATAR_URL_TEMPLATE.formatted(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAvatar getAvatar(UUID userId) {
        userService.getById(userId);
        return userAvatarRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Avatar not found for user with id: " + userId));
    }

    @Override
    @Transactional
    public void updateAvatar(UUID userId,
                             MultipartFile file) {
        User user = userService.getById(userId);
        ProcessedAvatar processedAvatar = imageProcessor.process(file);
        UserAvatar avatar = userAvatarRepository
                .findById(userId)
                .orElseGet(UserAvatar::new);

        avatar.setUser(user);
        avatar.setImageData(processedAvatar.imageData());
        avatar.setContentType(processedAvatar.contentType());
        userAvatarRepository.save(avatar);
    }

    @Override
    @Transactional
    public void deleteAvatar(UUID userId) {
        userService.getById(userId);
        userAvatarRepository.findById(userId).ifPresent(userAvatarRepository::delete);
    }
}
