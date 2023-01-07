package com.example.secondhand.domain.user.dto;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class ChangePasswordDto {

	@Getter
	@Setter
	@AllArgsConstructor
	public static class Request {
		@NotNull
		private String email;
		@NotNull
		private String password;
		@NotNull
		private String newPassword;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public static class lostRequest {
		@NotNull
		private String email;
		@NotNull
		private String newPassword;
	}
}
