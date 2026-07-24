package edu.bbte.daygig.backend.mappers;

import org.springframework.stereotype.Component;
import edu.bbte.daygig.backend.dto.user.UserOutDto;
import edu.bbte.daygig.backend.dto.user.UserRegisterInDto;
import edu.bbte.daygig.backend.dto.user.UserSummaryOutDto;
import edu.bbte.daygig.backend.dto.user.UserUpdateInDto;
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

    public User fromUpdateDto(UserUpdateInDto updateInDto) {
        User user = new User();
        user.setEmail(updateInDto.getEmail());
        user.setPassword(updateInDto.getPassword());
        user.setFirstName(updateInDto.getFirstName());
        user.setLastName(updateInDto.getLastName());
        user.setPhoneNumber(updateInDto.getPhoneNumber());
        user.setBio(updateInDto.getBio());
        return user;
    }

    public UserOutDto toDto(User user) {
        return new UserOutDto(user.getId(), user.getEmail(), user.getRole(), user.getFirstName(), user.getLastName(),
            user.getPhoneNumber(), user.getBio(), user.getCreatedAt());
    }

    public UserSummaryOutDto toSummaryDto(User user) {
        return new UserSummaryOutDto(user.getId(), user.getFirstName(), user.getLastName());
    }
}
