package com.example.secondhand.domain.interest_product.service;

import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EXIST_PRODUCT;

import com.example.secondhand.domain.interest_product.entity.InterestProduct;
import com.example.secondhand.domain.interest_product.repository.InterestProductRepository;
import com.example.secondhand.domain.interest_product.dto.AddInterestProductDto;
import com.example.secondhand.domain.interest_product.dto.DeleteInterestProductDto;
import com.example.secondhand.domain.interest_product.dto.ReadInterestProductListDto;
import com.example.secondhand.domain.interest_product.dto.ReadPopularProductListDto;
import com.example.secondhand.domain.product.entity.Product;
import com.example.secondhand.domain.product.repository.ProductRepository;
import com.example.secondhand.domain.user.dto.TokenInfoResponseDto;
import com.example.secondhand.domain.user.service.UserService;
import com.example.secondhand.global.config.redis.RedisDao;
import com.example.secondhand.global.exception.CustomException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterestProductService {

	private final ProductRepository productRepository;
	private final InterestProductRepository interestProductRepository;
	private final UserService userService;
	private final RedisDao redisDao;
	@Value("${pageSize}")
	private int pageSize;
	@Value("${interestDegreePrefix}")
	private String INTEREST_DEGREE_PREFIX;
	@Value("${popularProductListPrefix}")
	private String POPULAR_PRODUCT_LIST_PREFIX;
	@Value("${popularProductCriterion}")
	private String POPULAR_PRODUCT_CRITERION;

	@Transactional
	public void addInterestProduct(AddInterestProductDto.Request request) {
		TokenInfoResponseDto tokenInfo = userService.getTokenInfo();

		Product product = productRepository.findById(request.getProductId())
			.orElseThrow(() -> new CustomException(NOT_EXIST_PRODUCT));

		interestProductRepository.save(
			InterestProduct.builder()
				.id(tokenInfo.getUserId())
				.product(product)
				.build());

		//redis에 각 상품의 관심도를 카운트하여 HashMap 형태로 저장.
		if(redisDao.getValuesForHash(INTEREST_DEGREE_PREFIX) != null){
			Map<String,String> interestDegreeMap = redisDao.getValuesForHash(INTEREST_DEGREE_PREFIX);
			Long interestDegree = 0L;
			if(interestDegreeMap.containsKey(request.getProductId().toString())){
				interestDegree = Long.parseLong(interestDegreeMap.get(request.getProductId().toString()));
				interestDegree++;
			} else {
				interestDegree = interestProductRepository.countByProductId(request.getProductId());
			}
			interestDegreeMap.put(request.getProductId().toString(), interestDegree.toString());
			redisDao.setValuesForHash(INTEREST_DEGREE_PREFIX, interestDegreeMap);
		} else {
			Long interestDegree = interestProductRepository.countByProductId(request.getProductId());
			Map<String,String> interestDegreeMap = new HashMap<String,String>();
			interestDegreeMap.put(request.getProductId().toString(), interestDegree.toString());
			redisDao.setValuesForHash(INTEREST_DEGREE_PREFIX, interestDegreeMap);
		}
	}

	@Transactional
	public Page<InterestProduct> readInterestProduct(ReadInterestProductListDto.Request request) {
		Pageable pageable = PageRequest.of(request.getPage(), pageSize);
		TokenInfoResponseDto tokenInfo = userService.getTokenInfo();
		return interestProductRepository.findByUserId(tokenInfo.getUserId(), pageable);
	}

	@Transactional
	public void deleteInterestProduct(DeleteInterestProductDto.Request request) {
		TokenInfoResponseDto tokenInfo = userService.getTokenInfo();
		interestProductRepository.deleteByIdAndUserId(request.getInterestProductId(), tokenInfo.getUserId());
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

	@Transactional
	public Page<Product> readPopularProductList(ReadPopularProductListDto.Request request) {
		Pageable pageable = PageRequest.of(request.getPage(), pageSize);
		Set<Long> set = redisDao.getValuesForSet(POPULAR_PRODUCT_LIST_PREFIX).stream().map(Long::parseLong).collect(
			Collectors.toSet());
		return productRepository.findByIdIsIn(set, pageable);
	}
}
