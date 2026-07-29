package edu.bbte.daygig.backend.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.bbte.daygig.backend.model.UserAvatar;

@Repository
public interface UserAvatarRepository extends JpaRepository<UserAvatar, UUID> {
}
