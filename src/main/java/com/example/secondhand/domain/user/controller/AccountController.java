package com.example.secondhand.domain.user.controller;

import com.example.secondhand.domain.user.dto.CreateAccount;
import com.example.secondhand.domain.user.dto.SendEmailDto;
import com.example.secondhand.domain.user.dto.LoginAccount;
import com.example.secondhand.domain.user.model.StatusTrue;
import com.example.secondhand.domain.user.service.AccountService;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class AccountController {
	private final AccountService accountService;

	//이메일 전송
	@PostMapping("send-email")
	public ResponseEntity<StatusTrue> sendEmail(
		@Valid @RequestBody final SendEmailDto.Request request
	) {
		return accountService.sendEmail(request);
	}

	//이메일 인증
	@GetMapping("auth-email")
	public ResponseEntity<StatusTrue> authEmail(
		HttpServletRequest request
	) {
		String uuid = request.getParameter("id");
		return accountService.authEmail(uuid);
	}

	//회원가입
	@PostMapping("register")
	public ResponseEntity<StatusTrue> createAccount(
		@RequestBody @Valid CreateAccount.Request request
	) {
		return accountService.createAccount(request);
	}

	//로그인
	@PostMapping("login")
	public ResponseEntity<StatusTrue> loginAccount(
		@RequestBody @Valid LoginAccount.Request request
	) {
		return accountService.loginAccount(request);
	}

	// atk 재발급
	@GetMapping
	public ResponseEntity<Map<String,String>> reissue(
		@RequestHeader(value = "REFRESH_TOKEN") String rtk
	) {
		return accountService.reissue(rtk);
	}
}
