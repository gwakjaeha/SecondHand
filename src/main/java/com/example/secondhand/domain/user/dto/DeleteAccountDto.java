package com.example.secondhand.domain.user.dto;

import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DeleteAccountDto {

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@Builder
	public static class Request {
		@NotEmpty
		private String password;
	}
}
