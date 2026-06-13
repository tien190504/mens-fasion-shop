package com.powerranger.fashion_shop_backend.repository;

import com.powerranger.fashion_shop_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    boolean existsByAdminTrue();

    boolean existsByAdminTrueAndActiveTrueAndIdNot(Long id);

    @Query("""
            SELECT u FROM User u
            WHERE (:active IS NULL OR u.active = :active)
              AND (:admin IS NULL OR u.admin = :admin)
              AND (
                    :keyword = ''
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(u.phone, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<User> searchForAdmin(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            @Param("admin") Boolean admin,
            Pageable pageable);
}
