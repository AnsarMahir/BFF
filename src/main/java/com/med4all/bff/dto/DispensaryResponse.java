package com.med4all.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DispensaryResponse {
    private Long id;
    private String email;
    private String name;
    private String address;
    private Double longitude;
    private Double latitude;
    private String licenseNumber;
    private String ownerName;
    private Boolean isValid;
    private Boolean isLocatable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}