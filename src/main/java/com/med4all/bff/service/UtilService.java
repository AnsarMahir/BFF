package com.med4all.bff.service;

import com.med4all.bff.client.DispensaryServiceClient;
import com.med4all.bff.dto.CreateDispensary;
import com.med4all.bff.entity.User;
import lombok.extern.slf4j.Slf4j;
import com.med4all.bff.client.DispensaryServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
@Slf4j
@Service
public class UtilService {
    public UtilService(DispensaryServiceClient dispensaryServiceClient) {
        this.dispensaryServiceClient = dispensaryServiceClient;
    }
    private final DispensaryServiceClient dispensaryServiceClient;

    public void createDispensaryProfile(User user) {
        try {
            CreateDispensary dispensaryRequest = CreateDispensary.builder()
                    .email(user.getEmail())
                    .name(user.getFullName())
                    .build();

            dispensaryServiceClient.createDispensary(dispensaryRequest);
        } catch (Exception e) {
            // Improved error handling
            log.info( "Failed to create dispensary profile for user: " + user.getEmail(), e);
            // Consider adding retry mechanism or dead-letter queue here
        }
    }
}
