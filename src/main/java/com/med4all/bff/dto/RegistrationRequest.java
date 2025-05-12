package com.med4all.bff.dto;

import com.med4all.bff.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Data
public class RegistrationRequest {
    @NotBlank private String fullName;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Size(min=8)
    private String password;

    private Role role;
    private String licenseNumber; // optional
    private MultipartFile certificateFile ;// optional
    // Getters and Setters
}
