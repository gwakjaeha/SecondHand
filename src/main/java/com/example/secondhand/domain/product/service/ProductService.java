package com.example.secondhand.domain.product.service;

import static com.example.secondhand.global.exception.CustomErrorCode.*;

import com.example.secondhand.domain.product.entity.Area;
import com.example.secondhand.domain.product.entity.Product;
import com.example.secondhand.domain.product.dto.AddProductDto.Request;
import com.example.secondhand.domain.product.dto.DeleteProductDto;
import com.example.secondhand.domain.product.dto.ReadProductListDto;
import com.example.secondhand.domain.product.dto.UpdateProductDto;
import com.example.secondhand.domain.product.entity.ProductDocument;
import com.example.secondhand.domain.product.repository.AreaRepository;
import com.example.secondhand.domain.product.repository.ProductRepository;
import com.example.secondhand.domain.product.repository.ProductSearchRepository;
import com.example.secondhand.domain.user.dto.TokenInfoResponseDto;
import com.example.secondhand.domain.user.service.AccountService;
import com.example.secondhand.global.exception.CustomException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductSearchRepository productSearchRepository;
	private final AreaRepository areaRepository;
	private final AccountService accountService;
	@Value("${baseLocalPath}")
	private String baseLocalPath;
	@Value("${baseUrlPath}")
	private String baseUrlPath;
	@Value("${pageSize}")
	private int pageSize;

	//키워드가 입력된 경우, (지역, 카테고리, 키워드)로 검색을 진행하고,
	//키워드가 입력되지 않은 경우, (지역, 카테고리)로만 검색을 진행함.
	//검색을 하면 내 읍면동과 같은 시군구에 속하는 모든 읍면동을 근처 동네로 설정하여 해당 근처 동네들에서 판매되는 상품들을 모두 가져옴.
	//거리를 기준으로 근처 동네를 계산하는 방식은 읍면동간의 거리들을 모두 계산해야 하므로 연산량이 큼.
	//추후 만약 거리를 고려해야 한다면, read 할때마다 거리를 계산하는 방식이 아닌,
	//미리 거리를 계산하여 근처 동네를 구성해 높은 (내 동네-근처 동네) 매핑 테이블을 만들 필요가 있음.
	@Transactional
	public Page<ProductDocument> readProductList(ReadProductListDto.Request request) {
		readProductValidation(request);

		Pageable pageable = PageRequest.of(request.getPage(), pageSize);
		Page<ProductDocument> productDocumentList = null;

		//JPA 에서는 from 절의 서브쿼리를 구현하기 어려운 관계로, 여러 쿼리 단계로 나누어서 내 근처 지역 리스트를 가져옴.
		Area area = areaRepository.findByAreaId(request.getAreaId()).get();
		//(sido, sigungu) 칼럼을 index로 설정하여, 조회 속도가 빠름.
		List<Area> areaList = areaRepository.findBySidoAndSigungu(area.getSido(), area.getSigungu());
		List<Long> areaIdList = areaList.stream().map(Area::getAreaId).collect(Collectors.toList());

		//Elasticsearch 에서 Nori 형태소 분석기를 활용하여 키워드 검색을 수행함.
		if(request.getSearchWord() == null){
			productDocumentList = productSearchRepository
				.findByAreaIdInAndCategoryId(areaIdList, request.getCategoryId(), pageable);
		} else {
			productDocumentList = productSearchRepository
				.findBytitleOrContentOrTransactionPlaceAndAreaIdAndCategoryId
					(request.getSearchWord(), request.getSearchWord(), request.getSearchWord(),
						request.getAreaId(), request.getCategoryId(), pageable);
		}

		return productDocumentList;
	}

	@Transactional
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

	@Transactional
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

	@Transactional
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

	@Transactional
	public void saveAllProductDocuments() {
		List<ProductDocument> memberDocumentList
			= productRepository.findAll().stream().map(ProductDocument::from).collect(Collectors.toList());
		productSearchRepository.saveAll(memberDocumentList);
	}



	private void readProductValidation(ReadProductListDto.Request request) {
		if(request.getCategoryId() == null || request.getAreaId() == null){
			throw new CustomException(READ_PRODUCT_INFO_NULL);
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