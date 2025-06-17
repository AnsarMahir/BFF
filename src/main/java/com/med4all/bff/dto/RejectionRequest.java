// dto/RejectionRequest.java
package com.med4all.bff.dto;

import lombok.Data;

@Data
public class RejectionRequest {
    private String rejectionReason;
    private Long rejectedBy;
}