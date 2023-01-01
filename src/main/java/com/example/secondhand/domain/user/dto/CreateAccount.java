package com.example.secondhand.domain.user.dto;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class CreateAccount {

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
