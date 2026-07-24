package edu.bbte.daygig.backend.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.bbte.daygig.backend.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    User findByEmail(String email);

}
