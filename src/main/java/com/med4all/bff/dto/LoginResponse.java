package com.med4all.bff.dto;

import com.med4all.bff.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private Long id;
    private String email;
    private Role role;

    public LoginResponse(String jwt, Long id, String email, Role name) {
        this.token = jwt;
        this.id = id;
        this.email = email;
        this.role = name;
    }
}
