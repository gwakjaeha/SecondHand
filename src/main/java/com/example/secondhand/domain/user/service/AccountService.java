package com.example.secondhand.domain.user.service;

import static com.example.secondhand.domain.user.model.Model.AUTHORIZATION_HEADER;
import static com.example.secondhand.domain.user.type.AccountStatusCode.ACCOUNT_STATUS_ING;
import static com.example.secondhand.domain.user.type.AccountStatusCode.ACCOUNT_STATUS_REQ;
import static com.example.secondhand.global.exception.CustomErrorCode.DUPLICATE_USER;
import static com.example.secondhand.global.exception.CustomErrorCode.LOGIN_FALSE;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EMAIL_FORM;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EXIST_UUID;
import static com.example.secondhand.global.exception.CustomErrorCode.PASSWORD_SIZE_ERROR;
import static com.example.secondhand.global.exception.CustomErrorCode.REFRESH_TOKEN_IS_BAD_REQUEST;
import static com.example.secondhand.global.exception.CustomErrorCode.REGISTER_INFO_NULL;
import static com.example.secondhand.global.exception.CustomErrorCode.SEND_EMAIL_FAIL;

import com.example.secondhand.domain.user.components.MailComponents;
import com.example.secondhand.domain.user.dto.CreateAccount.Request;
import com.example.secondhand.domain.user.dto.SendEmailDto;
import com.example.secondhand.domain.user.dto.LoginAccount;
import com.example.secondhand.global.config.jwt.TokenProvider;
import com.example.secondhand.domain.user.domain.Account;
import com.example.secondhand.domain.user.model.StatusTrue;
import com.example.secondhand.domain.user.repository.AccountRepository;
import com.example.secondhand.global.config.redis.RedisDao;
import com.example.secondhand.domain.user.dto.CreateAccount;
import com.example.secondhand.global.exception.CustomException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
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

	private final MailComponents mailComponents;

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
			.status(ACCOUNT_STATUS_REQ)
			.createDt(LocalDateTime.now())
			.build());

		UsernamePasswordAuthenticationToken authenticationToken =
			new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String atk = tokenProvider.createToken(authentication);
		String rtk = tokenProvider.createRefreshToken(request.getEmail());

		redisDao.setValues(request.getEmail(), rtk, Duration.ofDays(30));

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

		redisDao.setValues(request.getEmail(), rtk, Duration.ofDays(30));

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

	public ResponseEntity<StatusTrue> sendEmail(SendEmailDto.Request request) {

		String uuid = UUID.randomUUID().toString();

		Optional<Account> optionalAccount = accountRepository.findByEmail(request.getEmail());
		Account account = optionalAccount.get();
		account.setEmailAuthKey(uuid);
		accountRepository.save(account);

		String email = request.getEmail();
		String subject = "중고물품 거래 서비스 사이트 가입을 축하드립니다.";
		String text = "<p>중고물품 거래 서비스 사이트 가입을 축하드립니다. </p><p>아래 링크를 클릭하셔서 가입을 완료하세요.</p>"
			+ "<div><a target='_blank' href='http://localhost:8080/auth/auth-email?id=" + uuid + "'> 가입 완료 </a></div>";

		boolean result = mailComponents.sendMail(email,subject,text);
		if(!result){
			throw new CustomException(SEND_EMAIL_FAIL);
		}

		return new ResponseEntity<>(StatusTrue.SEND_EMAIL_TRUE, HttpStatus.OK);
	}

	public ResponseEntity<StatusTrue> authEmail(String uuid) {

		Optional<Account> optionalAccount = accountRepository.findByEmailAuthKey(uuid);
		if(!optionalAccount.isPresent()) {
			throw new CustomException(NOT_EXIST_UUID);
		}

		Account account = optionalAccount.get();
		account.setStatus(ACCOUNT_STATUS_ING);
		accountRepository.save(account);

		return new ResponseEntity<>(StatusTrue.CERTIFICATION_EMAIL_TRUE, HttpStatus.OK);
	}

	public ResponseEntity<Map<String, String>> reissue(String rtk) {
		Map<String, String> response = new HashMap<>();
		String username = tokenProvider.getRefreshTokenInfo(rtk);
		String rtkInRedis = redisDao.getValues(username);
		if (Objects.isNull(rtkInRedis) || !rtkInRedis.equals(rtk))
			throw new CustomException(REFRESH_TOKEN_IS_BAD_REQUEST);
		response.put("atk", tokenProvider.reCreateToken(username));

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
