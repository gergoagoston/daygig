package edu.bbte.daygig.backend.mappers;

import org.springframework.stereotype.Component;
import edu.bbte.daygig.backend.dto.user.UserOutDto;
import edu.bbte.daygig.backend.dto.user.UserProfileOutDto;
import edu.bbte.daygig.backend.dto.user.UserRegisterInDto;
import edu.bbte.daygig.backend.dto.user.UserSummaryOutDto;
import edu.bbte.daygig.backend.model.User;

@Component
public class UserMapper {

    public User fromRegisterDto(UserRegisterInDto registerInDto) {
        User user = new User();
        user.setEmail(registerInDto.getEmail());
        user.setPassword(registerInDto.getPassword());
        user.setFirstName(registerInDto.getFirstName());
        user.setLastName(registerInDto.getLastName());
        user.setPhoneNumber(registerInDto.getPhoneNumber());
        return user;
    }

    public UserOutDto toDto(User user,
                            String avatarUrl) {
        return new UserOutDto(user.getId(), user.getEmail(), user.getRole(), user.getFirstName(), user.getLastName(),
            user.getPhoneNumber(), user.getBio(), avatarUrl, user.getCreatedAt());
    }

    public UserProfileOutDto toProfileDto(User user,
                                          String avatarUrl) {
        return new UserProfileOutDto(user.getId(), user.getFirstName(), user.getLastName(), user.getBio(),
                avatarUrl, user.getCreatedAt());
    }

    public UserSummaryOutDto toSummaryDto(User user) {
        return new UserSummaryOutDto(user.getId(), user.getFirstName(), user.getLastName());
    }
}
