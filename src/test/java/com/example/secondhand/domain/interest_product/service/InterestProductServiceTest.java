package com.example.secondhand.domain.interest_product.service;

import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EXIST_PRODUCT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.secondhand.domain.area.entity.Area;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class InterestProductServiceTest {
	@InjectMocks
	private InterestProductService interestProductService;
	@Mock
	private ProductRepository productRepository;
	@Mock
	private UserService userService;
	@Mock
	private InterestProductRepository interestProductRepository;
	@Mock
	private RedisDao redisDao;

	@Test
	void testAddInterestProductInCaseInterestDegreeExistAndContainKey() throws Exception{
		//given
		Map<String, String> interestDegreeMap = new HashMap<>();
		interestDegreeMap.put("3","10");

		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(4L)
				.area(Area.builder().id(2L).build())
				.build());
		given(productRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Product.builder()
				.id(3L)
				.user(User.builder().id(1L).build())
				.build()));
		given(interestProductRepository.save(any()))
			.willReturn(InterestProduct.builder()
				.id(1L)
				.product(Product.builder().build())
				.build());
		given(redisDao.getValuesForHash(anyString()))
			.willReturn(interestDegreeMap);
		willDoNothing().given(redisDao).setValuesForHash(anyString(),any());

		//when
		interestProductService.addInterestProduct(AddInterestProductDto.Request.builder()
			.productId(3L)
			.build(), "example@email.com");

		//then
		verify(productRepository, times(1)).findById(anyLong());
		verify(userService, times(1)).getUser(anyString());
		verify(interestProductRepository, times(1)).save(any());
		verify(redisDao, times(1)).getValuesForHash(anyString());
		verify(interestProductRepository, times(0)).countByProductId(anyLong());
		verify(redisDao, times(1)).setValuesForHash(anyString(),any());
	}

	@Test
	void testAddInterestProductInCaseInterestDegreeExistAndNotContainKey() throws Exception{
		//given
		Map<String, String> interestDegreeMap = new HashMap<>();
		interestDegreeMap.put("1","10");

		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(4L)
				.area(Area.builder().id(2L).build())
				.build());
		given(productRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Product.builder()
				.id(3L)
				.user(User.builder().id(1L).build())
				.build()));
		given(interestProductRepository.save(any()))
			.willReturn(InterestProduct.builder()
				.id(1L)
				.product(Product.builder().build())
				.build());
		given(redisDao.getValuesForHash(anyString()))
			.willReturn(interestDegreeMap);
		given(interestProductRepository.countByProductId(anyLong()))
			.willReturn(20L);
		willDoNothing().given(redisDao).setValuesForHash(anyString(),any());

		//when
		interestProductService.addInterestProduct(AddInterestProductDto.Request.builder()
			.productId(3L)
			.build(), "example@email.com");

		//then
		verify(productRepository, times(1)).findById(anyLong());
		verify(userService, times(1)).getUser(anyString());
		verify(interestProductRepository, times(1)).save(any());
		verify(redisDao, times(1)).getValuesForHash(anyString());
		verify(interestProductRepository, times(1)).countByProductId(anyLong());
		verify(redisDao, times(1)).setValuesForHash(anyString(),any());
	}

	@Test
	void testAddInterestProductInCaseInterestDegreeNotExist() throws Exception{
		//given
		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(4L)
				.area(Area.builder().id(2L).build())
				.build());
		given(productRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Product.builder()
				.id(3L)
				.user(User.builder().id(1L).build())
				.build()));
		given(interestProductRepository.save(any()))
			.willReturn(InterestProduct.builder()
				.id(1L)
				.product(Product.builder().build())
				.build());
		given(redisDao.getValuesForHash(anyString()))
			.willReturn(null);
		given(interestProductRepository.countByProductId(anyLong()))
			.willReturn(20L);
		willDoNothing().given(redisDao).setValuesForHash(anyString(),any());

		//when
		interestProductService.addInterestProduct(AddInterestProductDto.Request.builder()
			.productId(3L)
			.build(), "example@email.com");

		//then
		verify(productRepository, times(1)).findById(anyLong());
		verify(userService, times(1)).getUser(anyString());
		verify(interestProductRepository, times(1)).save(any());
		verify(redisDao, times(1)).getValuesForHash(anyString());
		verify(interestProductRepository, times(1)).countByProductId(anyLong());
		verify(redisDao, times(1)).setValuesForHash(anyString(),any());
	}

	@Test
	void testNotExistProductInAddInterestProduct() throws Exception{
		//given
		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(4L)
				.area(Area.builder().id(2L).build())
				.build());
		given(productRepository.findById(anyLong()))
			.willReturn(Optional.empty());

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> interestProductService.addInterestProduct(AddInterestProductDto.Request.builder()
				.productId(3L)
				.build(), "example@email.com"));

		//then
		assertEquals(NOT_EXIST_PRODUCT, exception.getCustomErrorCode());
	}

	@Test
	void testReadInterestProduct() throws Exception{
		//given
		List<InterestProduct> interestProductList = new ArrayList<>(Arrays.asList(InterestProduct.builder()
			.id(1L)
			.build()));

		Pageable pageable = PageRequest.of(0, 10);

		Page<InterestProduct> interestProductPage = new PageImpl<>(interestProductList, pageable, 1);

		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(4L)
				.area(Area.builder().id(2L).build())
				.build());
		given(interestProductRepository.findByUserId(anyLong(),any()))
			.willReturn(interestProductPage);

		//when
		Page<InterestProduct> response = interestProductService.readInterestProduct(ReadInterestProductListDto.Request.builder()
			.page(0)
			.build(), "example@email.com");

		//then
		assertEquals(1L, response.getContent().get(0).getId());
	}

	@Test
	void deleteInterestProduct() throws Exception{
		//given
		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(4L)
				.area(Area.builder().id(2L).build())
				.build());
		willDoNothing().given(interestProductRepository).deleteByIdAndUserId(anyLong(),anyLong());

		//when
		interestProductService.deleteInterestProduct(DeleteInterestProductDto.Request.builder()
			.InterestProductId(1L)
			.build(), "example@email.com");

		//then
		verify(userService, times(1)).getUser(anyString());
		verify(interestProductRepository, times(1)).deleteByIdAndUserId(anyLong(),anyLong());
	}

	@Test
	void savePopularProductList() throws Exception{
		//given
		Map<String,String> interestDegreeMap = new HashMap<>();
		interestDegreeMap.put("4","10");
		interestDegreeMap.put("5","1");
		interestDegreeMap.put("6","20");

		given(redisDao.getValuesForHash(anyString()))
			.willReturn(interestDegreeMap);
		willDoNothing().given(redisDao).deleteValues(anyString());
		willDoNothing().given(redisDao).setValuesForSet(anyString(), anyString());

		//when
		interestProductService.savePopularProductList();

		//then
		verify(redisDao,times(1)).getValuesForHash(anyString());
		verify(redisDao,times(1)).deleteValues(anyString());
		verify(redisDao,times(2)).setValuesForSet(anyString(),anyString());
	}

	@Test
	void readPopularProductList() throws Exception{
		//given
		Set<String> popularProductSet = new HashSet<>();
		popularProductSet.add("1");
		popularProductSet.add("2");

		List<Product> interestProductList = new ArrayList<>(Arrays.asList(
			Product.builder()
				.id(1L)
				.build(),
			Product.builder()
				.id(2L)
				.build()
			));

		Pageable pageable = PageRequest.of(0, 10);

		Page<Product> interestProductPage = new PageImpl<>(interestProductList, pageable, 2);

		given(redisDao.getValuesForSet(anyString()))
			.willReturn(popularProductSet);
		given(productRepository.findByIdIsIn(any(),any()))
			.willReturn(interestProductPage);

		//when
		Page<Product> response = interestProductService.readPopularProductList(
			ReadPopularProductListDto.Request
				.builder()
				.page(0)
				.build()
		);

		//then
		assertEquals(1L, response.getContent().get(0).getId());
		assertEquals(2L, response.getContent().get(1).getId());
	}
}