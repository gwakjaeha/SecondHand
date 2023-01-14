package com.example.secondhand.domain.product.controller;

import static com.example.secondhand.domain.user.status.StatusTrue.ADD_PRODUCT_INFO_TRUE;
import static com.example.secondhand.domain.user.status.StatusTrue.DELETE_PRODUCT_INFO_TRUE;
import static com.example.secondhand.domain.user.status.StatusTrue.READ_PRODUCT_INFO_TRUE;
import static com.example.secondhand.domain.user.status.StatusTrue.SAVE_PRODUCT_DOCUMENT_TRUE;
import static com.example.secondhand.domain.user.status.StatusTrue.UPDATE_PRODUCT_INFO_TRUE;

import com.example.secondhand.domain.product.dto.AddProductDto;
import com.example.secondhand.domain.product.dto.DeleteProductDto;
import com.example.secondhand.domain.product.dto.ReadProductListDto;
import com.example.secondhand.domain.product.dto.UpdateProductDto;
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

	@GetMapping("/product/save")
	public ApiResponse<String> saveProductDocumentInElasticsearch(){
		productService.saveAllProductDocuments();
		return ApiResponse.success(SAVE_PRODUCT_DOCUMENT_TRUE);
	}

	@PutMapping(value = "/product", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponse<String> updateProduct(
		@Valid @RequestPart UpdateProductDto.Request request, @RequestPart(required = false) MultipartFile imgFile){
		productService.updateProduct(request, imgFile);
		return ApiResponse.success(UPDATE_PRODUCT_INFO_TRUE);
	}

	@DeleteMapping(value = "/product")
	public ApiResponse<String> deleteProduct(
		@Valid @RequestBody DeleteProductDto.Request request){
		productService.deleteProduct(request);
		return ApiResponse.success(DELETE_PRODUCT_INFO_TRUE);
	}
}
