package com.example.secondhand.domain.product.service;

import static com.example.secondhand.global.exception.CustomErrorCode.*;

import com.example.secondhand.domain.product.domain.Product;
import com.example.secondhand.domain.product.dto.AddProductDto.Request;
import com.example.secondhand.domain.product.dto.DeleteProductDto;
import com.example.secondhand.domain.product.dto.ReadProductListDto;
import com.example.secondhand.domain.product.dto.UpdateProductDto;
import com.example.secondhand.domain.product.repository.ProductRepository;
import com.example.secondhand.domain.user.dto.TokenInfoResponseDto;
import com.example.secondhand.domain.user.service.AccountService;
import com.example.secondhand.global.exception.CustomException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final AccountService accountService;
	@Value("${baseLocalPath}")
	private String baseLocalPath;
	@Value("${baseUrlPath}")
	private String baseUrlPath;

	public List<ReadProductListDto.Response> readProductList(ReadProductListDto.Request request) {
		readProductValidation(request);
		List<Product> productList = productRepository.findByAreaIdAndCategoryIdAndDeleteDtIsNull(request.getAreaId(), request.getCategoryId());
		productListValidation(productList);

		return productList.stream().map(ReadProductListDto.Response::response).toList();
	}

	public void addProduct(Request request, MultipartFile imgFile) {
		addProductValidation(request);
		TokenInfoResponseDto tokenInfo = accountService.getTokenInfo();
		String imgFilePath = getSavedImageFilePath(imgFile);
		productRepository.save(
			Product.builder()
				.userId(tokenInfo.getUserId())
				.areaId(tokenInfo.getAreaId())
				.categoryId(request.getCategoryId())
				.title(request.getTitle())
				.content(request.getContent())
				.imagePath(imgFilePath)
				.price(request.getPrice())
				.transactionPlace(request.getTransactionPlace())
				.transactionStatus(true)
				.build());
	}

	public void updateProduct(UpdateProductDto.Request request, MultipartFile imgFile) {
		updateProductValidation(request);
		Product product = productRepository.findById(request.getProductId()).get();
		String imgFilePath = getSavedImageFilePath(imgFile);
		productRepository.save(
			Product.builder()
				.productId(request.getProductId())
				.userId(product.getUserId())
				.areaId(product.getAreaId())
				.categoryId(request.getCategoryId())
				.title(request.getTitle())
				.content(request.getContent())
				.imagePath(imgFilePath)
				.price(request.getPrice())
				.transactionPlace(request.getTransactionPlace())
				.transactionStatus(request.isTransactionStatus())
				.createdDt(product.getCreatedDt())
				.build());
	}

	public void deleteProduct(DeleteProductDto.Request request) {
		if(request.getProductId() == null){
			throw new CustomException(DELETE_PRODUCT_INFO_NULL);
		}
		Product product = productRepository.findById(request.getProductId()).get();
		productRepository.save(
			Product.builder()
				.productId(product.getProductId())
				.userId(product.getUserId())
				.areaId(product.getAreaId())
				.categoryId(product.getCategoryId())
				.title(product.getTitle())
				.content(product.getContent())
				.imagePath(product.getImagePath())
				.price(product.getPrice())
				.transactionPlace(product.getTransactionPlace())
				.createdDt(product.getCreatedDt())
				.updatedDt(product.getUpdatedDt())
				.transactionStatus(product.isTransactionStatus())
				.deleteDt(LocalDateTime.now())
				.build());
	}

	private void readProductValidation(ReadProductListDto.Request request) {
		if(request.getCategoryId() == null || request.getAreaId() == null){
			throw new CustomException(READ_PRODUCT_INFO_NULL);
		}
	}

	private void productListValidation(List<Product> productList) {
		if(productList.isEmpty()){
			throw new CustomException(NOT_EXIST_PRODUCT);
		}
	}

	private void addProductValidation(Request request) {
		if(request.getCategoryId() == null || request.getTitle() == null || request.getContent() == null
		|| request.getPrice() == null || request.getTransactionPlace() == null){
			throw new CustomException(ADD_PRODUCT_INFO_NULL);
		}
	}

	private void updateProductValidation(UpdateProductDto.Request request) {
		if(request.getProductId() == null || request.getCategoryId() == null || request.getTitle() == null
			|| request.getContent() == null || request.getPrice() == null || request.getTransactionPlace() == null){
			throw new CustomException(UPDATE_PRODUCT_INFO_NULL);
		}
	}

	private String getSavedImageFilePath(MultipartFile imgFile){

		String saveFilename = "";
		String urlFilename = "";

		if(imgFile != null) {
			String originalFilename = imgFile.getOriginalFilename();

			String[] arrFilename = getNewImgFilePath(baseLocalPath, baseUrlPath, originalFilename);

			saveFilename = arrFilename[0];
			urlFilename = arrFilename[1];

			try {
				File newFile = new File(saveFilename);
				FileCopyUtils.copy(imgFile.getInputStream(), new FileOutputStream(newFile));
			} catch (IOException e) {
				throw new CustomException(SAVE_IMAGE_FILE_FALSE);
			}
		}

		return urlFilename;
	}

	private String[] getNewImgFilePath(String baseLocalPath, String baseUrlPath, String originalFilename) {

		LocalDate now = LocalDate.now();

		String[] dirs = {
			String.format("%s/%d/", baseLocalPath, now.getYear()),
			String.format("%s/%d/%02d/", baseLocalPath, now.getYear(), now.getMonthValue()),
			String.format("%s/%d/%02d/%02d/", baseLocalPath, now.getYear(), now.getMonthValue(), now.getDayOfMonth())};

		String urlDir = String.format("%s/%d/%02d/%02d/", baseUrlPath, now.getYear(), now.getMonthValue(), now.getDayOfMonth());

		for(String dir: dirs){
			File file = new File(dir);
			if(!file.isDirectory()){
				file.mkdirs();
			}
		}

		String fileExtension = "";
		if(originalFilename != null){
			int dotPos = originalFilename.lastIndexOf(".");
			if (dotPos > -1) {
				fileExtension = originalFilename.substring(dotPos + 1);
			}
		}

		String uuid = UUID.randomUUID().toString().replaceAll("-","");
		String newFilename = String.format("%s%s", dirs[2], uuid);
		String newUrlFilename = String.format("%s%s", urlDir, uuid);
		if (fileExtension.length() > 0) {
			newFilename += "." + fileExtension;
			newUrlFilename += "." + fileExtension;
		}

		return new String[]{newFilename, newUrlFilename};
	}
}
