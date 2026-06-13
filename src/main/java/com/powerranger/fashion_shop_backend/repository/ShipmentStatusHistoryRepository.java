package com.powerranger.fashion_shop_backend.repository;

import com.powerranger.fashion_shop_backend.entity.ShipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentStatusHistoryRepository extends JpaRepository<ShipmentStatusHistory, Long> {
    List<ShipmentStatusHistory> findByShipmentIdOrderByCreatedAtDescIdDesc(Long shipmentId);
}
