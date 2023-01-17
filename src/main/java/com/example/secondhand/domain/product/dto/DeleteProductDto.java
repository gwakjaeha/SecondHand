package com.example.secondhand.domain.product.dto;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class DeleteProductDto {

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Request {
		@NotNull
		private Long productId;
	}
}
