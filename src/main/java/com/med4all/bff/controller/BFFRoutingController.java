package com.med4all.bff.controller;



import com.med4all.bff.service.RoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class BFFRoutingController {

    private final RoutingService routingService;

    // Route dispensary requests
    @GetMapping("/dispensary/**")
    public ResponseEntity<?> routeDispensaryGet(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestParam Map<String, String> params) {

        log.info("Routing GET request to dispensary service: {}", request.getRequestURI());
        return routingService.routeToDispensaryService("GET", request, authToken, params, null);
    }

    @PostMapping("/dispensary/**")
    public ResponseEntity<?> routeDispensaryPost(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestBody(required = false) Object body) {

        log.info("Routing POST request to dispensary service: {}", request.getRequestURI());
        return routingService.routeToDispensaryService("POST", request, authToken, null, body);
    }

    @PutMapping("/dispensary/**")
    public ResponseEntity<?> routeDispensaryPut(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestBody(required = false) Object body) {

        log.info("Routing PUT request to dispensary service: {}", request.getRequestURI());
        return routingService.routeToDispensaryService("PUT", request, authToken, null, body);
    }

    @DeleteMapping("/dispensary/**")
    public ResponseEntity<?> routeDispensaryDelete(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken) {

        log.info("Routing DELETE request to dispensary service: {}", request.getRequestURI());
        return routingService.routeToDispensaryService("DELETE", request, authToken, null, null);
    }

    // Route patient requests
    @GetMapping("/patient/**")
    public ResponseEntity<?> routePatientGet(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestParam(required = false) Map<String, String> params) {

        log.info("Routing GET request to patient service: {}", request.getRequestURI());
        return routingService.routeToPatientService("GET", request, authToken, params, null);
    }

    /**
     * JSON POST handler (expects Content-Type: application/json)
     */
    @PostMapping(value = "/patient/**", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> routePatientPostJson(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestParam(required = false) Map<String, String> params,
            @RequestBody(required = false) Map<String, Object> body) {

        log.info("Routing JSON POST request to patient service: {}", request.getRequestURI());
        log.debug("JSON body: {}", body);
        return routingService.routeToPatientService("POST", request, authToken, params, body);
    }

    /**
     * Multipart POST handler (expects Content-Type: multipart/form-data)
     * - file part name: "file" (change if your frontend uses another name)
     * - optional form fields will be available in params
     * - optional metadata part (JSON as string) can be passed in "metadata"
     */
    @PostMapping(value = "/patient/**", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> routePatientPostMultipart(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestParam(required = false) Map<String, String> params,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "metadata", required = false) String metadata) {

        log.info("Routing multipart POST request to patient service: {}", request.getRequestURI());

        // If frontend sent metadata as a part, you can forward it as a query/form param
        if (metadata != null) {
            params = (params == null) ? new java.util.HashMap<>() : params;
            params.put("metadata", metadata);
        }

        // forward MultipartFile directly — forwardToService will detect MultipartFile and build multipart payload
        return routingService.routeToPatientService("POST", request, authToken, params, file);
    }

    @PutMapping(value = "/patient/**", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> routePatientPut(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestBody Map<String, Object> body) {

        log.info("Routing PUT request to patient service: {}", request.getRequestURI());
        return routingService.routeToPatientService("PUT", request, authToken, null, body);
    }

    @DeleteMapping("/patient/**")
    public ResponseEntity<?> routePatientDelete(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken) {

        log.info("Routing DELETE request to patient service: {}", request.getRequestURI());
        return routingService.routeToPatientService("DELETE", request, authToken, null, null);
    }

    // Route doctor requests
    @GetMapping("/doctor/**")
    public ResponseEntity<?> routeDoctorGet(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestParam Map<String, String> params) {

        log.info("Routing GET request to doctor service: {}", request.getRequestURI());
        return routingService.routeToDoctorService("GET", request, authToken, params, null);
    }

    @PostMapping("/doctor/**")
    public ResponseEntity<?> routeDoctorPost(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestBody(required = false) Object body) {

        log.info("Routing POST request to doctor service: {}", request.getRequestURI());
        return routingService.routeToDoctorService("POST", request, authToken, null, body);
    }

    @PutMapping("/doctor/**")
    public ResponseEntity<?> routeDoctorPut(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestBody(required = false) Object body) {

        log.info("Routing PUT request to doctor service: {}", request.getRequestURI());
        return routingService.routeToDoctorService("PUT", request, authToken, null, body);
    }

    @DeleteMapping("/doctor/**")
    public ResponseEntity<?> routeDoctorDelete(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken) {

        log.info("Routing DELETE request to doctor service: {}", request.getRequestURI());
        return routingService.routeToDoctorService("DELETE", request, authToken, null, null);
    }

    @GetMapping("/payments/**")
    public ResponseEntity<?> routePaymentsGet(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestParam Map<String, String> params) {

        log.info("Routing GET request to payments (dispensary) service: {}", request.getRequestURI());
        return routingService.routeToPaymentsService("GET", request, authToken, params, null);
    }

    @PostMapping("/payments/**")
    public ResponseEntity<?> routePaymentsPost(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestBody(required = false) Object body) {

        log.info("Routing POST request to payments (dispensary) service: {}", request.getRequestURI());
        return routingService.routeToPaymentsService("POST", request, authToken, null, body);
    }

    @PutMapping("/payments/**")
    public ResponseEntity<?> routePaymentsPut(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestBody(required = false) Object body) {

        log.info("Routing PUT request to payments (dispensary) service: {}", request.getRequestURI());
        return routingService.routeToPaymentsService("PUT", request, authToken, null, body);
    }

    @DeleteMapping("/payments/**")
    public ResponseEntity<?> routePaymentsDelete(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken) {

        log.info("Routing DELETE request to payments (dispensary) service: {}", request.getRequestURI());
        return routingService.routeToPaymentsService("DELETE", request, authToken, null, null);
    }
}
