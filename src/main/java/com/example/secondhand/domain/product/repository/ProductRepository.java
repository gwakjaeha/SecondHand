package com.example.secondhand.domain.product.repository;

import com.example.secondhand.domain.product.domain.Product;
import com.example.secondhand.domain.user.domain.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByAreaIdAndCategoryIdAndDeleteDtIsNull(long areaId, long categoryId);
}
