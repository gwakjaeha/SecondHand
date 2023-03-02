package com.example.secondhand.domain.user.service;

import static com.example.secondhand.global.status.UserStatusCode.*;
import static com.example.secondhand.global.exception.CustomErrorCode.*;

import com.example.secondhand.domain.area.entity.Area;
import com.example.secondhand.domain.area.repository.AreaRepository;
import com.example.secondhand.domain.user.components.MailComponents;
import com.example.secondhand.domain.user.domain.User;
import com.example.secondhand.domain.user.dto.*;
import com.example.secondhand.domain.user.dto.ChangePasswordDto.LostRequest;
import com.example.secondhand.domain.user.dto.ChangeUserDto.Request;
import com.example.secondhand.domain.user.repository.UserRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final AreaRepository areaRepository;
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
	public void createAccount(CreateUserDto.Request request) {
		createAccountValidation(request);
		sendEmailAndSaveAccount(request);
	}

	public TokenDto.Response loginAccount(LoginUserDto.Request request) {
		loginAccountValidation(request);
		return getAccessAndRefreshToken(request);
	}

	@Transactional
	public void authEmail(String uuid) {
		User user = userRepository.findByEmailAuthKey(uuid)
			.orElseThrow(() -> new CustomException(NOT_EXIST_UUID));
		user.setStatus(USER_STATUS_ING);
		userRepository.save(user);
	}

	@Transactional
	public ReadUserDto readAccountInfo(String email) {

		User user = getUser(email);

		return ReadUserDto.builder()
			.areaId(user.getArea().getId())
			.email(user.getEmail())
			.userName(user.getUserName())
			.phone(user.getPhone())
			.build();
	}

	@Transactional
	public void changeAccountInfo(Request request, String email) {

		User user = getUser(email);

		userRepository.save(
			User.builder()
				.id(user.getId())
				.area(user.getArea())
				.email(user.getEmail())
				.password(user.getPassword())
				.userName(request.getUserName())
				.phone(request.getPhone())
				.status(user.getStatus())
				.emailAuthKey(user.getEmailAuthKey())
				.admin(user.isAdmin())
				.createdAt(user.getCreatedAt())
				.updatedAt(LocalDateTime.now())
				.build()
		);
	}

	@Transactional
	public void changePassword(ChangePasswordDto.Request request, String email) {

		User user = getUser(email);

		changePasswordValidation(request, user.getPassword());

		userRepository.save(
			User.builder()
				.id(user.getId())
				.area(user.getArea())
				.email(user.getEmail())
				.password(passwordEncoder.encode(request.getNewPassword()))
				.userName(user.getUserName())
				.phone(user.getPhone())
				.status(user.getStatus())
				.emailAuthKey(user.getEmailAuthKey())
				.admin(user.isAdmin())
				.createdAt(user.getCreatedAt())
				.updatedAt(LocalDateTime.now())
				.build()
		);
	}

	@Transactional
	public void changeLostPassword(ChangePasswordDto.LostRequest request) {
		TokenInfoResponseDto accountInfo = getAccountInfo(request.getEmail());
		changeLostPasswordValidation(request, accountInfo.getPassword());

		Area area = areaRepository.findById(accountInfo.getAreaId())
			.orElseThrow(() -> new CustomException(NOT_FOUND_AREA));

		userRepository.save(
			User.builder()
				.id(accountInfo.getUserId())
				.area(area)
				.email(accountInfo.getEmail())
				.password(passwordEncoder.encode(request.getNewPassword()))
				.userName(accountInfo.getUserName())
				.phone(accountInfo.getPhone())
				.status(accountInfo.getStatus())
				.emailAuthKey(accountInfo.getEmailAuthKey())
				.admin(accountInfo.isAdmin())
				.createdAt(accountInfo.getCreateAt())
				.updatedAt(LocalDateTime.now())
				.build()
		);
	}

	@Transactional
	public void logoutAccount(LogoutUserDto.Request request) {
		String atk = request.getAccessToken();
		makeIneffectiveToken(atk);
	}

	@Transactional
	public void deleteAccount(DeleteUserDto.Request request, String email) {

		User user = getUser(email);

		deleteAccountValidation(request, user.getPassword());

		userRepository.save(
			User.builder()
				.id(user.getId())
				.area(user.getArea())
				.email(user.getEmail())
				.password(user.getPassword())
				.userName(user.getUserName())
				.phone(user.getPhone())
				.status(user.getStatus())
				.emailAuthKey(user.getEmailAuthKey())
				.admin(user.isAdmin())
				.createdAt(user.getCreatedAt())
				.updatedAt(user.getUpdatedAt())
				.deleteAt(LocalDateTime.now())
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
	public void sendEmailAndSaveAccount(CreateUserDto.Request request) {

		String uuid = UUID.randomUUID().toString();

		Area area = areaRepository.findById(request.getAreaId())
			.orElseThrow(() -> new CustomException(NOT_FOUND_AREA));

		User savedUser = userRepository.save(User.builder()
			.area(area)
			.email(request.getEmail())
			.password(passwordEncoder.encode(request.getPassword()))
			.userName(request.getUserName())
			.phone(request.getPhone())
			.admin(false)
			.status(USER_STATUS_REQ)
			.emailAuthKey(uuid)
			.build());

		String subject = "중고물품 거래 서비스 사이트 가입을 축하드립니다.";
		String text = "<p>중고물품 거래 서비스 사이트 가입을 축하드립니다. </p><p>아래 링크를 클릭하셔서 가입을 완료하세요.</p>"
			+ "<div><a target='_blank' href='" + SERVICE_URL + "/auth/auth-email?id=" + uuid + "'> 가입 완료 </a></div>";

		mailComponents.sendMail(savedUser.getEmail(), subject, text);
	}

	private void createAccountValidation(CreateUserDto.Request request){

		if (userRepository.existsByEmail(request.getEmail()))
			throw new CustomException(DUPLICATE_ACCOUNT);

		if (!request.getEmail().contains("@"))
			throw new CustomException(NOT_EMAIL_FORM);

		if (!(request.getPassword().length() > 5))
			throw new CustomException(PASSWORD_SIZE_ERROR);
	}

	private void loginAccountValidation(LoginUserDto.Request request) {

		if (!request.getEmail().contains("@"))
			throw new CustomException(NOT_EMAIL_FORM);

		User user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new CustomException(LOGIN_FALSE_NOT_EXIST_EMAIL));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new CustomException(LOGIN_FALSE_NOT_CORRECT_PASSWORD);
		}

		if(user.USER_STATUS_REQ.equals(user.getStatus())){
			throw new CustomException(CustomErrorCode.REQ_EMAIL);
		}

		if(user.USER_STATUS_STOP.equals(user.getStatus())){
			throw new CustomException(CustomErrorCode.STOP_EMAIL);
		}

		if(user.USER_STATUS_WITHDRAW.equals(user.getStatus())){
			throw new CustomException(CustomErrorCode.WITHDRAW_EMAIL);
		}
	}

	private void changeLostPasswordValidation(LostRequest request, String password) {
		userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		if (passwordEncoder.matches(request.getNewPassword(), password)) {
			throw new CustomException(PASSWORD_IS_NOT_CHANGE);
		}
	}

	private void changePasswordValidation(ChangePasswordDto.Request request, String password) {
		userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		if (!passwordEncoder.matches(request.getPassword(), password)) {
			throw new CustomException(PASSWORD_CHANGE_FALSE);
		}
	}

	private void deleteAccountValidation(DeleteUserDto.Request request, String password) {
		if (!passwordEncoder.matches(request.getPassword(), password)) {
			throw new CustomException(DELETE_ACCOUNT_FALSE);
		}
	}

	private TokenDto.Response getAccessAndRefreshToken(LoginUserDto.Request request){
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

	public User getUser(String email){
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		return user;
	}

	public TokenInfoResponseDto getTokenInfo() {
		return TokenInfoResponseDto.Response(
			Objects.requireNonNull(SecurityUtil.getCurrentUsername()
				.flatMap(userRepository::findOneByEmail)
				.orElse(null))
		);
	}

	private TokenInfoResponseDto getAccountInfo(String email) {
		return TokenInfoResponseDto.Response(
			Objects.requireNonNull(Optional.of(email)
				.flatMap(userRepository::findOneByEmail)
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
