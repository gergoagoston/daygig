package edu.bbte.daygig.backend.mappers;

import edu.bbte.daygig.backend.dto.user.UserLoginInDto;
import edu.bbte.daygig.backend.model.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public User fromLoginDto(UserLoginInDto loginInDto) {
        User user = new User();
        user.setEmail(loginInDto.getEmail());
        user.setPassword(loginInDto.getPassword());
        return user;
    }
}
