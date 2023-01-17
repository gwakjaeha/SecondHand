package com.example.secondhand.domain.product.dto;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AddProductDto {

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Request {

		@NotNull
		private Long categoryId;
		@NotNull
		private String title;
		@NotNull
		private String content;
		@NotNull
		private Long price;
		@NotNull
		private String transactionPlace;
	}
}
