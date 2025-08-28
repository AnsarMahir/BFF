package com.med4all.bff.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CertificateRequest {
    private String email;
}
