package edu.bbte.daygig.backend.service.auth;

import edu.bbte.daygig.backend.model.User;

public interface AuthService {

    User login(String email,
               String password);

    User register(User user);
}
