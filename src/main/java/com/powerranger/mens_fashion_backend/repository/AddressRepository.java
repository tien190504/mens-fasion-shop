package com.powerranger.mens_fashion_backend.repository;

import com.powerranger.mens_fashion_backend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserEmail(String email);
    Optional<Address> findByIdAndUserEmail(Long id, String email);
}
