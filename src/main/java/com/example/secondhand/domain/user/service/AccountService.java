package com.example.secondhand.domain.user.service;

import static com.example.secondhand.domain.user.model.Model.AUTHORIZATION_HEADER;
import static com.example.secondhand.domain.user.type.AccountStatusCode.ACCOUNT_STATUS_ING;
import static com.example.secondhand.global.exception.CustomErrorCode.DUPLICATE_USER;
import static com.example.secondhand.global.exception.CustomErrorCode.LOGIN_FALSE;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EMAIL_FORM;
import static com.example.secondhand.global.exception.CustomErrorCode.PASSWORD_SIZE_ERROR;
import static com.example.secondhand.global.exception.CustomErrorCode.REGISTER_INFO_NULL;

import com.example.secondhand.domain.user.dto.CreateAccount.Request;
import com.example.secondhand.domain.user.dto.LoginAccount;
import com.example.secondhand.global.config.jwt.TokenProvider;
import com.example.secondhand.domain.user.domain.Account;
import com.example.secondhand.domain.user.model.Model;
import com.example.secondhand.domain.user.model.StatusTrue;
import com.example.secondhand.domain.user.repository.AccountRepository;
import com.example.secondhand.global.config.redis.RedisDao;
import com.example.secondhand.domain.user.dto.CreateAccount;
import com.example.secondhand.global.exception.CustomErrorCode;
import com.example.secondhand.global.exception.CustomException;
import java.time.Duration;
import java.time.LocalDateTime;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;

	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManagerBuilder authenticationManagerBuilder;

	private final RedisDao redisDao;
	private final TokenProvider tokenProvider;

	@Transactional
	public ResponseEntity<StatusTrue> createAccount(CreateAccount.Request request) {

		createAccountValidation(request);

		accountRepository.save(Account.builder()
			.areaId(request.getAreaId())
			.email(request.getEmail())
			.password(passwordEncoder.encode(request.getPassword()))
			.userName(request.getUserName())
			.phone(request.getPhone())
			.adminYn(false)
			.status(ACCOUNT_STATUS_ING)
			.createDt(LocalDateTime.now())
			.build());

		UsernamePasswordAuthenticationToken authenticationToken =
			new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String atk = tokenProvider.createToken(authentication);
		String rtk = tokenProvider.createRefreshToken(request.getEmail());

		redisDao.setValues(request.getEmail(), rtk, Duration.ofDays(1));

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(AUTHORIZATION_HEADER, "Bearer " + atk);

		return new ResponseEntity<>(StatusTrue.REGISTER_STATUS_TRUE, httpHeaders, HttpStatus.OK);
	}

	private void createAccountValidation(Request request){

		if (request.getAreaId() == null || request.getEmail() == null || request.getPassword() == null
			|| request.getUserName() == null || request.getPhone() == null)
			throw new CustomException(REGISTER_INFO_NULL);

		if (accountRepository.existsByEmail(request.getEmail()))
			throw new CustomException(DUPLICATE_USER);

		if (!request.getEmail().contains("@"))
			throw new CustomException(NOT_EMAIL_FORM);

		if (!(request.getPassword().length() > 5))
			throw new CustomException(PASSWORD_SIZE_ERROR);
	}

	public ResponseEntity<StatusTrue> loginAccount(LoginAccount.Request request) {
		loginAccountValidation(request);

		UsernamePasswordAuthenticationToken authenticationToken =
			new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String atk = tokenProvider.createToken(authentication);
		String rtk = tokenProvider.createRefreshToken(request.getEmail());

		redisDao.setValues(request.getEmail(), rtk, Duration.ofDays(1));

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add(AUTHORIZATION_HEADER, "Bearer " + atk);

		return new ResponseEntity<>(StatusTrue.LOGIN_STATUS_TRUE, httpHeaders, HttpStatus.OK);
	}

	private void loginAccountValidation(LoginAccount.Request request) {

		if (!request.getEmail().contains("@"))
			throw new CustomException(NOT_EMAIL_FORM);

		accountRepository.findByEmail(request.getEmail())
			.orElseThrow(
				() -> new CustomException(LOGIN_FALSE)
			);

		if (!passwordEncoder.matches(
			request.getPassword(),
			accountRepository.findByEmail(request.getEmail())
				.get()
				.getPassword()
		)
		) {
			throw new CustomException(LOGIN_FALSE);
		}
	}
}
