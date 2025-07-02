package com.med4all.bff.service;

import com.med4all.bff.client.DispensaryServiceClient;
import com.med4all.bff.dto.*;
import com.med4all.bff.entity.Role;
import com.med4all.bff.entity.User;
import com.med4all.bff.entity.UserStatus;
import com.med4all.bff.exception.AccountNotApprovedException;
import com.med4all.bff.exception.EmailAlreadyExistsException;
import com.med4all.bff.exception.InvalidCredentialsException;
import com.med4all.bff.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final DispensaryServiceClient dispensaryServiceClient;
    private static final Logger log = Logger.getLogger(AuthService.class.getName());


    @Value("${app.upload.dir}")
    private String uploadDir;
    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        validateRegistration(request);

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        // Set status based on role
        if (request.getRole() == Role.PATIENT) {
            user.setStatus(UserStatus.ACTIVE); // Auto-approve patients
        } else {
            user.setStatus(UserStatus.PENDING); // Require approval for DOCTOR/DISPENSARY
        }

        // Handle Doctor/Dispensary certificate upload
        if (request.getRole() == Role.DOCTOR || request.getRole() == Role.DISPENSARY) {
            validateCertificate(request.getCertificateFile());
            String savedFilePath = saveCertificate(request.getCertificateFile());
            user.setCertificatePath(savedFilePath);
            user.setLicenseNumber(request.getLicenseNumber());
        }

        User savedUser = userRepository.save(user);

        // If dispensary, create dispensary profile in dispensary service
        if (request.getRole() == Role.DISPENSARY) {
            createDispensaryProfile(savedUser, request);
        }

        // Return appropriate response based on status
        String message = savedUser.getStatus() == UserStatus.ACTIVE
                ? "Registration successful. You can now login."
                : "Registration successful. Your account is pending admin approval.";

        return new RegistrationResponse(message, savedUser.getStatus().name());
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        // Check approval status
        validateUserStatus(user);

        return generateToken(user);
    }

    private void validateUserStatus(User user) {
        // Patients and Admins can always login if credentials are correct
        if (user.getRole() == Role.PATIENT || user.getRole() == Role.ADMIN) {
            return;
        }

        // For DOCTOR and DISPENSARY, check approval status
        switch (user.getStatus()) {
            case PENDING:
                throw new AccountNotApprovedException(
                        "Your account is pending admin approval. Please contact the administrator."
                );
            case REJECTED:
                throw new AccountNotApprovedException(
                        "Your account has been rejected. Please contact the administrator for more information."
                );
            case APPROVED:
                // Allow login
                break;
            default:
                throw new AccountNotApprovedException("Account status is invalid.");
        }
    }

    private LoginResponse generateToken(User user) {
        String jwt = jwtService.generateToken(user);
        return new LoginResponse(jwt, user.getId(), user.getEmail(), user.getRole().name());
    }

    private String saveCertificate(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save certificate file: " + e.getMessage());
        }
    }

    private void validateCertificate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Certificate file is required for this role");
        }

        String contentType = file.getContentType();
        if (!"application/pdf".equals(contentType)) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
    }

    private void validateRegistration(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        final Role role = request.getRole();

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
            throw new IllegalArgumentException("Invalid role: " + role);
        }  // Remove the nested method from here
    }
    private void createDispensaryProfile(User user, RegistrationRequest request) {
        try {
            DispensaryCreateRequest dispensaryRequest = DispensaryCreateRequest.builder()
                    .email(user.getEmail())
                    .name(request.getDispensaryName())
                    .address(request.getAddress())
                    .longitude(request.getLongitude())
                    .latitude(request.getLatitude())
                    .licenseNumber(user.getLicenseNumber())
                    .ownerName(user.getFullName())
                    .isValid(false)
                    .build();

            dispensaryServiceClient.createDispensary(dispensaryRequest);
        } catch (Exception e) {
            // Improved error handling
            log.log(Level.INFO, "Failed to create dispensary profile for user: " + user.getEmail(), e);
            // Consider adding retry mechanism or dead-letter queue here
        }
    }
}
