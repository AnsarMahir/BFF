package com.med4all.bff.service;

import com.med4all.bff.dto.LoginRequest;
import com.med4all.bff.dto.LoginResponse;
import com.med4all.bff.dto.RegistrationRequest;
import com.med4all.bff.entity.Role;
import com.med4all.bff.entity.User;
import com.med4all.bff.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

// service/AuthService.java
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.upload.dir}")
    private String uploadDir; // Injects the upload directory path


    public LoginResponse register(RegistrationRequest request) {
        validateRegistration(request);
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        // Handle Doctor/Dispensary certificate upload
        if (request.getRole() == Role.DOCTOR || request.getRole() == Role.DISPENSARY) {
            validateCertificate(request.getCertificateFile());
            String savedFilePath = saveCertificate(request.getCertificateFile());
            user.setCertificatePath(savedFilePath);
            user.setLicenseNumber(request.getLicenseNumber());
        }

        userRepository.save(user);
        return generateToken(user);
    }

    private String saveCertificate(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);

            // Create directory if it doesn't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save the file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString(); // Return full path
        } catch (IOException e) {
            throw new RuntimeException("Failed to save certificate file: " + e.getMessage());
        }
    }

    private void validateCertificate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Certificate file is required for this role");
        }
        // Optional: Validate file type/size
        String contentType = file.getContentType();
        if (!"application/pdf".equals(contentType)) { // Example: Allow PDF only
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return generateToken(user);
    }

    private LoginResponse generateToken(User user) {
        String jwt = jwtService.generateToken(user);
        return new LoginResponse(jwt, user.getId(), user.getEmail(), user.getRole());
    }

    private void validateRegistration(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        final Role role = request.getRole();

        // Validate fields based on role
        if (role == Role.ADMIN || role == Role.PATIENT) {
            if (request.getLicenseNumber() != null || request.getCertificateFile() != null) {
                throw new IllegalArgumentException(
                        "License number or certificate file not allowed for role: " + role
                );
            }
        } else if (role == Role.DOCTOR || role == Role.DISPENSARY) {
            if (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank()) {
                throw new IllegalArgumentException("License number is required for " + role);
            }
            if (request.getCertificateFile() == null || request.getCertificateFile().isEmpty()) {
                throw new IllegalArgumentException("Certificate file is required for " + role);
            }
        } else {
            throw new IllegalArgumentException("Invalid user role: " + role);
        }
    }
}