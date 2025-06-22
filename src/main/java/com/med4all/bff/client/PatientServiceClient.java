package com.med4all.bff.client;

import com.med4all.bff.config.FeignConfiguration;
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
            @RequestHeader("Authorization") String token,
            @RequestParam Map<String, String> params
    );

    @PostMapping("/api/patient/**")
    ResponseEntity<?> forwardPatientPostRequest(
            @RequestHeader("Authorization") String token,
            @RequestBody Object body
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
