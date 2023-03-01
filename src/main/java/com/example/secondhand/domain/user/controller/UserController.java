package com.example.secondhand.domain.user.controller;

import static com.example.secondhand.domain.user.status.StatusTrue.*;

import com.example.secondhand.domain.user.dto.*;
import com.example.secondhand.domain.user.service.UserService;
import com.example.secondhand.global.dto.ApiResponse;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {
	private final UserService userService;

	@ApiOperation(value = "회원가입을 합니다.")
	@PostMapping("/register")
	public ApiResponse<String> createAccount(@RequestBody @Valid CreateUserDto.Request request) {
		userService.createAccount(request);
		return ApiResponse.success(REGISTER_TRUE);
	}

	@ApiOperation(value = "회원가입시 이메일 인증을 합니다.")
	@GetMapping("/auth-email")
	public ApiResponse<String> authEmail(HttpServletRequest request) {
		userService.authEmail(request.getParameter("id"));
		return ApiResponse.success(CERTIFICATION_EMAIL_TRUE);
	}

	@ApiOperation(value = "로그인을 합니다.")
	@PostMapping("/login")
	public ApiResponse<TokenDto.Response> loginAccount(@RequestBody @Valid LoginUserDto.Request request) {
		return ApiResponse.success(LOGIN_TRUE, userService.loginAccount(request));
	}

	@ApiOperation(value = "내 정보를 조회합니다.")
	@GetMapping("/")
	public ApiResponse<ReadUserDto> readAccountInfo() {
		return ApiResponse.success(READ_ACCOUNT_INFO_TRUE, userService.readAccountInfo());
	}

	@ApiOperation(value = "내 정보를 수정합니다.")
	@PatchMapping("/")
	public ApiResponse<String> changeAccountInfo(@RequestBody @Valid final ChangeUserDto.Request request){
		userService.changeAccountInfo(request);
		return ApiResponse.success(CHANGE_ACCOUNT_INFO_TRUE);
	}

	@ApiOperation(value = "로그아웃을 합니다.")
	@DeleteMapping("/logout")
	public ApiResponse<String> logoutAccount(@RequestBody @Valid LogoutUserDto.Request request){
		userService.logoutAccount(request);
		return ApiResponse.success(LOGOUT_TRUE);
	}

	@ApiOperation(value = "로그인한 상태에서 비밀번호를 변경합니다.")
	@PatchMapping("/password")
	public ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordDto.Request request){
		userService.changePassword(request);
		return ApiResponse.success(PASSWORD_CHANGE_TRUE);
	}

	@ApiOperation(value = "비밀번호 분실시 비밀번호를 변경합니다.")
	@PutMapping("/password")
	public ApiResponse<String> changeLostPassword(@RequestBody @Valid ChangePasswordDto.LostRequest request){
		userService.changeLostPassword(request);
		return ApiResponse.success(PASSWORD_CHANGE_TRUE);
	}

	@ApiOperation(value = "회원탈퇴를 합니다.")
	@DeleteMapping("/")
	public ApiResponse<String> deleteAccount(@RequestBody @Valid DeleteUserDto.Request request){
		userService.deleteAccount(request);
		return ApiResponse.success(DELETE_ACCOUNT_TRUE);
	}

	@ApiOperation(value = "Access Token 을 재발급 받습니다.")
	@GetMapping
	public ApiResponse<TokenDto.Response> reissue(@RequestBody @Valid TokenDto.Request request) {
		return ApiResponse.success(TOKEN_REISSUE_TRUE, userService.reissue(request));
	}
}
