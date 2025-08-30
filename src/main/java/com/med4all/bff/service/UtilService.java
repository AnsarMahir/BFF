package com.med4all.bff.service;

import com.med4all.bff.client.DispensaryServiceClient;
import com.med4all.bff.client.DoctorServiceClient;
import com.med4all.bff.client.PatientServiceClient;
import com.med4all.bff.dto.CreateDispensary;
import com.med4all.bff.entity.Role;
import com.med4all.bff.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UtilService {
    public UtilService(PatientServiceClient patientServiceClient, DispensaryServiceClient dispensaryServiceClient, DoctorServiceClient doctorServiceClient) {
        this.patientServiceClient = patientServiceClient;
        this.dispensaryServiceClient = dispensaryServiceClient;
        this.doctorServiceClient = doctorServiceClient;
    }
    private final PatientServiceClient patientServiceClient;
    private final DispensaryServiceClient dispensaryServiceClient;

    private final DoctorServiceClient doctorServiceClient;

    public void createDispensaryProfile(User user) {
        try {
            CreateDispensary dispensaryRequest = CreateDispensary.builder()
                    .email(user.getEmail())
                    .name(user.getFullName())
                    .build();

            if(user.getRole().equals(Role.DISPENSARY)){
                dispensaryServiceClient.createDispensary(dispensaryRequest);
                return;
            }

            if(user.getRole().equals(Role.DOCTOR)){
                doctorServiceClient.createDoctor(dispensaryRequest);
                return;
            }

            return;
        } catch (Exception e) {
            // Improved error handling
            log.info( "Failed to create dispensary profile for user: " + user.getEmail(), e);
            // Consider adding retry mechanism or dead-letter queue here
        }
    }

    public void createPatientProfile(User user) {
        try {
            CreateDispensary dispensaryRequest = CreateDispensary.builder()
                    .email(user.getEmail())
                    .name(user.getFullName())
                    .build();

            patientServiceClient.createPatient(dispensaryRequest);
        } catch (Exception e) {
            // Improved error handling
            log.info( "Failed to create dispensary profile for user: " + user.getEmail(), e);
            // Consider adding retry mechanism or dead-letter queue here
        }
    }
}
