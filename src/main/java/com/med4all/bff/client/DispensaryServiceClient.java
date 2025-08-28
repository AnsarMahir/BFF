package com.med4all.bff.client;

import com.med4all.bff.config.CurrentUser;
import com.med4all.bff.dto.*;
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
    ResponseEntity<DispensaryResponse> createDispensary (@RequestBody CreateDispensary request);

    @PostMapping("/dispensary/profile-status")
    ResponseEntity<ProfileCompleteResponse> checkProfileStatus(@RequestBody String email);
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
