package com.med4all.bff.dto;

public class CurrentUserDetails {
    private Long id;
    private String email;

    public CurrentUserDetails(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
}
