package com.med4all.bff.controller;

import com.med4all.bff.dto.LoginRequest;
import com.med4all.bff.dto.LoginResponse;
import com.med4all.bff.dto.RegistrationRequest;
import com.med4all.bff.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, name = "registerEndpoint")
    public ResponseEntity<LoginResponse> register(
            @ModelAttribute RegistrationRequest request // Use @ModelAttribute for form-data
    ) {

        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping(path="/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, name = "loginEndpoint")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}