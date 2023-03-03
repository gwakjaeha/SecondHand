package com.example.secondhand.domain.product.repository;

import com.example.secondhand.domain.product.entity.Product;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	Page<Product> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);
	Page<Product> findByIdIsIn(Set<Long> set, Pageable pageable);
}
