package edu.bbte.daygig.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateInDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;
}
