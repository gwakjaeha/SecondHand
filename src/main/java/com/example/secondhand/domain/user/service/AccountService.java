package com.example.secondhand.domain.user.service;

import static com.example.secondhand.domain.user.status.AccountStatusCode.*;
import static com.example.secondhand.global.exception.CustomErrorCode.*;

import com.example.secondhand.domain.user.components.MailComponents;
import com.example.secondhand.domain.user.domain.Account;
import com.example.secondhand.domain.user.dto.*;
import com.example.secondhand.domain.user.dto.ChangePasswordDto.LostRequest;
import com.example.secondhand.domain.user.repository.AccountRepository;
import com.example.secondhand.global.config.jwt.SecurityUtil;
import com.example.secondhand.global.config.jwt.TokenProvider;
import com.example.secondhand.global.config.redis.RedisDao;
import com.example.secondhand.global.exception.CustomErrorCode;
import com.example.secondhand.global.exception.CustomException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
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
	private final AuthenticationManager authenticationManager;

	private final RedisDao redisDao;
	private final TokenProvider tokenProvider;
	private final MailComponents mailComponents;

	@Value("${serviceUrl}")
	private String SERVICE_URL;

	@Value("${accessTokenPrefix}")
	private String ACCESS_TOKEN_PREFIX;

	@Value("${refreshTokenPrefix}")
	private String REFRESH_TOKEN_PREFIX;

	@Transactional
	public void createAccount(CreateAccountDto.Request request) {
		createAccountValidation(request);
		sendEmailAndSaveAccount(request);
	}

	public TokenDto.Response loginAccount(LoginAccountDto.Request request) {
		loginAccountValidation(request);
		return getAccessAndRefreshToken(request);
	}

	@Transactional
	public void authEmail(String uuid) {
		Account account = accountRepository.findByEmailAuthKey(uuid)
			.orElseThrow(() -> new CustomException(NOT_EXIST_UUID));
		account.setStatus(ACCOUNT_STATUS_ING);
		accountRepository.save(account);
	}

	@Transactional
	public ReadAccountDto readAccountInfo() {
		Optional<Account> accountList = accountRepository.findById(getTokenInfo().getUserId());
		Account account = accountList.get();
		return ReadAccountDto.builder()
			.areaId(account.getAreaId())
			.email(account.getEmail())
			.userName(account.getUserName())
			.phone(account.getPhone())
			.build();
	}

	@Transactional
	public void changeAccountInfo(ChangeAccountDto.Request request) {
		TokenInfoResponseDto tokenInfo = getTokenInfo();
		accountRepository.save(
			Account.builder()
				.userId(tokenInfo.getUserId())
				.areaId(request.getAreaId())
				.email(tokenInfo.getEmail())
				.password(tokenInfo.getPassword())
				.userName(request.getUserName())
				.phone(request.getPhone())
				.status(tokenInfo.getStatus())
				.emailAuthKey(tokenInfo.getEmailAuthKey())
				.admin(tokenInfo.isAdmin())
				.createdDt(tokenInfo.getCreateDt())
				.updatedDt(LocalDateTime.now())
				.build()
		);
	}

	@Transactional
	public void changePassword(ChangePasswordDto.Request request) {
		TokenInfoResponseDto tokenInfo = getTokenInfo();
		changePasswordValidation(request, tokenInfo);
		accountRepository.save(
			Account.builder()
				.userId(tokenInfo.getUserId())
				.areaId(tokenInfo.getAreaId())
				.email(tokenInfo.getEmail())
				.password(passwordEncoder.encode(request.getNewPassword()))
				.userName(tokenInfo.getUserName())
				.phone(tokenInfo.getPhone())
				.status(tokenInfo.getStatus())
				.emailAuthKey(tokenInfo.getEmailAuthKey())
				.admin(tokenInfo.isAdmin())
				.createdDt(tokenInfo.getCreateDt())
				.updatedDt(LocalDateTime.now())
				.build()
		);
	}

	@Transactional
	public void changeLostPassword(ChangePasswordDto.LostRequest request) {
		TokenInfoResponseDto accountInfo = getAccountInfo(request.getEmail());
		changeLostPasswordValidation(request, accountInfo.getPassword());
		accountRepository.save(
			Account.builder()
				.userId(accountInfo.getUserId())
				.areaId(accountInfo.getAreaId())
				.email(accountInfo.getEmail())
				.password(passwordEncoder.encode(request.getNewPassword()))
				.userName(accountInfo.getUserName())
				.phone(accountInfo.getPhone())
				.status(accountInfo.getStatus())
				.emailAuthKey(accountInfo.getEmailAuthKey())
				.admin(accountInfo.isAdmin())
				.createdDt(accountInfo.getCreateDt())
				.updatedDt(LocalDateTime.now())
				.build()
		);
	}

	@Transactional
	public void logoutAccount(LogoutAccountDto.Request request) {
		String atk = request.getAccessToken();
		makeIneffectiveToken(atk);
	}

	@Transactional
	public void deleteAccount(DeleteAccountDto.Request request) {
		TokenInfoResponseDto tokenInfo = getTokenInfo();
		deleteAccountValidation(request, tokenInfo);
		accountRepository.save(
			Account.builder()
				.userId(tokenInfo.getUserId())
				.areaId(tokenInfo.getAreaId())
				.email(tokenInfo.getEmail())
				.password(tokenInfo.getPassword())
				.userName(tokenInfo.getUserName())
				.phone(tokenInfo.getPhone())
				.status(tokenInfo.getStatus())
				.emailAuthKey(tokenInfo.getEmailAuthKey())
				.admin(tokenInfo.isAdmin())
				.createdDt(tokenInfo.getCreateDt())
				.updatedDt(tokenInfo.getUpdateDt())
				.deleteDt(LocalDateTime.now())
				.build()
		);
	}

	public TokenDto.Response reissue(TokenDto.Request request) {
		String email = tokenProvider.getRefreshTokenInfo(request.getRefreshToken());
		String rtkInRedis = redisDao.getValues(REFRESH_TOKEN_PREFIX + email);
		if (Objects.isNull(rtkInRedis) || !rtkInRedis.equals(request.getRefreshToken()))
			throw new CustomException(REFRESH_TOKEN_IS_BAD_REQUEST);

		return TokenDto.Response.builder()
			.grantType("Bearer")
			.accessToken(tokenProvider.reCreateToken(email))
			.refreshToken(request.getRefreshToken())
			.build();
	}

	@Transactional
	public void sendEmailAndSaveAccount(CreateAccountDto.Request request) {

		String uuid = UUID.randomUUID().toString();

		Account savedAccount = accountRepository.save(Account.builder()
			.areaId(request.getAreaId())
			.email(request.getEmail())
			.password(passwordEncoder.encode(request.getPassword()))
			.userName(request.getUserName())
			.phone(request.getPhone())
			.admin(false)
			.status(ACCOUNT_STATUS_REQ)
			.emailAuthKey(uuid)
			.build());

		String subject = "중고물품 거래 서비스 사이트 가입을 축하드립니다.";
		String text = "<p>중고물품 거래 서비스 사이트 가입을 축하드립니다. </p><p>아래 링크를 클릭하셔서 가입을 완료하세요.</p>"
			+ "<div><a target='_blank' href='" + SERVICE_URL + "/auth/auth-email?id=" + uuid + "'> 가입 완료 </a></div>";

		mailComponents.sendMail(savedAccount.getEmail(), subject, text);
	}

	private void createAccountValidation(CreateAccountDto.Request request){

		if (accountRepository.existsByEmail(request.getEmail()))
			throw new CustomException(DUPLICATE_ACCOUNT);

		if (!request.getEmail().contains("@"))
			throw new CustomException(NOT_EMAIL_FORM);

		if (!(request.getPassword().length() > 5))
			throw new CustomException(PASSWORD_SIZE_ERROR);
	}

	private void loginAccountValidation(LoginAccountDto.Request request) {

		if (!request.getEmail().contains("@"))
			throw new CustomException(NOT_EMAIL_FORM);

		Account account = accountRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new CustomException(LOGIN_FALSE_NOT_EXIST_EMAIL));

		if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
			throw new CustomException(LOGIN_FALSE_NOT_CORRECT_PASSWORD);
		}

		if(account.ACCOUNT_STATUS_REQ.equals(account.getStatus())){
			throw new CustomException(CustomErrorCode.REQ_EMAIL);
		}

		if(account.ACCOUNT_STATUS_STOP.equals(account.getStatus())){
			throw new CustomException(CustomErrorCode.STOP_EMAIL);
		}

		if(account.ACCOUNT_STATUS_WITHDRAW.equals(account.getStatus())){
			throw new CustomException(CustomErrorCode.WITHDRAW_EMAIL);
		}
	}

	private void changeLostPasswordValidation(LostRequest request, String password) {
		accountRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		if (passwordEncoder.matches(request.getNewPassword(), password)) {
			throw new CustomException(PASSWORD_IS_NOT_CHANGE);
		}
	}

	private void changePasswordValidation(ChangePasswordDto.Request request, TokenInfoResponseDto tokenInfo) {
		accountRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		if (!passwordEncoder.matches(request.getPassword(), tokenInfo.getPassword())) {
			throw new CustomException(PASSWORD_CHANGE_FALSE);
		}
	}

	private void deleteAccountValidation(DeleteAccountDto.Request request, TokenInfoResponseDto tokenInfo) {
		if (!passwordEncoder.matches(request.getPassword(), tokenInfo.getPassword())) {
			throw new CustomException(DELETE_ACCOUNT_FALSE);
		}
	}

	private TokenDto.Response getAccessAndRefreshToken(LoginAccountDto.Request request){
		UsernamePasswordAuthenticationToken authenticationToken =
			new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

		//loadUserByUsername() 를 통해 권한 정보도 포함시킴.
		Authentication authentication = authenticationManager.authenticate(authenticationToken);
		//헤더의 인증정보를 스레드 내 저장소에 담아놓고 해당 스레드에서 필요 시 꺼내서 사용하기 위함.
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String atk = tokenProvider.createToken(authentication);
		String rtk = tokenProvider.createRefreshToken(request.getEmail());

		redisDao.setValues(REFRESH_TOKEN_PREFIX + request.getEmail(), rtk, Duration.ofDays(14));

		return TokenDto.Response.builder()
			.grantType("Bearer")
			.accessToken(atk)
			.refreshToken(rtk).build();
	}

	public TokenInfoResponseDto getTokenInfo() {
		return TokenInfoResponseDto.Response(
			Objects.requireNonNull(SecurityUtil.getCurrentUsername()
				.flatMap(accountRepository::findOneByEmail)
				.orElse(null))
		);
	}

	private TokenInfoResponseDto getAccountInfo(String email) {
		return TokenInfoResponseDto.Response(
			Objects.requireNonNull(Optional.of(email)
				.flatMap(accountRepository::findOneByEmail)
				.orElse(null))
		);
	}

	private void makeIneffectiveToken(String atk){
		String email = SecurityContextHolder.getContext()
			.getAuthentication()
			.getName();

		if(redisDao.getValues(ACCESS_TOKEN_PREFIX + email) != null){
			redisDao.deleteValues(ACCESS_TOKEN_PREFIX + email);
		}

		//추후 서비스 사용시, JwtFilter 에서 해당 Access Token 은 유효하지 않은 것으로 처리하기 위해 필요.
		redisDao.setValues(ACCESS_TOKEN_PREFIX + atk, "logout", Duration.ofMillis(
			tokenProvider.getExpiration(atk)
		));
	}
}
