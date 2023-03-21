package com.example.secondhand.domain.catetory.service;

import static com.example.secondhand.global.exception.CustomErrorCode.EXIST_PRODUCT_IN_CATEGORY;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EXIST_CATEGORY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.secondhand.domain.catetory.dto.AddCategoryDto;
import com.example.secondhand.domain.catetory.dto.UpdateCategoryDto;
import com.example.secondhand.domain.catetory.entity.Category;
import com.example.secondhand.domain.catetory.repository.CategoryRepository;
import com.example.secondhand.domain.product.repository.ProductRepository;
import com.example.secondhand.global.exception.CustomException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
	@InjectMocks
	private CategoryService categoryService;
	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private ProductRepository productRepository;

	@Test
	void testReadCategoryList() throws Exception{
		//given
		given(categoryRepository.findAll())
			.willReturn(Arrays.asList(Category.builder()
				.id(1L)
				.categoryName("categoryName")
				.build()));

		//when
		List<String> response = categoryService.readCategoryList();

		//then
		assertEquals("categoryName", response.get(0));
	}

	@Test
	void testAddCategory() throws Exception{
		//given
		given(categoryRepository.save(any()))
			.willReturn(Category.builder()
				.id(1L)
				.categoryName("categoryName")
				.build());

		ArgumentCaptor<Category> categoryArgumentCaptor = ArgumentCaptor.forClass(Category.class);

		//when
		categoryService.addCategory(AddCategoryDto.Request.builder()
			.categoryName("categoryName")
			.build());

		//then
		verify(categoryRepository,times(1)).save(categoryArgumentCaptor.capture());
		assertEquals("categoryName", categoryArgumentCaptor.getValue().getCategoryName());
	}

	@Test
	void testUpdateCategory() throws Exception{
		//given
		given(categoryRepository.findByCategoryName(anyString()))
			.willReturn(Optional.ofNullable(Category.builder()
				.id(1L)
				.categoryName("categoryName")
				.build()));
		given(categoryRepository.save(any()))
			.willReturn(Category.builder()
				.id(1L)
				.categoryName("newCategoryName")
				.build());

		ArgumentCaptor<Category> categoryArgumentCaptor = ArgumentCaptor.forClass(Category.class);

		//when
		categoryService.updateCategory(UpdateCategoryDto.Request.builder()
			.categoryName("categoryName")
			.newCategoryName("newCategoryName")
			.build());

		//then
		verify(categoryRepository,times(1)).findByCategoryName(anyString());
		verify(categoryRepository,times(1)).save(categoryArgumentCaptor.capture());
		assertEquals("newCategoryName", categoryArgumentCaptor.getValue().getCategoryName());
	}

	@Test
	void testNotExistCategoryInUpdateCategory() throws Exception{
		//given
		given(categoryRepository.findByCategoryName(anyString()))
			.willReturn(Optional.empty());

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> categoryService.updateCategory(UpdateCategoryDto.Request.builder()
				.categoryName("categoryName")
				.newCategoryName("newCategoryName")
				.build()));

		//then
		assertEquals(NOT_EXIST_CATEGORY, exception.getCustomErrorCode());
	}

	@Test
	void testDeleteCategory() throws Exception{
		//given
		given(categoryRepository.findByCategoryName(anyString()))
			.willReturn(Optional.ofNullable(Category.builder()
				.id(1L)
				.categoryName("categoryName")
				.build()));
		given(productRepository.existsByCategory(any()))
			.willReturn(false);
		willDoNothing().given(categoryRepository).deleteByCategoryName(any());

		ArgumentCaptor<String> categoryNameArgumentCaptor = ArgumentCaptor.forClass(String.class);

		//when
		categoryService.deleteCategory("categoryName");

		//then
		verify(categoryRepository,times(1)).findByCategoryName(anyString());
		verify(categoryRepository,times(1)).deleteByCategoryName(categoryNameArgumentCaptor.capture());
		assertEquals("categoryName", categoryNameArgumentCaptor.getValue());
	}

	@Test
	void testNotExistCategoryInDeleteCategory() throws Exception{
		//given
		given(categoryRepository.findByCategoryName(anyString()))
			.willReturn(Optional.empty());

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> categoryService.deleteCategory("categoryName"));

		//then
		assertEquals(NOT_EXIST_CATEGORY, exception.getCustomErrorCode());
	}

	@Test
	void testExistProductInCategoryInDeleteCategory() throws Exception{
		//given
		given(categoryRepository.findByCategoryName(anyString()))
			.willReturn(Optional.ofNullable(Category.builder()
				.id(1L)
				.categoryName("categoryName")
				.build()));
		given(productRepository.existsByCategory(any()))
			.willReturn(true);

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> categoryService.deleteCategory("categoryName"));

		//then
		assertEquals(EXIST_PRODUCT_IN_CATEGORY, exception.getCustomErrorCode());
	}
}