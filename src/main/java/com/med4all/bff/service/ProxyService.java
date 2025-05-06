package com.med4all.bff.service;

import com.med4all.bff.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

@Service
public class ProxyService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.dispensary}")
    private String dispensaryServiceUrl;

    @Value("${services.doctor}")
    private String doctorServiceUrl;

    @Value("${services.patient}")
    private String patientServiceUrl;

    public ResponseEntity<?> forwardRequest(String userType, HttpServletRequest request, String actionType) {
        String targetUrl;

        switch (userType.toLowerCase()) {
            case "doctor":
                targetUrl = doctorServiceUrl + "/" + actionType;
                break;
            case "dispensary":
                targetUrl = dispensaryServiceUrl + "/" + actionType;
                break;
            case "patient":
                targetUrl = patientServiceUrl + "/" + actionType;
                break;
            default:
                throw new UnauthorizedException("Invalid user type for " + actionType);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.add(headerName, request.getHeader(headerName));
            }
            headers.setContentType(MediaType.APPLICATION_JSON);

            byte[] body = request.getInputStream().readAllBytes();
            HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);

            return restTemplate.exchange(targetUrl, HttpMethod.POST, httpEntity, String.class);

        } catch (IOException e) {
            throw new RuntimeException("Failed to forward request: " + e.getMessage());
        }
    }
}
