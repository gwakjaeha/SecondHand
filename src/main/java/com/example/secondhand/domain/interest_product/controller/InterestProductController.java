package com.example.secondhand.domain.interest_product.controller;

import static com.example.secondhand.global.status.StatusTrue.ADD_INTEREST_PRODUCT_INFO_TRUE;
import static com.example.secondhand.global.status.StatusTrue.DELETE_INTEREST_PRODUCT_INFO_TRUE;
import static com.example.secondhand.global.status.StatusTrue.READ_INTEREST_PRODUCT_INFO_TRUE;
import static com.example.secondhand.global.status.StatusTrue.READ_POPULAR_PRODUCT_INFO_TRUE;

import com.example.secondhand.domain.interest_product.entity.InterestProduct;
import com.example.secondhand.domain.interest_product.service.InterestProductService;
import com.example.secondhand.domain.interest_product.dto.AddInterestProductDto;
import com.example.secondhand.domain.interest_product.dto.DeleteInterestProductDto;
import com.example.secondhand.domain.interest_product.dto.ReadInterestProductListDto;
import com.example.secondhand.domain.interest_product.dto.ReadPopularProductListDto;
import com.example.secondhand.domain.product.entity.Product;
import com.example.secondhand.global.dto.ApiResponse;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main")
public class InterestProductController {

	private final InterestProductService interestProductService;

	@ApiOperation(value = "관심 상품으로 지정합니다.")
	@PostMapping(value = "/interest-product")
	public ApiResponse<String> addInterestProduct(
		@Valid @RequestBody AddInterestProductDto.Request request){
		interestProductService.addInterestProduct(request);
		return ApiResponse.success(ADD_INTEREST_PRODUCT_INFO_TRUE);
	}

	@ApiOperation(value = "내 관심 상품 목록을 조회합니다.")
	@GetMapping(value = "/interest-product")
	public ApiResponse<Page<InterestProduct>> readInterestProduct(
		@Valid @RequestBody ReadInterestProductListDto.Request request){
		Page<InterestProduct> response = interestProductService.readInterestProduct(request);
		return ApiResponse.success(READ_INTEREST_PRODUCT_INFO_TRUE, response);
	}

	@ApiOperation(value = "관심 상품을 취소합니다.")
	@DeleteMapping(value = "/interest-product")
	public ApiResponse<Page<InterestProduct>> deleteInterestProduct(
		@Valid @RequestBody DeleteInterestProductDto.Request request){
		interestProductService.deleteInterestProduct(request);
		return ApiResponse.success(DELETE_INTEREST_PRODUCT_INFO_TRUE);
	}

	@ApiOperation(value = "인기 물품을 조회합니다.")
	@GetMapping("/popular-product")
	public ApiResponse<Page<Product>> readPopularProduct
		(@Valid @RequestBody ReadPopularProductListDto.Request request){
		Page<Product> response = interestProductService.readPopularProductList(request);
		return ApiResponse.success(READ_POPULAR_PRODUCT_INFO_TRUE, response);
	}
}
