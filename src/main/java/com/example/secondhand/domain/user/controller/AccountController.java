package com.example.secondhand.domain.user.controller;

import com.example.secondhand.domain.user.dto.CreateAccount;
import com.example.secondhand.domain.user.dto.LoginAccount;
import com.example.secondhand.domain.user.model.StatusTrue;
import com.example.secondhand.domain.user.service.AccountService;
import com.example.secondhand.global.exception.CustomException;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;

	//회원가입
	@PostMapping("/account")
	public ResponseEntity<StatusTrue> createAccount(
		@RequestBody @Valid CreateAccount.Request request
	) {
		return accountService.createAccount(request);
	}

	//로그인
	@PostMapping("/login")
	public ResponseEntity<StatusTrue> loginAccount(
		@RequestBody @Valid LoginAccount.Request request
	) {
		return accountService.loginAccount(request);
	}

}
