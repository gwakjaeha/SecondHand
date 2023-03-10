package com.example.secondhand.domain.product.service;

import static com.example.secondhand.global.constant.GlobalConstant.BASE_LOCAL_PATH;
import static com.example.secondhand.global.constant.GlobalConstant.BASE_URL_PATH;
import static com.example.secondhand.global.constant.GlobalConstant.INTEREST_DEGREE_PREFIX;
import static com.example.secondhand.global.constant.GlobalConstant.PAGE_SIZE;
import static com.example.secondhand.global.constant.GlobalConstant.POPULAR_PRODUCT_CRITERION;
import static com.example.secondhand.global.constant.GlobalConstant.POPULAR_PRODUCT_LIST_PREFIX;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EXIST_CATEGORY;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EXIST_PRODUCT;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_FOUND_CATEGORY;
import static com.example.secondhand.global.exception.CustomErrorCode.SAVE_IMAGE_FILE_FALSE;
import static com.example.secondhand.global.exception.CustomErrorCode.USER_NOT_MATCH;

import com.example.secondhand.domain.area.entity.Area;
import com.example.secondhand.domain.area.repository.AreaRepository;
import com.example.secondhand.domain.catetory.entity.Category;
import com.example.secondhand.domain.catetory.repository.CategoryRepository;
import com.example.secondhand.domain.product.dto.AddProductDto;
import com.example.secondhand.domain.product.dto.DeleteProductDto;
import com.example.secondhand.domain.product.dto.ReadMySellingProductListDto;
import com.example.secondhand.domain.product.dto.ReadProductListDto;
import com.example.secondhand.domain.product.dto.UpdateProductDto;
import com.example.secondhand.domain.product.entity.Product;
import com.example.secondhand.domain.product.entity.ProductDocument;
import com.example.secondhand.domain.product.repository.ProductRepository;
import com.example.secondhand.domain.product.repository.ProductSearchRepository;
import com.example.secondhand.domain.user.domain.User;
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

	//???????????? ????????? ??????, (??????, ????????????, ?????????)??? ????????? ????????????,
	//???????????? ???????????? ?????? ??????, (??????, ????????????)?????? ????????? ?????????.
	//????????? ?????? ??? ???????????? ?????? ???????????? ????????? ?????? ???????????? ?????? ????????? ???????????? ?????? ?????? ??????????????? ???????????? ???????????? ?????? ?????????.
	//????????? ???????????? ?????? ????????? ???????????? ????????? ??????????????? ???????????? ?????? ???????????? ????????? ???????????? ???.
	//?????? ?????? ????????? ???????????? ?????????, read ???????????? ????????? ???????????? ????????? ??????,
	//?????? ????????? ???????????? ?????? ????????? ????????? ?????? (??? ??????-?????? ??????) ?????? ???????????? ?????? ????????? ??????.
	@Transactional
	public Page<ProductDocument> readProductList(ReadProductListDto.Request request) {

		Pageable pageable = PageRequest.of(request.getPage(), PAGE_SIZE);
		Page<ProductDocument> productDocumentList = null;

		//JPA ????????? from ?????? ??????????????? ???????????? ????????? ?????????, ?????? ?????? ????????? ???????????? ??? ?????? ?????? ???????????? ?????????.
		Area area = areaRepository.findById(request.getAreaId()).get();
		//(sido, sigungu) ????????? index??? ????????????, ?????? ????????? ??????.
		List<Area> areaList = areaRepository.findBySidoAndSigungu(area.getSido(), area.getSigungu());
		List<Long> areaIdList = areaList.stream().map(Area::getId).collect(Collectors.toList());

		//Elasticsearch ?????? Nori ????????? ???????????? ???????????? ????????? ????????? ?????????.
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
	public void addProduct(AddProductDto.Request request, MultipartFile imgFile, String email) {

		User user = userService.getUser(email);

		String imgFilePath = getSavedImageFilePath(imgFile);

		Category category = categoryRepository.findById(request.getCategoryId())
			.orElseThrow(() -> new CustomException(NOT_FOUND_CATEGORY));

		Product product = Product.builder()
			.id(user.getId())
			.user(user)
			.area(user.getArea())
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
	public void updateProduct(
		UpdateProductDto.Request request, MultipartFile imgFile, String email) {
		Product product = productRepository.findById(request.getProductId())
			.orElseThrow(() -> new CustomException(NOT_EXIST_PRODUCT));

		User user = userService.getUser(email);

		if(product.getUser().getId() != user.getId()){
			throw new CustomException(USER_NOT_MATCH);
		}

		Category category = categoryRepository.findById(request.getCategoryId())
			.orElseThrow(() -> new CustomException(NOT_EXIST_CATEGORY));

		String imgFilePath = getSavedImageFilePath(imgFile);

		product.setCategory(category);
		product.setTitle(request.getTitle());
		product.setContent(request.getContent());
		product.setPrice(request.getPrice());
		product.setTransactionPlace(request.getTransactionPlace());
		product.setTransactionStatus(request.isTransactionStatus());
		product.setImagePath(imgFilePath);

		productRepository.save(product);
	}

	@Transactional
	public void deleteProduct(DeleteProductDto.Request request, String email) {
		Product product = productRepository.findById(request.getProductId()).get();

		User user = userService.getUser(email);

		if(product.getUser().getId() != user.getId()){
			throw new CustomException(USER_NOT_MATCH);
		}

		product.setDeletedAt(LocalDateTime.now());
		productRepository.save(product);
	}

	@Transactional
	public Page<Product> readMySellingProductList(
		ReadMySellingProductListDto.Request request, String email) {
		Pageable pageable = PageRequest.of(request.getPage(), PAGE_SIZE);
		User user = userService.getUser(email);
		return productRepository.findByUserIdAndDeletedAtIsNull(user.getId(), pageable);
	}


	//??????????????? ??????????????? ?????? ?????? ?????? ????????? redis set ????????? ?????? ???????????????.
	@Transactional
	@Scheduled(cron = "* 0/10 * * * *") //10??? ???????????? ??????
	public void savePopularProductList(){
		Map<String,String> interestDegreeMap = redisDao.getValuesForHash(INTEREST_DEGREE_PREFIX);
		redisDao.deleteValues(POPULAR_PRODUCT_LIST_PREFIX);
		for(String key: interestDegreeMap.keySet()){
			if(Long.parseLong(interestDegreeMap.get(key)) > POPULAR_PRODUCT_CRITERION){
				redisDao.setValuesForSet(POPULAR_PRODUCT_LIST_PREFIX, key);
			}
		}
	}

	private String getSavedImageFilePath(MultipartFile imgFile){

		String saveFilename = "";
		String urlFilename = "";

		if(imgFile != null) {
			String originalFilename = imgFile.getOriginalFilename();

			String[] arrFilename = getNewImgFilePath(BASE_LOCAL_PATH, BASE_URL_PATH, originalFilename);

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
