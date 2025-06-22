package com.med4all.bff.dto;

import com.med4all.bff.entity.Role;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class RegistrationRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    // For DOCTOR and DISPENSARY roles
    private String licenseNumber;
    private MultipartFile certificateFile;

    // For DISPENSARY role only
    private String dispensaryName;
    private String address;
    private Double longitude;
    private Double latitude;
}