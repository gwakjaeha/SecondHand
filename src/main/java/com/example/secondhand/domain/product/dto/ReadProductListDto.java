package com.example.secondhand.domain.product.dto;

import com.example.secondhand.domain.product.entity.Product;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.bind.DefaultValue;

public class ReadProductListDto {

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Request {
		@NotNull
		private Long areaId;

		@NotNull
		private Long categoryId;

		private String searchWord;

		private int page = 0;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@Builder
	public static class Response {
		@NotNull
		private Long productId;
		@NotNull
		private Long userId;
		@NotNull
		private Long areaId;
		@NotNull
		private Long categoryId;
		@NotNull
		private String title;
		@NotNull
		private String content;
		private String image_path;
		@NotNull
		private Long price;
		@NotNull
		private String transactionPlace;

		public static ReadProductListDto.Response response(@NotNull Product product){
			return Response.builder()
				.productId(product.getProductId())
				.userId(product.getUserId())
				.areaId(product.getAreaId())
				.categoryId(product.getCategoryId())
				.title(product.getTitle())
				.content(product.getContent())
				.image_path(product.getImagePath())
				.price(product.getPrice())
				.transactionPlace(product.getTransactionPlace())
				.build();

		}
	}
}
