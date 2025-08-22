// controller/AuthController.java
package com.med4all.bff.controller;

import com.med4all.bff.dto.*;
import com.med4all.bff.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.logging.Logger;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger log = Logger.getLogger(AuthController.class.getName());
    private final AuthService authService;

    @PostMapping(value = "/register",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,name = "registerEndpoint")
    public ResponseEntity<RegistrationResponse> register(@Valid @ModelAttribute RegistrationRequest request) {
        log.info("Received registration request for email: " + request.getName());
        RegistrationResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE,name = "loginEndpoint")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request.getEmail(), request.getOtp()));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody ResendOtpRequest request) {
        return ResponseEntity.ok(authService.resendOtp(request.getEmail()));
    }
}