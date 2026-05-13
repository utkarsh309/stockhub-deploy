package com.stockhub.auth.repository;

import com.stockhub.auth.entity.User;
import com.stockhub.auth.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Integer> {

    //  Find Methods

    // Find user by email
    // Used in: login, registration check
    Optional<User> findByEmail(String email);

    // Find all users by role
    // Used in: Admin viewing users by role
    List<User> findByRole(Role role);

    // Find all active users
    // Used in: Admin user management
    List<User> findByIsActiveTrue();

    // Find all inactive users
    List<User> findByIsActiveFalse();

    // Find by department
    // Used in: department level filtering
    List<User> findByDepartment(String department);

    // ─── Check Methods ─────────────────────────

    // Check if email already exists
    // Used in: registration validation
    boolean existsByEmail(String email);

    // ─── Custom Query Methods ──────────────────

    // Update last login time
    // Used in: after successful login
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :time WHERE u.userId = :userId")
    void updateLastLoginAt(
            @Param("userId") Integer userId,
            @Param("time") LocalDateTime time);

    // Deactivate user
    // Used in: Admin deactivating a user
    @Modifying
    @Query("UPDATE User u SET u.isActive = false WHERE u.userId = :userId")
    void deactivateUser(
            @Param("userId") Integer userId);
}