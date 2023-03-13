package com.example.secondhand.domain.interest_product.service;

import static com.example.secondhand.global.constant.GlobalConstant.INTEREST_DEGREE_PREFIX;
import static com.example.secondhand.global.constant.GlobalConstant.PAGE_SIZE;
import static com.example.secondhand.global.constant.GlobalConstant.POPULAR_PRODUCT_CRITERION;
import static com.example.secondhand.global.constant.GlobalConstant.POPULAR_PRODUCT_LIST_PREFIX;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EXIST_PRODUCT;

import com.example.secondhand.domain.interest_product.dto.AddInterestProductDto;
import com.example.secondhand.domain.interest_product.dto.DeleteInterestProductDto;
import com.example.secondhand.domain.interest_product.dto.ReadInterestProductListDto;
import com.example.secondhand.domain.interest_product.dto.ReadPopularProductListDto;
import com.example.secondhand.domain.interest_product.entity.InterestProduct;
import com.example.secondhand.domain.interest_product.repository.InterestProductRepository;
import com.example.secondhand.domain.product.entity.Product;
import com.example.secondhand.domain.product.repository.ProductRepository;
import com.example.secondhand.domain.user.domain.User;
import com.example.secondhand.domain.user.service.UserService;
import com.example.secondhand.global.config.redis.RedisDao;
import com.example.secondhand.global.exception.CustomException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

	@Transactional
	public void addInterestProduct(AddInterestProductDto.Request request, String email) {
		User user = userService.getUser(email);

		Product product = productRepository.findById(request.getProductId())
			.orElseThrow(() -> new CustomException(NOT_EXIST_PRODUCT));

		interestProductRepository.save(
			InterestProduct.builder()
				.id(user.getId())
				.product(product)
				.build());

		//redis에 각 상품의 관심도를 카운트하여 HashMap 형태로 저장.
		Map<String,String> interestDegreeMap = redisDao.getValuesForHash(INTEREST_DEGREE_PREFIX);
		if(interestDegreeMap != null){
			//기존에 해당 상품의 관심도가 저장된 hashmap 자료구조가 redis 에 저장되어 있는 경우.
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
			//기존에 해당 상품의 관심도가 저장된 hashmap 자료구조가 redis 에 저장되어 있지 않은 경우.
			Long interestDegree = interestProductRepository.countByProductId(request.getProductId());
			interestDegreeMap = new HashMap<String,String>();
			interestDegreeMap.put(request.getProductId().toString(), interestDegree.toString());
			redisDao.setValuesForHash(INTEREST_DEGREE_PREFIX, interestDegreeMap);
		}
	}

	@Transactional
	public Page<InterestProduct> readInterestProduct(
		ReadInterestProductListDto.Request request, String email) {

		Pageable pageable = PageRequest.of(request.getPage(), PAGE_SIZE);

		User user = userService.getUser(email);

		return interestProductRepository.findByUserId(user.getId(), pageable);
	}

	@Transactional
	public void deleteInterestProduct(
		DeleteInterestProductDto.Request request, String email) {
		User user = userService.getUser(email);
		interestProductRepository.deleteByIdAndUserId(
			request.getInterestProductId(), user.getId());
	}

	//주기적으로 관심정도가 높은 인기 상품 목록을 redis set 형태로 따로 저장해놓음.
	@Transactional
	@Scheduled(cron = "* 0/10 * * * *") //10분 간격으로 실행
	public void savePopularProductList(){
		Map<String,String> interestDegreeMap = redisDao.getValuesForHash(INTEREST_DEGREE_PREFIX);
		redisDao.deleteValues(POPULAR_PRODUCT_LIST_PREFIX);
		for(String key: interestDegreeMap.keySet()){
			if(Long.parseLong(interestDegreeMap.get(key)) > POPULAR_PRODUCT_CRITERION){
				redisDao.setValuesForSet(POPULAR_PRODUCT_LIST_PREFIX, key);
			}
		}
	}

	@Transactional
	public Page<Product> readPopularProductList(ReadPopularProductListDto.Request request) {
		Pageable pageable = PageRequest.of(request.getPage(), PAGE_SIZE);
		Set<Long> set = redisDao.getValuesForSet(POPULAR_PRODUCT_LIST_PREFIX).stream().map(Long::parseLong).collect(
			Collectors.toSet());
		return productRepository.findByIdIsIn(set, pageable);
	}
}
