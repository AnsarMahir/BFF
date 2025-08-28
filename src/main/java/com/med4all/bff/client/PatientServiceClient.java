package com.med4all.bff.client;

import com.med4all.bff.config.FeignConfiguration;
import com.med4all.bff.dto.CreateDispensary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(
        name= "patient-service",
        url = "${microservices.patient.url}",
        configuration = FeignConfiguration.class
)
public interface PatientServiceClient {
    @GetMapping("/api/patient/**")
    ResponseEntity<?> forwardPatientRequest(
            @RequestParam Map<String, String> params
    );

    @PostMapping("/api/patient/**")
    ResponseEntity<?> forwardPatientPostRequest(
            @RequestBody Object body
    );

    @PostMapping("/api/patient")
    ResponseEntity<?> createPatient(
            @RequestBody CreateDispensary body
    );
    @PutMapping("/api/patient/**")
    ResponseEntity<?> forwardPatientPutRequest(
            @RequestHeader("Authorization") String token,
            @RequestBody Object body
    );

    @DeleteMapping("/api/patient/**")
    ResponseEntity<?> forwardPatientDeleteRequest(
            @RequestHeader("Authorization") String token
    );
}
