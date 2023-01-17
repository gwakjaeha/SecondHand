package com.example.secondhand.domain.product.repository;


import com.example.secondhand.domain.product.entity.ProductDocument;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {
	Page<ProductDocument> findByAreaIdInAndCategoryId(List<Long> areaId, Long categoryId, Pageable pageable);
	Page<ProductDocument> findBytitleOrContentOrTransactionPlaceAndAreaIdAndCategoryId(String title, String content, String transactionPlace, Long areaId, Long categoryId, Pageable pageable);
}
