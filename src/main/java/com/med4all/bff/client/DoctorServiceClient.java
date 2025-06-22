package com.med4all.bff.client;

import com.med4all.bff.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "doctor-service",
        url = "${microservices.doctor.url}",
        configuration = FeignConfiguration.class
)
public interface DoctorServiceClient {

    @GetMapping("/api/doctor/**")
    ResponseEntity<?> forwardDoctorRequest(
            @RequestHeader("Authorization") String token,
            @RequestParam MultiValueMap<String, String> params
    );

    @PostMapping("/api/doctor/**")
    ResponseEntity<?> forwardDoctorPostRequest(
            @RequestHeader("Authorization") String token,
            @RequestBody Object body
    );

    @PutMapping("/api/doctor/**")
    ResponseEntity<?> forwardDoctorPutRequest(
            @RequestHeader("Authorization") String token,
            @RequestBody Object body
    );

    @DeleteMapping("/api/doctor/**")
    ResponseEntity<?> forwardDoctorDeleteRequest(
            @RequestHeader("Authorization") String token
    );
}
