package com.med4all.bff.service;


import com.med4all.bff.client.DispensaryServiceClient;
import com.med4all.bff.client.DoctorServiceClient;
import com.med4all.bff.client.PatientServiceClient;
import com.med4all.bff.entity.Role;
import com.med4all.bff.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingService {

    private final DispensaryServiceClient dispensaryServiceClient;
    private final PatientServiceClient patientServiceClient;
    private final DoctorServiceClient doctorServiceClient;
    private final JwtService jwtService;
    private final UserService userService;
    private final RestTemplate restTemplate;

    public ResponseEntity<?> routeToDispensaryService(
            String method,
            HttpServletRequest request,
            String authToken,
            Map<String, String> params,
            Object body) {

        // Validate access
        if (!canAccessDispensaryService(authToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied to dispensary service"));
        }

        try {
            return forwardToService("dispensary", method, request, authToken, params, body);
        } catch (Exception e) {
            log.error("Error routing to dispensary service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Service temporarily unavailable"));
        }
    }

    public ResponseEntity<?> routeToPatientService(
            String method,
            HttpServletRequest request,
            String authToken,
            Map<String, String> params,
            Object body) {

        if (!canAccessPatientService(authToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied to patient service"));
        }

        try {
            return forwardToService("patient", method, request, authToken, params, body);
        } catch (Exception e) {
            log.error("Error routing to patient service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Service temporarily unavailable"));
        }
    }

    public ResponseEntity<?> routeToDoctorService(
            String method,
            HttpServletRequest request,
            String authToken,
            Map<String, String> params,
            Object body) {

        if (!canAccessDoctorService(authToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied to doctor service"));
        }

        try {
            return forwardToService("doctor", method, request, authToken, params, body);
        } catch (Exception e) {
            log.error("Error routing to doctor service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Service temporarily unavailable"));
        }
    }

    private ResponseEntity<?> forwardToService(
            String serviceName,
            String method,
            HttpServletRequest request,
            String authToken,
            Map<String, String> params,
            Object body) {

        // Build target URL
        String baseUrl = getServiceBaseUrl(serviceName);
        String path = extractPathAfterService(request.getRequestURI(), serviceName);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + path);
        if (params != null && !params.isEmpty()) {
            params.forEach(uriBuilder::queryParam);
        }

        URI targetUri = uriBuilder.build().toUri();

        // Forward request using RestTemplate with proper headers
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", authToken);
        headers.set("Content-Type", "application/json");

        org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(body, headers);

        switch (method.toUpperCase()) {
            case "GET":
                return restTemplate.getForEntity(targetUri, Object.class);
            case "POST":
                return restTemplate.postForEntity(targetUri, entity, Object.class);
            case "PUT":
                restTemplate.put(targetUri, entity);
                return ResponseEntity.ok().build();
            case "DELETE":
                restTemplate.delete(targetUri);
                return ResponseEntity.ok().build();
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }

    private String getServiceBaseUrl(String serviceName) {
        return switch (serviceName) {
            case "dispensary" -> "http://localhost:8082/api/dispensary";
            case "patient" -> "http://localhost:8083/api/patient";
            case "doctor" -> "http://localhost:8084/api/doctor";
            default -> throw new IllegalArgumentException("Unknown service: " + serviceName);
        };
    }

    private String extractPathAfterService(String fullPath, String serviceName) {
        String servicePrefix = "/api/" + serviceName;
        if (fullPath.startsWith(servicePrefix)) {
            return fullPath.substring(servicePrefix.length());
        }
        return "";
    }

    private boolean canAccessDispensaryService(String authToken) {
        try {
            User user = getUserFromToken(authToken);
            // ADMIN can access all, DISPENSARY can access their own data, others based on business logic
            return user.getRole() == Role.ADMIN ||
                    user.getRole() == Role.DISPENSARY ||
                    user.getRole() == Role.PATIENT; // Patients can search dispensaries
        } catch (Exception e) {
            log.error("Error validating dispensary service access", e);
            return false;
        }
    }

    private boolean canAccessPatientService(String authToken) {
        try {
            User user = getUserFromToken(authToken);
            // ADMIN and PATIENT can access, DOCTOR can access patient data for appointments
            return user.getRole() == Role.ADMIN ||
                    user.getRole() == Role.PATIENT ||
                    user.getRole() == Role.DOCTOR;
        } catch (Exception e) {
            log.error("Error validating patient service access", e);
            return false;
        }
    }

    private boolean canAccessDoctorService(String authToken) {
        try {
            User user = getUserFromToken(authToken);
            // ADMIN and DOCTOR can access, PATIENT can search doctors
            return user.getRole() == Role.ADMIN ||
                    user.getRole() == Role.DOCTOR ||
                    user.getRole() == Role.PATIENT;
        } catch (Exception e) {
            log.error("Error validating doctor service access", e);
            return false;
        }
    }

    private User getUserFromToken(String authToken) {
        String token = authToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        return userService.findByEmail(email);
    }
}

