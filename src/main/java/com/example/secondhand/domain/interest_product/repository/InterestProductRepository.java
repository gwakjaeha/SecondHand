package com.example.secondhand.domain.interest_product.repository;

import com.example.secondhand.domain.interest_product.entity.InterestProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestProductRepository extends JpaRepository<InterestProduct, Long> {
	Page<InterestProduct> findByUserId(Long userId, Pageable pageable);
	void deleteByIdAndUserId(Long interestProductId, Long userId);
	Long countByProductId(Long productId);
}
