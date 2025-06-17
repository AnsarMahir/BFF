// repository/UserRepository.java
package com.med4all.bff.repository;

import com.med4all.bff.entity.Role;
import com.med4all.bff.entity.User;
import com.med4all.bff.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Find users by status (for admin approval workflow)
    List<User> findByStatus(UserStatus status);

    // Find users by status and role (for filtering approvals by role)
    List<User> findByStatusAndRoleIn(UserStatus status, List<Role> roles);

    // Find users by role
    List<User> findByRole(Role role);

    // Find pending users by role
    List<User> findByStatusAndRole(UserStatus status, Role role);

    // Count pending approvals
    long countByStatusAndRoleIn(UserStatus status, List<Role> roles);
}