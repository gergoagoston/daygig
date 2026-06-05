package edu.bbte.daygig.backend.repository;

import edu.bbte.daygig.backend.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;


@Repository
@Profile("jpa")
public interface UserRepository extends JpaRepository<User, Long> {

}
