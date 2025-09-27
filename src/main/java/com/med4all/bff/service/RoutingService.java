package com.med4all.bff.service;

import com.med4all.bff.client.DispensaryServiceClient;
import com.med4all.bff.client.DoctorServiceClient;
import com.med4all.bff.client.PatientServiceClient;
import com.med4all.bff.dto.CurrentUserDetails;
import com.med4all.bff.entity.Role;
import com.med4all.bff.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
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
        } catch (IllegalArgumentException e) {
            // e.g. unsupported HTTP method
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error routing to patient service", e);
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
        }  catch (HttpStatusCodeException ex) {
        // Forward the actual status and body from downstream
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ex.getResponseBodyAsString());
    }
    }

    private ResponseEntity<?> forwardToService(
            String serviceName,
            String method,
            HttpServletRequest request,
            String authToken,
            Map<String, String> params,
            Object body) {

        try {
            // Build target URL
            String baseUrl = getServiceBaseUrl(serviceName);
            String path = extractPathAfterService(request.getRequestURI(), serviceName);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + path);
            if (params != null && !params.isEmpty()) {
                params.forEach(uriBuilder::queryParam);
            }
            URI targetUri = uriBuilder.build().toUri();

            // Build headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);

            // Forward user email header if present
            String userEmail = extractEmailFromToken(authToken);
            if (userEmail != null) {
                headers.set("X-User-Email", userEmail);
                log.debug("Forwarding request with X-User-Email: {}", userEmail);
            }

            HttpEntity<?> entity;

            // Multipart handling (MVC-safe)
            if (body instanceof MultipartFile file) {
                MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
                multipartBody.add("file", new MultipartInputStreamFileResource(
                        file.getInputStream(), file.getOriginalFilename()));

                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                entity = new HttpEntity<>(multipartBody, headers);

            } else {
                headers.setContentType(MediaType.APPLICATION_JSON);
                entity = new HttpEntity<>(body, headers);
            }

            // --- Special-case GET to allow binary responses (PDF/image/etc) ---
            if ("GET".equalsIgnoreCase(method)) {
                ResponseEntity<byte[]> downstreamResp = restTemplate.exchange(
                        targetUri, HttpMethod.GET, entity, byte[].class);

                HttpHeaders respHeaders = new HttpHeaders();
                respHeaders.putAll(downstreamResp.getHeaders()); // preserve downstream headers

                return ResponseEntity.status(downstreamResp.getStatusCode())
                        .headers(respHeaders)
                        .body(downstreamResp.getBody());
            }

            // For other methods use generic Object response
            ResponseEntity<Object> downstreamResp = restTemplate.exchange(
                    targetUri, HttpMethod.valueOf(method.toUpperCase()), entity, Object.class);

            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.putAll(downstreamResp.getHeaders());

            return ResponseEntity.status(downstreamResp.getStatusCode())
                    .headers(respHeaders)
                    .body(downstreamResp.getBody());

        } catch (HttpStatusCodeException ex) {
            // Forward downstream error status, headers and body.
            HttpHeaders errorHeaders = new HttpHeaders();
            if (ex.getResponseHeaders() != null) {
                errorHeaders.putAll(ex.getResponseHeaders());
            }

            byte[] errorBody = ex.getResponseBodyAsByteArray();
            // If there's a binary body, return it as byte[], else try to return string
            if (errorBody != null && errorBody.length > 0) {
                return ResponseEntity.status(ex.getStatusCode())
                        .headers(errorHeaders)
                        .body(errorBody);
            } else {
                return ResponseEntity.status(ex.getStatusCode())
                        .headers(errorHeaders)
                        .body(ex.getResponseBodyAsString());
            }
        } catch (Exception e) {
            log.error("Error forwarding request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Service temporarily unavailable"));
        }
    }



    /**
     * Extract email from JWT token's 'sub' claim
     */
    private String extractEmailFromToken(String authToken) {
        try {
            String token = authToken.replace("Bearer ", "");
            // Since your JWT has email in 'sub' claim, use extractUsername which likely extracts 'sub'
            return jwtService.extractUsername(token);
        } catch (Exception e) {
            log.warn("Could not extract email from JWT token", e);
            return null;
        }
    }

    public ResponseEntity<?> routeToPaymentsService(
            String method,
            HttpServletRequest request,
            String authToken,
            Map<String, String> params,
            Object body) {

        // Use same access control as dispensary for now
        if (!canAccessDispensaryService(authToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied to payments service"));
        }

        try {
            return forwardToService("payments", method, request, authToken, params, body); // ← Note "payments" here
        } catch (Exception e) {
            log.error("Error routing to payments service", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Service temporarily unavailable"));
        }
    }

    private String getServiceBaseUrl(String serviceName) {
        return switch (serviceName) {
            case "dispensary" -> "http://localhost:8080/api/dispensary";
            case "payments" -> "http://localhost:8080/api/payments";
            case "patient" -> "http://localhost:8080/api/patient";
            case "doctor" -> "http://localhost:8080/api/doctor";
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
                    user.getRole() == Role.DOCTOR ||
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
                    user.getRole() == Role.DISPENSARY ||
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
                    user.getRole() == Role.DISPENSARY || // Dispensary staff may need to access doctor info
                    user.getRole() == Role.DOCTOR ||
                    user.getRole() == Role.PATIENT;
        } catch (Exception e) {
            log.error("Error validating doctor service access", e);
            return false;
        }
    }

    public User getUserFromToken(String authToken) {
        String token = authToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        return userService.findByEmail(email);
    }
}