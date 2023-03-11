package com.example.secondhand.domain.product.service;

import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EXIST_CATEGORY;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EXIST_PRODUCT;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_FOUND_CATEGORY;
import static com.example.secondhand.global.exception.CustomErrorCode.USER_NOT_MATCH;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@InjectMocks
	private ProductService productService;
	@Mock
	private AreaRepository areaRepository;
	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private ProductRepository productRepository;
	@Mock
	private ProductSearchRepository productSearchRepository;
	@Mock
	private UserService userService;
	@Mock
	private RedisDao redisDao;

	@Test
	void testReadProductListInCaseSearchWordNull() throws Exception{
		//given
		List<ProductDocument> productDocumentList = new ArrayList<>(Arrays.asList(ProductDocument.builder()
			.productId(1L)
			.title("title")
			.content("content")
			.areaId(2L)
			.categoryId(3L)
			.price(3000L)
			.build()));

		Pageable pageable = PageRequest.of(0, 10);

		Page<ProductDocument> productDocumentPage = new PageImpl<>(productDocumentList, pageable, 1);

		given(areaRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Area.builder().build()));
		given(areaRepository.findBySidoAndSigungu(any(), any()))
			.willReturn(new ArrayList<>(Arrays.asList(Area.builder().id(2L).build())));
		given(productSearchRepository.findByAreaIdInAndCategoryId(any(), anyLong(), any()))
			.willReturn(productDocumentPage);
		//when
		Page<ProductDocument> response = productService.readProductList(
			ReadProductListDto.Request.builder()
				.page(0)
				.areaId(2L)
				.categoryId(3L)
				.build()
		);
		//then
		assertEquals("title", response.getContent().get(0).getTitle());
		assertEquals("content", response.getContent().get(0).getContent());
		assertEquals(2L, response.getContent().get(0).getAreaId());
		assertEquals(3L, response.getContent().get(0).getCategoryId());
		assertEquals(3000L, response.getContent().get(0).getPrice());
	}

	@Test
	void testReadProductListInCaseSearchWordNotNull() throws Exception{
		//given
		List<ProductDocument> productDocumentList = new ArrayList<>(Arrays.asList(ProductDocument.builder()
			.productId(1L)
			.title("title")
			.content("content")
			.areaId(2L)
			.categoryId(3L)
			.price(3000L)
			.build()));

		Pageable pageable = PageRequest.of(0, 10);

		Page<ProductDocument> productDocumentPage = new PageImpl<>(productDocumentList, pageable, 1);

		given(areaRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Area.builder().build()));
		given(areaRepository.findBySidoAndSigungu(any(), any()))
			.willReturn(new ArrayList<>(Arrays.asList(Area.builder().id(2L).build())));
		given(productSearchRepository.findBytitleOrContentOrTransactionPlaceAndAreaIdAndCategoryId(
			anyString(), anyString(), anyString(), anyLong(), anyLong(), any()))
			.willReturn(productDocumentPage);
		//when
		Page<ProductDocument> response = productService.readProductList(
			ReadProductListDto.Request.builder()
				.page(0)
				.areaId(2L)
				.categoryId(3L)
				.searchWord("searchWord")
				.build()
		);
		//then
		assertEquals("title", response.getContent().get(0).getTitle());
		assertEquals("content", response.getContent().get(0).getContent());
		assertEquals(2L, response.getContent().get(0).getAreaId());
		assertEquals(3L, response.getContent().get(0).getCategoryId());
		assertEquals(3000L, response.getContent().get(0).getPrice());
	}

	@Test
	void testAddProduct() throws Exception{
		//given
		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(1L)
				.area(Area.builder().id(2L).build())
				.build());
		given(categoryRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Category.builder().id(3L).build()));
		given(productRepository.save(any()))
			.willReturn(Product.builder()
				.area(Area.builder().id(2L).build())
				.category(Category.builder().id(3L).build())
				.title("title")
				.content("content")
				.price(1000L)
				.transactionPlace("transactionPlace")
				.build()
			);
		given(productSearchRepository.save(any()))
			.willReturn(ProductDocument.builder()
				.areaId(2L)
				.categoryId(3L)
				.title("title")
				.content("content")
				.price(1000L)
				.transactionPlace("transactionPlace")
				.build());

		ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);
		ArgumentCaptor<ProductDocument> productDocumentArgumentCaptor = ArgumentCaptor.forClass(ProductDocument.class);

		//when
		MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

		productService.addProduct(AddProductDto.Request.builder()
			.categoryId(3L)
			.title("title")
			.content("content")
			.price(1000L)
			.transactionPlace("transactionPlace")
			.build(), multipartFile, "example@email.com");

		//then
		verify(userService, times(1)).getUser(any());
		verify(categoryRepository, times(1)).findById(any());
		verify(productRepository, times(1)).save(productArgumentCaptor.capture());
		verify(productSearchRepository, times(1)).save(productDocumentArgumentCaptor.capture());
		assertEquals(2L, productArgumentCaptor.getValue().getArea().getId());
		assertEquals(3L, productArgumentCaptor.getValue().getCategory().getId());
		assertEquals("title", productArgumentCaptor.getValue().getTitle());
		assertEquals("content", productArgumentCaptor.getValue().getContent());
		assertEquals(1000L, productArgumentCaptor.getValue().getPrice());
		assertEquals("transactionPlace", productArgumentCaptor.getValue().getTransactionPlace());
		assertEquals(2L, productDocumentArgumentCaptor.getValue().getAreaId());
		assertEquals(3L, productDocumentArgumentCaptor.getValue().getCategoryId());
		assertEquals("title", productDocumentArgumentCaptor.getValue().getTitle());
		assertEquals("content", productDocumentArgumentCaptor.getValue().getContent());
		assertEquals(1000L, productDocumentArgumentCaptor.getValue().getPrice());
		assertEquals("transactionPlace", productDocumentArgumentCaptor.getValue().getTransactionPlace());
	}

	@Test
	void testNotFoundCategoryInAddProduct() throws Exception{
		//given
		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(1L)
				.area(Area.builder().id(2L).build())
				.build());

		given(categoryRepository.findById(anyLong())).willReturn(Optional.empty());

		//when
		MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

		CustomException exception = assertThrows(CustomException.class,
			() -> productService.addProduct(AddProductDto.Request.builder()
				.categoryId(3L)
				.title("title")
				.content("content")
				.price(1000L)
				.transactionPlace("transactionPlace")
				.build(), multipartFile, "example@email.com"));

		//then
		assertEquals(NOT_FOUND_CATEGORY, exception.getCustomErrorCode());
	}

	@Test
	void testUpdateProduct() throws Exception{
		//given
		given(productRepository.findById(anyLong()))
			.willReturn(
				Optional.ofNullable(Product.builder()
					.id(4L)
					.user(User.builder().id(1L).build())
					.area(Area.builder().id(2L).build())
					.category(Category.builder().id(3L).build())
					.title("title")
					.content("content")
					.price(1000L)
					.transactionPlace("transactionPlace")
					.transactionStatus(true)
					.build()));
		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(1L)
				.area(Area.builder().id(2L).build())
				.build());
		given(categoryRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Category.builder()
				.id(3L)
				.build()));
		given(productRepository.save(any()))
			.willReturn(
				Product.builder()
					.title("title")
					.content("content")
					.price(1000L)
					.transactionPlace("transactionPlace")
					.transactionStatus(true)
					.build());

		ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);

		//when
		MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

		productService.updateProduct(UpdateProductDto.Request.builder()
			.productId(4L)
			.categoryId(3L)
			.title("title")
			.content("content")
			.price(1000L)
			.transactionPlace("transactionPlace")
			.transactionStatus(true)
			.build(), multipartFile, "example@email.com");

		//then
		verify(productRepository, times(1)).findById(any());
		verify(userService, times(1)).getUser(any());
		verify(categoryRepository, times(1)).findById(any());
		verify(productRepository, times(1)).save(productArgumentCaptor.capture());
		assertEquals(4L, productArgumentCaptor.getValue().getId());
		assertEquals(2L, productArgumentCaptor.getValue().getArea().getId());
		assertEquals(3L, productArgumentCaptor.getValue().getCategory().getId());
		assertEquals("title", productArgumentCaptor.getValue().getTitle());
		assertEquals("content", productArgumentCaptor.getValue().getContent());
		assertEquals(1000L, productArgumentCaptor.getValue().getPrice());
		assertEquals("transactionPlace", productArgumentCaptor.getValue().getTransactionPlace());
		assertEquals(true, productArgumentCaptor.getValue().isTransactionStatus());
	}

	@Test
	void testNotExistProductInUpdateProduct() throws Exception{
		//given
		given(productRepository.findById(anyLong())).willReturn(Optional.empty());

		//when
		MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

		CustomException exception = assertThrows(CustomException.class,
			() -> productService.updateProduct(UpdateProductDto.Request.builder()
				.productId(4L)
				.categoryId(3L)
				.title("title")
				.content("content")
				.price(1000L)
				.transactionPlace("transactionPlace")
				.transactionStatus(true)
				.build(), multipartFile, "example@email.com"));

		//then
		assertEquals(NOT_EXIST_PRODUCT, exception.getCustomErrorCode());
	}

	@Test
	void testUserNotMatchInUpdateProduct() throws Exception{
		//given
		given(productRepository.findById(anyLong()))
			.willReturn(
				Optional.ofNullable(Product.builder()
					.id(4L)
					.user(User.builder().id(1L).build())
					.area(Area.builder().id(2L).build())
					.category(Category.builder().id(3L).build())
					.title("title")
					.content("content")
					.price(1000L)
					.transactionPlace("transactionPlace")
					.transactionStatus(true)
					.build()));

		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(2L)
				.area(Area.builder().id(2L).build())
				.build());

		//when
		MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

		CustomException exception = assertThrows(CustomException.class,
			() -> productService.updateProduct(UpdateProductDto.Request.builder()
				.productId(4L)
				.categoryId(3L)
				.title("title")
				.content("content")
				.price(1000L)
				.transactionPlace("transactionPlace")
				.transactionStatus(true)
				.build(), multipartFile, "example@email.com"));

		//then
		assertEquals(USER_NOT_MATCH, exception.getCustomErrorCode());
	}

	@Test
	void testNotExistCategoryInUpdateProduct() throws Exception{
		//given
		given(productRepository.findById(anyLong()))
			.willReturn(
				Optional.ofNullable(Product.builder()
					.id(4L)
					.user(User.builder().id(1L).build())
					.area(Area.builder().id(2L).build())
					.category(Category.builder().id(3L).build())
					.title("title")
					.content("content")
					.price(1000L)
					.transactionPlace("transactionPlace")
					.transactionStatus(true)
					.build()));

		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(1L)
				.area(Area.builder().id(2L).build())
				.build());

		given(categoryRepository.findById(anyLong())).willReturn(Optional.empty());

		//when
		MultipartFile multipartFile = new MockMultipartFile("file", "fileContent".getBytes());

		CustomException exception = assertThrows(CustomException.class,
			() -> productService.updateProduct(UpdateProductDto.Request.builder()
				.productId(4L)
				.categoryId(3L)
				.title("title")
				.content("content")
				.price(1000L)
				.transactionPlace("transactionPlace")
				.transactionStatus(true)
				.build(), multipartFile, "example@email.com"));

		//then
		assertEquals(NOT_EXIST_CATEGORY, exception.getCustomErrorCode());
	}

	@Test
	void testDeleteProduct() throws Exception{
		//given
		given(productRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Product.builder()
				.id(3L)
				.user(User.builder().id(1L).build())
				.build()));
		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(1L)
				.area(Area.builder().id(2L).build())
				.build());
		given(productRepository.save(any()))
			.willReturn(
				Product.builder()
					.id(3L)
					.build());

		ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);

		//when
		productService.deleteProduct(DeleteProductDto.Request.builder()
			.productId(3L)
			.build(), "example@email.com");

		//then
		verify(productRepository, times(1)).findById(any());
		verify(userService, times(1)).getUser(any());
		verify(productRepository, times(1)).save(productArgumentCaptor.capture());
		assertEquals(3L, productArgumentCaptor.getValue().getId());
	}

	@Test
	void testUserNotMatchInDeleteProduct() throws Exception{
		//given
		given(productRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Product.builder()
				.id(3L)
				.user(User.builder().id(1L).build())
				.build()));
		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(4L)
				.area(Area.builder().id(2L).build())
				.build());

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> productService.deleteProduct(DeleteProductDto.Request.builder()
				.productId(3L)
				.build(), "example@email.com"));

		//then
		assertEquals(USER_NOT_MATCH, exception.getCustomErrorCode());
	}

	@Test
	void testReadMySellingProductList() throws Exception{
		//given
		Pageable pageable = PageRequest.of(0, 10);

		List<Product> productList = new ArrayList<>();
		productList.add(Product.builder()
			.id(3L)
			.title("title")
			.content("content")
			.price(1000L)
			.transactionPlace("transactionPlace")
			.transactionStatus(true)
			.build());

		Page<Product> productDocumentPage = new PageImpl<>(productList, pageable, 1);

		given(userService.getUser("example@email.com"))
			.willReturn(User.builder()
				.id(1L)
				.area(Area.builder().id(2L).build())
				.build());
		given(productRepository.findByUserIdAndDeletedAtIsNull(anyLong(), any()))
			.willReturn(productDocumentPage);

		//when
		Page<Product> response = productService.readMySellingProductList(
			ReadMySellingProductListDto.Request.builder()
				.build(), "example@email.com");
		//then
		assertEquals("title", response.getContent().get(0).getTitle());
		assertEquals("content", response.getContent().get(0).getContent());
		assertEquals(1000L, response.getContent().get(0).getPrice());
		assertEquals("transactionPlace", response.getContent().get(0).getTransactionPlace());
		assertEquals(true, response.getContent().get(0).isTransactionStatus());
	}

	@Test
	void testSavePopularProductList() throws Exception{
		//given
		Map<String, String> interestDegreeMap = new HashMap<>();
		interestDegreeMap.put("A","5");
		interestDegreeMap.put("B","10");
		given(redisDao.getValuesForHash(anyString()))
			.willReturn(interestDegreeMap);
		willDoNothing().given(redisDao).deleteValues(anyString());
		willDoNothing().given(redisDao).setValuesForSet(anyString(), anyString());

		//when
		productService.savePopularProductList();

		//then
		verify(redisDao, times(1)).getValuesForHash(anyString());
		verify(redisDao, times(1)).deleteValues(anyString());
		verify(redisDao, times(2)).setValuesForSet(anyString(), anyString());
	}
}