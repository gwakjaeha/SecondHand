package com.example.secondhand.domain.product.service;

import static com.example.secondhand.global.exception.CustomErrorCode.*;

import com.example.secondhand.domain.catetory.entity.Category;
import com.example.secondhand.domain.catetory.repository.CategoryRepository;
import com.example.secondhand.domain.product.dto.AddProductDto;
import com.example.secondhand.domain.product.dto.ReadMySellingProductListDto;
import com.example.secondhand.domain.area.entity.Area;
import com.example.secondhand.domain.product.entity.Product;
import com.example.secondhand.domain.product.dto.DeleteProductDto;
import com.example.secondhand.domain.product.dto.ReadProductListDto;
import com.example.secondhand.domain.product.dto.UpdateProductDto;
import com.example.secondhand.domain.product.entity.ProductDocument;
import com.example.secondhand.domain.area.repository.AreaRepository;
import com.example.secondhand.domain.product.repository.ProductRepository;
import com.example.secondhand.domain.product.repository.ProductSearchRepository;
import com.example.secondhand.domain.user.dto.TokenInfoResponseDto;
import com.example.secondhand.domain.user.service.UserService;
import com.example.secondhand.global.config.redis.RedisDao;
import com.example.secondhand.global.exception.CustomException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductSearchRepository productSearchRepository;
	private final AreaRepository areaRepository;
	private final CategoryRepository categoryRepository;
	private final UserService userService;
	private final RedisDao redisDao;
	@Value("${baseLocalPath}")
	private String baseLocalPath;
	@Value("${baseUrlPath}")
	private String baseUrlPath;
	@Value("${pageSize}")
	private int pageSize;
	@Value("${interestDegreePrefix}")
	private String INTEREST_DEGREE_PREFIX;
	@Value("${popularProductListPrefix}")
	private String POPULAR_PRODUCT_LIST_PREFIX;
	@Value("${popularProductCriterion}")
	private String POPULAR_PRODUCT_CRITERION;

	//키워드가 입력된 경우, (지역, 카테고리, 키워드)로 검색을 진행하고,
	//키워드가 입력되지 않은 경우, (지역, 카테고리)로만 검색을 진행함.
	//검색을 하면 내 읍면동과 같은 시군구에 속하는 모든 읍면동을 근처 동네로 설정하여 해당 근처 동네들에서 판매되는 상품들을 모두 가져옴.
	//거리를 기준으로 근처 동네를 계산하는 방식은 읍면동간의 거리들을 모두 계산해야 하므로 연산량이 큼.
	//추후 만약 거리를 고려해야 한다면, read 할때마다 거리를 계산하는 방식이 아닌,
	//미리 거리를 계산하여 근처 동네를 구성해 높은 (내 동네-근처 동네) 매핑 테이블을 만들 필요가 있음.
	@Transactional
	public Page<ProductDocument> readProductList(ReadProductListDto.Request request) {

		Pageable pageable = PageRequest.of(request.getPage(), pageSize);
		Page<ProductDocument> productDocumentList = null;

		//JPA 에서는 from 절의 서브쿼리를 구현하기 어려운 관계로, 여러 쿼리 단계로 나누어서 내 근처 지역 리스트를 가져옴.
		Area area = areaRepository.findById(request.getAreaId()).get();
		//(sido, sigungu) 칼럼을 index로 설정하여, 조회 속도가 빠름.
		List<Area> areaList = areaRepository.findBySidoAndSigungu(area.getSido(), area.getSigungu());
		List<Long> areaIdList = areaList.stream().map(Area::getId).collect(Collectors.toList());

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
	public void addProduct(AddProductDto.Request request, MultipartFile imgFile) {
		TokenInfoResponseDto tokenInfo = userService.getTokenInfo();
		String imgFilePath = getSavedImageFilePath(imgFile);

		Area area = areaRepository.findById(tokenInfo.getAreaId())
			.orElseThrow(() -> new CustomException(NOT_FOUND_AREA));

		Category category = categoryRepository.findById(request.getCategoryId())
			.orElseThrow(() -> new CustomException(NOT_FOUND_CATEGORY));

		Product product = Product.builder()
			.id(tokenInfo.getUserId())
			.area(area)
			.category(category)
			.title(request.getTitle())
			.content(request.getContent())
			.imagePath(imgFilePath)
			.price(request.getPrice())
			.transactionPlace(request.getTransactionPlace())
			.transactionStatus(true)
			.build();

		productRepository.save(product);

		ProductDocument productDocument = ProductDocument.from(product);
		productSearchRepository.save(productDocument);
	}

	@Transactional
	public void updateProduct(UpdateProductDto.Request request, MultipartFile imgFile) {
		Product product = productRepository.findById(request.getProductId())
			.orElseThrow(() -> new CustomException(NOT_EXIST_PRODUCT));

		String imgFilePath = getSavedImageFilePath(imgFile);
		productRepository.save(
			Product.builder()
				.id(request.getProductId())
				.user(product.getUser())
				.area(product.getArea())
				.category(product.getCategory())
				.title(request.getTitle())
				.content(request.getContent())
				.imagePath(imgFilePath)
				.price(request.getPrice())
				.transactionPlace(request.getTransactionPlace())
				.transactionStatus(request.isTransactionStatus())
				.createdAt(product.getCreatedAt())
				.build());
	}

	@Transactional
	public void deleteProduct(DeleteProductDto.Request request) {
		Product product = productRepository.findById(request.getProductId()).get();
		product.setDeleteAt(LocalDateTime.now());
		productRepository.save(product);
	}

	@Transactional
	public Page<Product> readMySellingProductList(
		ReadMySellingProductListDto.Request request) {
		Pageable pageable = PageRequest.of(request.getPage(), pageSize);
		TokenInfoResponseDto tokenInfo = userService.getTokenInfo();
		return productRepository.findByUserIdAndDeleteAtIsNull(tokenInfo.getUserId(), pageable);
	}


	//주기적으로 관심정도가 높은 인기 상품 목록을 redis set 형태로 따로 저장해놓음.
	@Transactional
	@Scheduled(cron = "* 0/10 * * * *") //10분 간격으로 실행
	public void savePopularProductList(){
		Map<String,String> interestDegreeMap = redisDao.getValuesForHash(INTEREST_DEGREE_PREFIX);
		redisDao.deleteValues(POPULAR_PRODUCT_LIST_PREFIX);
		for(String key: interestDegreeMap.keySet()){
			if(Long.parseLong(interestDegreeMap.get(key)) > Long.parseLong(POPULAR_PRODUCT_CRITERION)){
				redisDao.setValuesForSet(POPULAR_PRODUCT_LIST_PREFIX, key);
			}
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
