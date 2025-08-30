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

import java.time.LocalDateTime;
import java.util.Random;
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
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final DispensaryServiceClient dispensaryServiceClient;
    private final UtilService utilService;
    private static final Logger log = Logger.getLogger(AuthService.class.getName());


    @Value("${app.upload.dir}")
    private String uploadDir;
    @Transactional
    public RegistrationResponse register(RegistrationRequest request) {
        validateRegistration(request);

        User user = new User();
        user.setFullName(request.getName());
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
        if (request.getRole() == Role.DISPENSARY) {
            validateCertificate(request.getCertificateFile());
            String savedFilePath = saveCertificate(request.getCertificateFile());
            user.setCertificatePath(savedFilePath);
            user.setLicenseNumber(request.getLicenseNumber());
        }


        if (request.getRole() == Role.DOCTOR) {
            validateCertificate(request.getCertificateFile());
            String savedFilePath = saveCertificate(request.getCertificateFile());
            user.setCertificatePath(savedFilePath);
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        emailService.sendOtpEmail(savedUser.getEmail(), otp);

        String message = savedUser.getStatus() == UserStatus.ACTIVE
                ? "Registration successful. Please verify your email."
                : "Registration successful. Please verify your email and wait for admin approval.";

        return new RegistrationResponse(message, savedUser.getStatus().name());
    }

    public String verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return "Email already verified.";
        }

        if (!otp.equals(user.getOtp()) || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired OTP.");
        }

        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        //  If dispensary, create dispensary profile in dispensary service
        if (user.getRole() == Role.DISPENSARY && user.getStatus() == UserStatus.APPROVED) {
           utilService.createDispensaryProfile(user);
        }
        if (user.getRole() == Role.PATIENT && user.getStatus() == UserStatus.ACTIVE) {
            utilService.createPatientProfile(user);
        }

        if (user.getRole() == Role.DOCTOR && user.getStatus() == UserStatus.APPROVED) {
            utilService.createDispensaryProfile(user);
        }

        userRepository.save(user);

        return "Email verified successfully.";
    }

    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return "Email already verified.";
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);

        return "New OTP sent to your email.";
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.getEmailVerified()){
            throw new AccountNotApprovedException("Please verify the email");
        }

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
        log.info("Generated JWT: " + jwt);
        log.info("User ID: {}, Email: {}, Role: {}" + user.getId() + user.getEmail() + user.getRole());
        return new LoginResponse(jwt, user.getId(), user.getEmail(), user.getRole());
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

    public CertificateFileResponse getCertificateByEmail(String email) throws RuntimeException {
        // Validate email
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        // Find user by email and get certificate path
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with email: " + email));

        String certificatePath = user.getCertificatePath();
        if (certificatePath == null || certificatePath.trim().isEmpty()) {
            throw new RuntimeException("No certificate found for email: " + email);
        }

        // Verify file exists
        Path filePath = Paths.get(certificatePath);
        if (!Files.exists(filePath)) {
            log.info("Certificate file not found at path: {}");
            throw new RuntimeException("Certificate file not found on server");
        }

        // Extract original filename from the stored path
        String fileName = filePath.getFileName().toString();
        String originalFileName = extractOriginalFileName(fileName);

        return CertificateFileResponse.builder()
                .filePath(certificatePath)
                .originalFileName(originalFileName)
                .build();
    }

    private String extractOriginalFileName(String storedFileName) {
        // Assuming the stored filename format is: UUID_originalFileName
        int underscoreIndex = storedFileName.indexOf('_');
        if (underscoreIndex != -1 && underscoreIndex < storedFileName.length() - 1) {
            return storedFileName.substring(underscoreIndex + 1);
        }
        return storedFileName;
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
        } else if (role == Role.DISPENSARY) {
            if (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank()) {
                throw new IllegalArgumentException("License number is required for " + role);
            }
            if (request.getCertificateFile() == null || request.getCertificateFile().isEmpty()) {
                throw new IllegalArgumentException("Certificate file is required for " + role);
            }
        } else if (role == Role.DOCTOR ) {
            if (request.getCertificateFile() == null || request.getCertificateFile().isEmpty()) {
                throw new IllegalArgumentException("Certificate file is required for " + role);
            }
        } else {
            throw new IllegalArgumentException("Invalid role: " + role);
        }  // Remove the nested method from here
    }

}
