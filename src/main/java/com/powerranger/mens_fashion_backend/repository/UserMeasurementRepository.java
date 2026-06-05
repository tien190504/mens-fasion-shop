package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.UserMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMeasurementRepository extends JpaRepository<UserMeasurement, Long> {
    Optional<UserMeasurement> findByUserEmail(String email);
}
