package edu.bbte.daygig.backend.service.user;

import edu.bbte.daygig.backend.model.User;

import java.util.List;

public interface UserService {

    User login(User user);

    User register(User user);

    User getByUserName(String userName);

    List<User> getAll();

    User getById(Long id);

    User getByEmail(String email);

}
