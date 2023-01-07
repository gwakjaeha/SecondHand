package com.example.secondhand.domain.user.dto;

import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class LoginAccountDto {

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Request {
		@NotEmpty
		private String email;
		@NotEmpty
		private String password;
	}
}
