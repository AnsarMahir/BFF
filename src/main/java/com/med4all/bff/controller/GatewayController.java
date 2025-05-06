package com.med4all.bff.controller;

import com.med4all.bff.service.ProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class GatewayController {

    @Autowired
    private ProxyService proxyService;

    // Register Endpoint Proxy
    @PostMapping("/register/{userType}")
    public ResponseEntity<?> registerProxy(@PathVariable String userType, HttpServletRequest request) {
        return proxyService.forwardRequest(userType, request, "register");
    }

    // Login Endpoint Proxy
    @PostMapping("/login/{userType}")
    public ResponseEntity<?> loginProxy(@PathVariable String userType, HttpServletRequest request) {
        return proxyService.forwardRequest(userType, request, "login");
    }
}