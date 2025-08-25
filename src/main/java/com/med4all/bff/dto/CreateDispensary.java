package com.med4all.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class CreateDispensary {
    private String name;
    private String email;
}
