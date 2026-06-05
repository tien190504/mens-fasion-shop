package com.powerranger.mens_fashion_backend.service.impl;

import com.powerranger.mens_fashion_backend.entity.InventoryMovement;
import com.powerranger.mens_fashion_backend.entity.ProductVariant;
import com.powerranger.mens_fashion_backend.entity.enums.InventoryReason;
import com.powerranger.mens_fashion_backend.exception.NotFoundException;
import com.powerranger.mens_fashion_backend.repository.InventoryMovementRepository;
import com.powerranger.mens_fashion_backend.repository.ProductVariantRepository;
import com.powerranger.mens_fashion_backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovement> getMovementsByVariant(Long variantId) {
        return inventoryMovementRepository.findByVariantId(variantId);
    }

    @Override
    @Transactional
    public void addMovement(Long variantId, int quantity, String reason, String note) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException("Variant not found"));

        InventoryMovement movement = new InventoryMovement();
        movement.setVariant(variant);
        movement.setChangeQty(quantity);
        movement.setReason(InventoryReason.fromValue(reason));
        movement.setNote(note);

        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        productVariantRepository.save(variant);
        inventoryMovementRepository.save(movement);
    }
}
