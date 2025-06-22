package com.med4all.bff.controller;



import com.med4all.bff.service.RoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
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
            @RequestParam Map<String, String> params) {

        log.info("Routing GET request to patient service: {}", request.getRequestURI());
        return routingService.routeToPatientService("GET", request, authToken, params, null);
    }

    @PostMapping("/patient/**")
    public ResponseEntity<?> routePatientPost(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestBody(required = false) Object body) {

        log.info("Routing POST request to patient service: {}", request.getRequestURI());
        return routingService.routeToPatientService("POST", request, authToken, null, body);
    }

    @PutMapping("/patient/**")
    public ResponseEntity<?> routePatientPut(
            HttpServletRequest request,
            @RequestHeader("Authorization") String authToken,
            @RequestBody(required = false) Object body) {

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
}
