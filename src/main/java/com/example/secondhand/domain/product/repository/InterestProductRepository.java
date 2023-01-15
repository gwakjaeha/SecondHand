package com.example.secondhand.domain.product.repository;

import com.example.secondhand.domain.product.entity.InterestProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestProductRepository extends JpaRepository<InterestProduct, Long> {
	Page<InterestProduct> findByUserId(Long userId, Pageable pageable);
	void deleteByInterestProductIdAndUserId(Long interestProductId, Long userId);
}
