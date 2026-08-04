package io.datasheild.auth.repository;

import io.datasheild.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.email = :email AND u.deleted = false")
    Optional<User> findByTenantIdAndEmail(@Param("tenantId") UUID tenantId, @Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.username = :username AND u.deleted = false")
    Optional<User> findByTenantIdAndUsername(@Param("tenantId") UUID tenantId, @Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.id = :userId AND u.tenantId = :tenantId AND u.deleted = false")
    Optional<User> findByIdAndTenantId(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.status = :status AND u.deleted = false ORDER BY u.createdAt DESC")
    List<User> findByTenantIdAndStatus(@Param("tenantId") UUID tenantId, @Param("status") User.UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.deleted = false")
    long countByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.mfaEnabled = true AND u.deleted = false")
    List<User> findByTenantIdWithMFAEnabled(@Param("tenantId") UUID tenantId);
}
