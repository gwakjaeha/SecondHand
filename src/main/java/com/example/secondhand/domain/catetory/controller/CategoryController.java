package com.example.secondhand.domain.catetory.controller;

import static com.example.secondhand.global.status.StatusTrue.ADD_CATEGORY_INFO_TRUE;
import static com.example.secondhand.global.status.StatusTrue.DELETE_CATEGORY_INFO_TRUE;
import static com.example.secondhand.global.status.StatusTrue.READ_CATEGORY_LIST_INFO_TRUE;
import static com.example.secondhand.global.status.StatusTrue.UPDATE_CATEGORY_INFO_TRUE;

import com.example.secondhand.domain.catetory.dto.AddCategoryDto;
import com.example.secondhand.domain.catetory.dto.UpdateCategoryDto;
import com.example.secondhand.domain.catetory.service.CategoryService;
import com.example.secondhand.global.dto.ApiResponse;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main")
public class CategoryController {

	private final CategoryService categoryService;

	@ApiOperation(value = "카테고리 목록을 조회합니다.")
	@GetMapping("/category")
	public ApiResponse<List<String>> readCategoryList(){
		List<String> response = categoryService.readCategoryList();
		return ApiResponse.success(READ_CATEGORY_LIST_INFO_TRUE, response);
	}

	@ApiOperation(value = "카테고리를 등록합니다.")
	@PostMapping("/category")
	public ApiResponse<String> addCategory(
		@Valid @RequestBody AddCategoryDto.Request request){
		categoryService.addCategory(request);
		return ApiResponse.success(ADD_CATEGORY_INFO_TRUE);
	}

	@ApiOperation(value = "카테고리를 수정합니다.")
	@PutMapping("/category")
	public ApiResponse<String> updateCategory(
		@Valid @RequestBody UpdateCategoryDto.Request request){
		categoryService.updateCategory(request);
		return ApiResponse.success(UPDATE_CATEGORY_INFO_TRUE);
	}

	@ApiOperation(value = "카테고리를 삭제합니다.")
	@DeleteMapping("/category/{categoryName}")
	public ApiResponse<String> deleteCategory(
		@PathVariable("categoryName") String categoryName){
		categoryService.deleteCategory(categoryName);
		return ApiResponse.success(DELETE_CATEGORY_INFO_TRUE);
	}
}
