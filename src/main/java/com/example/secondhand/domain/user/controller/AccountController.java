package com.example.secondhand.domain.user.controller;

import static com.example.secondhand.domain.user.status.StatusTrue.CERTIFICATION_EMAIL_TRUE;
import static com.example.secondhand.domain.user.status.StatusTrue.CHANGE_INFO_TRUE;
import static com.example.secondhand.domain.user.status.StatusTrue.DELETE_ACCOUNT_TRUE;
import static com.example.secondhand.domain.user.status.StatusTrue.LOGIN_TRUE;
import static com.example.secondhand.domain.user.status.StatusTrue.LOGOUT_TRUE;
import static com.example.secondhand.domain.user.status.StatusTrue.PASSWORD_CHANGE_TRUE;
import static com.example.secondhand.domain.user.status.StatusTrue.READ_INFO_TRUE;
import static com.example.secondhand.domain.user.status.StatusTrue.REGISTER_TRUE;
import static com.example.secondhand.domain.user.status.StatusTrue.TOKEN_REISSUE_TRUE;

import com.example.secondhand.domain.user.dto.ChangeAccountDto;
import com.example.secondhand.domain.user.dto.ChangePasswordDto;
import com.example.secondhand.domain.user.dto.CreateAccountDto;
import com.example.secondhand.domain.user.dto.DeleteAccountDto;
import com.example.secondhand.domain.user.dto.LoginAccountDto;
import com.example.secondhand.domain.user.dto.LogoutAccountDto;
import com.example.secondhand.domain.user.dto.ReadAccountDto;
import com.example.secondhand.domain.user.dto.TokenDto;
import com.example.secondhand.domain.user.service.AccountService;
import com.example.secondhand.global.dto.ApiResponse;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
		return ApiResponse.success(READ_INFO_TRUE, accountService.readAccountInfo());
	}

	//내 정보 수정
	@PatchMapping("/")
	public ApiResponse<String> changeAccountInfo(@RequestBody @Valid final ChangeAccountDto.Request request){
		accountService.changeAccountInfo(request);
		return ApiResponse.success(CHANGE_INFO_TRUE);
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
