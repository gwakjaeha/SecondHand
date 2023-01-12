package com.example.secondhand.domain.user.controller;

import static com.example.secondhand.domain.user.status.StatusTrue.*;

import com.example.secondhand.domain.user.dto.*;
import com.example.secondhand.domain.user.service.AccountService;
import com.example.secondhand.global.dto.ApiResponse;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AccountController {
	private final AccountService accountService;

	//회원가입
	@PostMapping("/register")
	public ApiResponse<String> createAccount(@RequestBody @Valid CreateAccountDto.Request request) {
		accountService.createAccount(request);
		return ApiResponse.success(REGISTER_TRUE);
	}

	//이메일 인증
	@GetMapping("/auth-email")
	public ApiResponse<String> authEmail(HttpServletRequest request) {
		accountService.authEmail(request.getParameter("id"));
		return ApiResponse.success(CERTIFICATION_EMAIL_TRUE);
	}

	//로그인
	@PostMapping("/login")
	public ApiResponse<TokenDto.Response> loginAccount(@RequestBody @Valid LoginAccountDto.Request request) {
		return ApiResponse.success(LOGIN_TRUE, accountService.loginAccount(request));
	}

	//내 정보 조회
	@GetMapping("/")
	public ApiResponse<ReadAccountDto> readAccountInfo() {
		return ApiResponse.success(READ_ACCOUNT_INFO_TRUE, accountService.readAccountInfo());
	}

	//내 정보 수정
	@PatchMapping("/")
	public ApiResponse<String> changeAccountInfo(@RequestBody @Valid final ChangeAccountDto.Request request){
		accountService.changeAccountInfo(request);
		return ApiResponse.success(CHANGE_ACCOUNT_INFO_TRUE);
	}

	//로그아웃
	@DeleteMapping("/logout")
	public ApiResponse<String> logoutAccount(@RequestBody @Valid LogoutAccountDto.Request request){
		accountService.logoutAccount(request);
		return ApiResponse.success(LOGOUT_TRUE);
	}

	//비밀번호 변경 (로그인 상태)
	@PatchMapping("/password")
	public ApiResponse<String> changePassword(@RequestBody @Valid ChangePasswordDto.Request request){
		accountService.changePassword(request);
		return ApiResponse.success(PASSWORD_CHANGE_TRUE);
	}

	//비밀번호 변경 (로그인 안한 상태, 비밀번호 분실 경우)
	@PutMapping("/password")
	public ApiResponse<String> changeLostPassword(@RequestBody @Valid ChangePasswordDto.LostRequest request){
		accountService.changeLostPassword(request);
		return ApiResponse.success(PASSWORD_CHANGE_TRUE);
	}

	//회원탈퇴
	@DeleteMapping("/")
	public ApiResponse<String> deleteAccount(@RequestBody @Valid DeleteAccountDto.Request request){
		accountService.deleteAccount(request);
		return ApiResponse.success(DELETE_ACCOUNT_TRUE);
	}

	//Access Token 재발급
	@GetMapping
	public ApiResponse<TokenDto.Response> reissue(@RequestBody @Valid TokenDto.Request request) {
		return ApiResponse.success(TOKEN_REISSUE_TRUE, accountService.reissue(request));
	}
}
