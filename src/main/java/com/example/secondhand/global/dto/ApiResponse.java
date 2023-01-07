package com.example.secondhand.global.dto;

import com.example.secondhand.domain.user.status.StatusTrue;
import com.example.secondhand.global.exception.CustomErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {

	private int status;
	private StatusTrue message;
	private T data;

	public ApiResponse(int status, StatusTrue message) {
		this.status = status;
		this.message = message;
	}

	public static <T> ApiResponse<T> success(StatusTrue status) {
		return new ApiResponse<>(200, status);
	}

	public static <T> ApiResponse<T> success(StatusTrue status, T data) {
		return new ApiResponse<>(200, status, data);
	}
}
