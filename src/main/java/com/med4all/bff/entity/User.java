package com.med4all.bff.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String licenseNumber; // for DOCTOR/DISPENSARY

    private String certificatePath; // for DOCTOR/DISPENSARY

    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.PENDING; // NEW FIELD

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private Long approvedBy; // Admin ID who approved

    private LocalDateTime approvedAt;

    private String rejectionReason; // If rejected
}

