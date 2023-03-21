package com.example.secondhand.domain.catetory.service;

import static com.example.secondhand.global.exception.CustomErrorCode.EXIST_PRODUCT_IN_CATEGORY;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EXIST_CATEGORY;

import com.example.secondhand.domain.catetory.dto.AddCategoryDto;
import com.example.secondhand.domain.catetory.dto.UpdateCategoryDto.Request;
import com.example.secondhand.domain.catetory.entity.Category;
import com.example.secondhand.domain.catetory.repository.CategoryRepository;
import com.example.secondhand.domain.product.repository.ProductRepository;
import com.example.secondhand.global.exception.CustomException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;

	@Transactional
	public List<String> readCategoryList() {
		List<String> categoryList = categoryRepository.findAll()
			.stream().map(Category -> Category.getCategoryName()).collect(Collectors.toList());
		return categoryList;
	}

	@Transactional
	public void addCategory(AddCategoryDto.Request request) {
		categoryRepository.save(Category.builder()
			.categoryName(request.getCategoryName())
			.build());
	}

	@Transactional
	public void updateCategory(Request request) {
		Category category = categoryRepository.findByCategoryName(request.getCategoryName())
			.orElseThrow(()-> new CustomException(NOT_EXIST_CATEGORY));

		category.setCategoryName(request.getNewCategoryName());

		categoryRepository.save(category);
	}

	@Transactional
	public void deleteCategory(String categoryName) {
		Category category = categoryRepository.findByCategoryName(categoryName)
			.orElseThrow(()-> new CustomException(NOT_EXIST_CATEGORY));

		boolean checkExistProduct = productRepository.existsByCategory(category);

		if(checkExistProduct){
			throw new CustomException(EXIST_PRODUCT_IN_CATEGORY);
		}

		categoryRepository.deleteByCategoryName(category.getCategoryName());
	}
}
