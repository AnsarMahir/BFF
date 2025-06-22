package com.med4all.bff.client;

import com.med4all.bff.dto.DispensaryCreateRequest;
import com.med4all.bff.dto.DispensaryResponse;
import com.med4all.bff.dto.MessageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.med4all.bff.config.FeignConfiguration;

@FeignClient(
        name = "dispensary-service",
        url = "${microservices.dispensary.url}",
        configuration = FeignConfiguration.class
)
public interface DispensaryServiceClient {
    @PostMapping("/api/dispensary")
    ResponseEntity<DispensaryResponse> createDispensary (@RequestBody DispensaryCreateRequest request);

    @PutMapping("api/dispensary/{email}/validity")
    ResponseEntity<MessageResponse> updateDispensaryValidity(
            @PathVariable String email,
            @RequestParam boolean isValid
    );

    @GetMapping("/api/dispensary/{email}")
    ResponseEntity<DispensaryResponse> getDispensaryByEmail(@PathVariable String email);

    @GetMapping("/api/dispensary")
    ResponseEntity<?> getAllDispensaries(@RequestHeader("Authorization") String token);
}
