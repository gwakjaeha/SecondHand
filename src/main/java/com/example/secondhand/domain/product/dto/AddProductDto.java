package com.example.secondhand.domain.product.dto;

import com.example.secondhand.domain.product.domain.Product;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
