package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.request.ProductVariantRequest;
import com.powerranger.fashion_shop_backend.dto.response.ProductVariantResponse;
import com.powerranger.fashion_shop_backend.entity.Product;
import com.powerranger.fashion_shop_backend.entity.ProductVariant;
import com.powerranger.fashion_shop_backend.exception.BadRequestException;
import com.powerranger.fashion_shop_backend.exception.NotFoundException;
import com.powerranger.fashion_shop_backend.repository.ProductRepository;
import com.powerranger.fashion_shop_backend.repository.ProductVariantRepository;
import com.powerranger.fashion_shop_backend.service.impl.ProductVariantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductVariantServiceImplTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductVariantServiceImpl productVariantService;

    private Product product;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("T-Shirt");

        variant = new ProductVariant();
        variant.setId(10L);
        variant.setProduct(product);
        variant.setSku("TSHIRT-M-BLK");
        variant.setSize("M");
        variant.setColor("Black");
        variant.setPrice(BigDecimal.valueOf(150000));
        variant.setStockQuantity(50);
        variant.setActive(true);
    }

    @Test
    void getAllVariantsSuccessfully() {
        when(productVariantRepository.findByProductIdOrderByIdAsc(1L)).thenReturn(List.of(variant));

        List<ProductVariantResponse> result = productVariantService.getAll(1L);

        assertEquals(1, result.size());
        assertEquals("TSHIRT-M-BLK", result.get(0).sku());
    }

    @Test
    void getVariantByIdSuccessfully() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(variant));

        ProductVariantResponse result = productVariantService.getById(10L);

        assertNotNull(result);
        assertEquals("TSHIRT-M-BLK", result.sku());
    }

    @Test
    void getVariantByIdThrowsNotFound() {
        when(productVariantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productVariantService.getById(99L));
    }

    @Test
    void createVariantSuccessfully() {
        ProductVariantRequest request = new ProductVariantRequest(
                1L, "TSHIRT-M-BLK-NEW", "M", "Black", BigDecimal.valueOf(150000), 50, "image.png", true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productVariantRepository.findBySku("TSHIRT-M-BLK-NEW")).thenReturn(Optional.empty());
        when(productVariantRepository.save(any(ProductVariant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductVariantResponse result = productVariantService.create(request);

        assertNotNull(result);
        assertEquals("TSHIRT-M-BLK-NEW", result.sku());
        verify(productVariantRepository, times(1)).save(any(ProductVariant.class));
    }

    @Test
    void createVariantThrowsWhenSkuExists() {
        ProductVariantRequest request = new ProductVariantRequest(
                1L, "TSHIRT-M-BLK", "M", "Black", BigDecimal.valueOf(150000), 50, "image.png", true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productVariantRepository.findBySku("TSHIRT-M-BLK")).thenReturn(Optional.of(variant));

        assertThrows(BadRequestException.class, () -> productVariantService.create(request));
    }

    @Test
    void updateVariantSuccessfully() {
        ProductVariantRequest request = new ProductVariantRequest(
                1L, "TSHIRT-M-BLK-UPD", "M", "Black", BigDecimal.valueOf(160000), 40, "image2.png", true);

        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(variant));
        when(productVariantRepository.findBySku("TSHIRT-M-BLK-UPD")).thenReturn(Optional.empty());
        when(productVariantRepository.save(any(ProductVariant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductVariantResponse result = productVariantService.update(10L, request);

        assertNotNull(result);
        assertEquals("TSHIRT-M-BLK-UPD", result.sku());
        assertEquals(BigDecimal.valueOf(160000), result.price());
    }

    @Test
    void deleteVariantSuccessfully() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(variant));
        doNothing().when(productVariantRepository).delete(variant);

        assertDoesNotThrow(() -> productVariantService.delete(10L));
        verify(productVariantRepository, times(1)).delete(variant);
    }
}
