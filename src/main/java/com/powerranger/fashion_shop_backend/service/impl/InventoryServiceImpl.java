package com.powerranger.fashion_shop_backend.service.impl;

import com.powerranger.fashion_shop_backend.dto.response.MovementResponseDTO;
import com.powerranger.fashion_shop_backend.entity.InventoryMovement;
import com.powerranger.fashion_shop_backend.entity.ProductVariant;
import com.powerranger.fashion_shop_backend.entity.enums.InventoryReason;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.mapper.InventoryMovementMapper;
import com.powerranger.fashion_shop_backend.repository.InventoryMovementRepository;
import com.powerranger.fashion_shop_backend.repository.ProductVariantRepository;
import com.powerranger.fashion_shop_backend.service.InventoryService;
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
    public List<MovementResponseDTO> getMovementsByVariant(Long variantId) {
        return inventoryMovementRepository.findByVariantId(variantId).stream()
                .map(InventoryMovementMapper::toResponse)
                .toList();
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
