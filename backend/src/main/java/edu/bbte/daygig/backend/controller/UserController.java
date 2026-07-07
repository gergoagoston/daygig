package edu.bbte.daygig.backend.controller;

import edu.bbte.daygig.backend.dto.user.UserOutDto;
import edu.bbte.daygig.backend.dto.user.UserUpdateInDto;
import edu.bbte.daygig.backend.mappers.UserMapper;
import edu.bbte.daygig.backend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserOutDto> getAll() {
        return userService.getAll()
                          .stream()
                          .map(userMapper::toDto)
                          .toList();
    }

    @GetMapping("/{id}")
    public UserOutDto getById(@PathVariable UUID id) {
        return userMapper.toDto(userService.getById(id));
    }

    @PutMapping("/{id}")
    public UserOutDto update(@PathVariable UUID id, @Valid @RequestBody UserUpdateInDto updateInDto) {
        return userMapper.toDto(userService.update(id, userMapper.fromUpdateDto(updateInDto)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}
