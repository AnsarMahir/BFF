// dto/ApprovalRequest.java
package com.med4all.bff.dto;

import lombok.Data;

@Data
public class ApprovalRequest {
    private Long approvedBy;
    private String remarks;
}