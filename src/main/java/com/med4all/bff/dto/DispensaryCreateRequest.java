// dto/DispensaryCreateRequest.java
package com.med4all.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DispensaryCreateRequest {
    private String email;
    private String name;
    private String address;
    private Double longitude;
    private Double latitude;
    private String licenseNumber;
    private String ownerName; // From user's fullName
    private boolean isValid = false; // Default to false until approved
}