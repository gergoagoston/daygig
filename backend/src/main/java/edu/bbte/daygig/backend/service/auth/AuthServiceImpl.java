package edu.bbte.daygig.backend.service.auth;

import edu.bbte.daygig.backend.exception.AuthException;
import edu.bbte.daygig.backend.exception.ServiceException;
import edu.bbte.daygig.backend.model.Role;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public User login(User user) {
        User dbUser = userRepository.findByEmail(user.getEmail());

        if (dbUser == null || !passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new AuthException("Invalid email or password");
        }

        return dbUser;
    }

    @Override
    @Transactional
    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new ServiceException("Email is already taken");
        }

        user.setRole(Role.ROLE_USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
}
