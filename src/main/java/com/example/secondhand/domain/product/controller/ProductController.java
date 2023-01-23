package com.example.secondhand.domain.product.controller;

import static com.example.secondhand.domain.user.status.StatusTrue.*;

import com.example.secondhand.domain.product.dto.AddInterestProductDto;
import com.example.secondhand.domain.product.dto.AddProductDto;
import com.example.secondhand.domain.product.dto.DeleteInterestProductDto;
import com.example.secondhand.domain.product.dto.DeleteProductDto;
import com.example.secondhand.domain.product.dto.ReadInterestProductListDto;
import com.example.secondhand.domain.product.dto.ReadMySellingProductListDto;
import com.example.secondhand.domain.product.dto.ReadPopularProductListDto;
import com.example.secondhand.domain.product.dto.ReadProductListDto;
import com.example.secondhand.domain.product.dto.UpdateProductDto;
import com.example.secondhand.domain.product.entity.InterestProduct;
import com.example.secondhand.domain.product.entity.Product;
import com.example.secondhand.domain.product.entity.ProductDocument;
import com.example.secondhand.domain.product.service.ProductService;
import com.example.secondhand.global.dto.ApiResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main")
public class ProductController {

	private final ProductService productService;

	@GetMapping("/product")
	public ApiResponse<Page<ProductDocument>> readProduct(
		@Valid @RequestBody ReadProductListDto.Request request){
			Page<ProductDocument> response = productService.readProductList(request);
			return ApiResponse.success(READ_PRODUCT_INFO_TRUE,response);
	}

	@PostMapping(value = "/product", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponse<String> addProduct(
		@Valid @RequestPart AddProductDto.Request request, @RequestPart(required = false) MultipartFile imgFile){
		productService.addProduct(request, imgFile);
		return ApiResponse.success(ADD_PRODUCT_INFO_TRUE);
	}

	@PutMapping(value = "/my-product", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponse<String> updateProduct(
		@Valid @RequestPart UpdateProductDto.Request request, @RequestPart(required = false) MultipartFile imgFile){
		productService.updateProduct(request, imgFile);
		return ApiResponse.success(UPDATE_PRODUCT_INFO_TRUE);
	}

	@DeleteMapping(value = "/my-product")
	public ApiResponse<String> deleteProduct(
		@Valid @RequestBody DeleteProductDto.Request request){
		productService.deleteProduct(request);
		return ApiResponse.success(DELETE_PRODUCT_INFO_TRUE);
	}

	@GetMapping("/my-product")
	public ApiResponse<Page<Product>> readMySellingProduct
		(@Valid @RequestBody ReadMySellingProductListDto.Request request){
		Page<Product> response = productService.readMySellingProductList(request);
		return ApiResponse.success(READ_MY_SELLING_PRODUCT_INFO_TRUE, response);
	}

	@PostMapping(value = "/interest-product")
	public ApiResponse<String> addInterestProduct(
		@Valid @RequestBody AddInterestProductDto.Request request){
		productService.addInterestProduct(request);
		return ApiResponse.success(ADD_INTEREST_PRODUCT_INFO_TRUE);
	}

	@GetMapping(value = "/interest-product")
	public ApiResponse<Page<InterestProduct>> readInterestProduct(
		@Valid @RequestBody ReadInterestProductListDto.Request request){
		Page<InterestProduct> response = productService.readInterestProduct(request);
		return ApiResponse.success(READ_INTEREST_PRODUCT_INFO_TRUE, response);
	}

	@DeleteMapping(value = "/interest-product")
	public ApiResponse<Page<InterestProduct>> deleteInterestProduct(
		@Valid @RequestBody DeleteInterestProductDto.Request request){
		productService.deleteInterestProduct(request);
		return ApiResponse.success(DELETE_INTEREST_PRODUCT_INFO_TRUE);
	}

	@GetMapping("/popular-product")
	public ApiResponse<Page<Product>> readPopularProduct
		(@Valid @RequestBody ReadPopularProductListDto.Request request){
		Page<Product> response = productService.readPopularProductList(request);
		return ApiResponse.success(READ_POPULAR_PRODUCT_INFO_TRUE, response);
	}
}
