package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.response.MovementResponseDTO;
import com.powerranger.fashion_shop_backend.entity.InventoryMovement;
import com.powerranger.fashion_shop_backend.entity.ProductVariant;
import com.powerranger.fashion_shop_backend.entity.enums.InventoryReason;
import com.powerranger.fashion_shop_backend.repository.InventoryMovementRepository;
import com.powerranger.fashion_shop_backend.repository.ProductVariantRepository;
import com.powerranger.fashion_shop_backend.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private ProductVariant variant;
    private InventoryMovement movement;

    @BeforeEach
    void setUp() {
        variant = new ProductVariant();
        variant.setId(10L);
        variant.setSku("VAR-10");
        variant.setStockQuantity(100);

        movement = new InventoryMovement();
        movement.setId(1L);
        movement.setVariant(variant);
        movement.setChangeQty(10);
        movement.setReason(InventoryReason.purchase);
        movement.setNote("Import goods");
    }

    @Test
    void getMovementsByVariantReturnsDTOsSuccessfully() {
        when(inventoryMovementRepository.findByVariantId(10L)).thenReturn(List.of(movement));

        List<MovementResponseDTO> result = inventoryService.getMovementsByVariant(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).variantId());
        assertEquals("VAR-10", result.get(0).sku());
        assertEquals(10, result.get(0).changeQty());
        assertEquals("purchase", result.get(0).reason());
        assertEquals("Import goods", result.get(0).note());
    }

    @Test
    void addMovementSuccessfully() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(variant));
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventoryService.addMovement(10L, 20, "purchase", "Imported 20 items");

        assertEquals(120, variant.getStockQuantity()); // 100 + 20
        verify(productVariantRepository, times(1)).save(variant);
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));
    }
}
