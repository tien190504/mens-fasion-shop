package com.powerranger.fashion_shop_backend.repository;

import com.powerranger.fashion_shop_backend.entity.UserMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMeasurementRepository extends JpaRepository<UserMeasurement, Long> {
    Optional<UserMeasurement> findByUserEmail(String email);
}
