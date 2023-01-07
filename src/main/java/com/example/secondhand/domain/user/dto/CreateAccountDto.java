package com.example.secondhand.domain.user.dto;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

public class CreateAccountDto {

	@Getter
	@Setter
	@AllArgsConstructor
	public static class Request {
		@NotNull
		private Long areaId;

		@NotNull
		private String email;

		@NotNull
		private String password;

		@NotNull
		private String userName;

		@NotNull
		private String phone;

	}
}
