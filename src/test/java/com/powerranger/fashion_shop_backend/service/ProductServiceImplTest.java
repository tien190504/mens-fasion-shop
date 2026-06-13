package com.powerranger.fashion_shop_backend.service;

import com.powerranger.fashion_shop_backend.dto.response.ProductSummaryResponse;
import com.powerranger.fashion_shop_backend.entity.Product;
import com.powerranger.fashion_shop_backend.repository.BrandRepository;
import com.powerranger.fashion_shop_backend.repository.CategoryRepository;
import com.powerranger.fashion_shop_backend.repository.ProductRepository;
import com.powerranger.fashion_shop_backend.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Nike Air Max");
        product.setSlug("nike-air-max");
        product.setBasePrice(BigDecimal.valueOf(2000000));
        product.setPublished(true);
    }

    @Test
    void filterProductsSuccessfully() {
        Page<Product> productPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        Page<ProductSummaryResponse> result = productService.filter(
                "nike", null, null, null, "M", "black", BigDecimal.ZERO, BigDecimal.valueOf(3000000), "price_asc", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Nike Air Max", result.getContent().get(0).name());
        verify(productRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void autocompleteSuggestionsReturnedSuccessfully() {
        when(productRepository.findNamesByKeyword(eq("nike"), any(Pageable.class))).thenReturn(List.of("Nike Air Max", "Nike Jordan"));

        List<String> result = productService.autocomplete("nike");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("Nike Air Max"));
        verify(productRepository, times(1)).findNamesByKeyword(eq("nike"), any(Pageable.class));
    }

    @Test
    void autocompleteWithEmptyKeywordReturnsEmptyList() {
        List<String> result = productService.autocomplete("");
        assertTrue(result.isEmpty());
        verifyNoInteractions(productRepository);
    }
}
