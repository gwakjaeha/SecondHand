package com.example.secondhand.domain.user.dto;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class LoginAccount {

	@Getter
	@Setter
	@AllArgsConstructor
	public static class Request {
		@NotNull
		private String email;
		@NotNull
		private String password;
	}
}
