package com.example.secondhand.domain.user.dto;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ChangePasswordDto {

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@Builder
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
	@Builder
	public static class LostRequest {
		@NotNull
		private String email;
		@NotNull
		private String newPassword;
	}
}
