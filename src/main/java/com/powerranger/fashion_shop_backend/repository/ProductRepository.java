package com.powerranger.fashion_shop_backend.repository;

import com.powerranger.fashion_shop_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("select distinct p.name from Product p where lower(p.name) like concat('%', :keyword, '%') order by p.name asc")
    List<String> findNamesByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
