package com.example.secondhand.domain.user.controller;

import static com.example.secondhand.domain.user.status.StatusTrue.*;

import com.example.secondhand.domain.user.dto.*;
import com.example.secondhand.domain.user.service.AccountService;
import com.example.secondhand.global.dto.ApiResponse;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AccountController {
	private final AccountService accountService;

	@ApiOperation(value = "회원가입을 합니다.")
	@PostMapping("/register")
	public ApiResponse<String> createAccount(@RequestBody @Valid CreateAccountDto.Request request) {
		accountService.createAccount(request);
		return ApiResponse.success(REGISTER_TRUE);
	}

	@ApiOperation(value = "회원가입시 이메일 인증을 합니다.")
	@GetMapping("/auth-email")
	public ApiResponse<String> authEmail(HttpServletRequest request) {
		accountService.authEmail(request.getParameter("id"));
		return ApiResponse.success(CERTIFICATION_EMAIL_TRUE);
	}

	@ApiOperation(value = "로그인을 합니다.")
	@PostMapping("/login")
	public ApiResponse<TokenDto.Response> loginAccount(@RequestBody @Valid LoginAccountDto.Request request) {
		return ApiResponse.success(LOGIN_TRUE, accountService.loginAccount(request));
	}

	@ApiOperation(value = "내 정보를 조회합니다.")
	@GetMapping("/")
	public ApiResponse<ReadAccountDto> readAccountInfo() {
		return ApiResponse.success(READ_ACCOUNT_INFO_TRUE, accountService.readAccountInfo());
	}

	@ApiOperation(value = "내 정보를 수정합니다.")
	@PatchMapping("/")
	public ApiResponse<String> changeAccountInfo(@RequestBody @Valid final ChangeAccountDto.Request request){
		accountService.changeAccountInfo(request);
		return ApiResponse.success(CHANGE_ACCOUNT_INFO_TRUE);
	}

	@ApiOperation(value = "로그아웃을 합니다.")
	@DeleteMapping("/logout")
	public ApiResponse<String> logoutAccount(@RequestBody @Valid LogoutAccountDto.Request request){
		accountService.logoutAccount(request);
		return ApiResponse.success(LOGOUT_TRUE);
	}

	@ApiOperation(value = "로그인한 상태에서 비밀번호를 변경합니다.")
	@PatchMapping("/password")
	public ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordDto.Request request){
		accountService.changePassword(request);
		return ApiResponse.success(PASSWORD_CHANGE_TRUE);
	}

	@ApiOperation(value = "비밀번호 분실시 비밀번호를 변경합니다.")
	@PutMapping("/password")
	public ApiResponse<String> changeLostPassword(@RequestBody @Valid ChangePasswordDto.LostRequest request){
		accountService.changeLostPassword(request);
		return ApiResponse.success(PASSWORD_CHANGE_TRUE);
	}

	@ApiOperation(value = "회원탈퇴를 합니다.")
	@DeleteMapping("/")
	public ApiResponse<String> deleteAccount(@RequestBody @Valid DeleteAccountDto.Request request){
		accountService.deleteAccount(request);
		return ApiResponse.success(DELETE_ACCOUNT_TRUE);
	}

	@ApiOperation(value = "Access Token 을 재발급 받습니다.")
	@GetMapping
	public ApiResponse<TokenDto.Response> reissue(@RequestBody @Valid TokenDto.Request request) {
		return ApiResponse.success(TOKEN_REISSUE_TRUE, accountService.reissue(request));
	}
}
