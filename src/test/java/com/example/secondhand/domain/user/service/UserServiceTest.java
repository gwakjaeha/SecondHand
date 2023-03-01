package com.example.secondhand.domain.user.service;

import static com.example.secondhand.global.status.UserStatusCode.USER_STATUS_ING;
import static com.example.secondhand.global.status.UserStatusCode.USER_STATUS_REQ;
import static com.example.secondhand.global.exception.CustomErrorCode.DUPLICATE_ACCOUNT;
import static com.example.secondhand.global.exception.CustomErrorCode.LOGIN_FALSE_NOT_CORRECT_PASSWORD;
import static com.example.secondhand.global.exception.CustomErrorCode.LOGIN_FALSE_NOT_EXIST_EMAIL;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EMAIL_FORM;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EXIST_UUID;
import static com.example.secondhand.global.exception.CustomErrorCode.PASSWORD_SIZE_ERROR;
import static com.example.secondhand.global.exception.CustomErrorCode.REQ_EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.secondhand.domain.area.entity.Area;
import com.example.secondhand.domain.area.repository.AreaRepository;
import com.example.secondhand.domain.user.components.MailComponents;
import com.example.secondhand.domain.user.domain.User;
import com.example.secondhand.domain.user.dto.ChangeUserDto;
import com.example.secondhand.domain.user.dto.ChangePasswordDto;
import com.example.secondhand.domain.user.dto.CreateUserDto;
import com.example.secondhand.domain.user.dto.DeleteUserDto;
import com.example.secondhand.domain.user.dto.LoginUserDto;
import com.example.secondhand.domain.user.dto.ReadUserDto;
import com.example.secondhand.domain.user.dto.TokenDto;
import com.example.secondhand.domain.user.repository.UserRepository;
import com.example.secondhand.global.config.jwt.TokenProvider;
import com.example.secondhand.global.config.redis.RedisDao;
import com.example.secondhand.global.exception.CustomException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

@ExtendWith(MockitoExtension.class) //Test 클래스가 Mockito 를 사용.
class UserServiceTest {

	@InjectMocks //Mock 객체가 주입된 클래스를 사용.
	private UserService userService;
	@Mock //실제 구현된 객체 대신 Mock 객체를 사용.
	private UserRepository userRepository;
	@Mock
	private AreaRepository areaRepository;
	@Mock
	private MailComponents mailComponents;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private TokenProvider tokenProvider;
	@Mock
	private RedisDao redisDao;
	@Mock
	private AuthenticationManager authenticationManager;
	@Value("${refreshTokenPrefix}")
	private String REFRESH_TOKEN_PREFIX;


	@Test
	void testCreateAccount() throws Exception{
		//given
		given(userRepository.existsByEmail(anyString()))
			.willReturn(false);
		given(passwordEncoder.encode(anyString()))
			.willReturn("encoded-password");
		given(areaRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Area.builder().id(300L).build()));
		given(userRepository.save(any()))
			.willReturn(User.builder()
				.area(Area.builder().id(300L).build())
				.email("example@email.com")
				.password("encoded-password")
				.userName("name")
				.phone("010-1111-2222")
				.admin(false)
				.status(USER_STATUS_REQ)
				.emailAuthKey("uuid")
				.build());
		willDoNothing().given(mailComponents).sendMail(anyString(), anyString(), anyString());

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

		//when
		userService.createAccount(CreateUserDto.Request.builder()
			.areaId(300L)
			.email("example@email.com")
			.password("password")
			.userName("name")
			.phone("010-1111-2222").build());

		//then
		verify(userRepository, times(1)).save(captor.capture());
		assertEquals(300, captor.getValue().getArea().getId());
		assertEquals("example@email.com", captor.getValue().getEmail());
		assertEquals("encoded-password", captor.getValue().getPassword());
		assertEquals("name", captor.getValue().getUserName());
		assertEquals("010-1111-2222", captor.getValue().getPhone());
		assertEquals(false, captor.getValue().isAdmin());
		assertEquals(USER_STATUS_REQ, captor.getValue().getStatus());
		assertNotNull(captor.getValue().getEmailAuthKey());
	}

	@Test
	void testDuplicateAccountInCreateAccount() throws Exception{
		//given
		given(userRepository.existsByEmail(anyString()))
			.willReturn(true);
		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> userService.createAccount(CreateUserDto.Request.builder()
					.areaId(300L)
					.email("example@email.com")
					.password("password")
					.userName("name")
					.phone("010-1111-2222")
					.build()));
		//then
		assertEquals(new CustomException(DUPLICATE_ACCOUNT).getCustomErrorCode(), exception.getCustomErrorCode());
	}

	@Test
	void testNotEmailFormInCreateAccount() throws Exception{
		//given
		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> userService.createAccount(CreateUserDto.Request.builder()
				.areaId(300L)
				.email("example.email.com")
				.password("password")
				.userName("name")
				.phone("010-1111-2222")
				.build()));
		//then
		assertEquals(new CustomException(NOT_EMAIL_FORM).getCustomErrorCode(), exception.getCustomErrorCode());
	}

	@Test
	void testPasswordSizeErrorInCreateAccount() throws Exception{
		//given
		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> userService.createAccount(CreateUserDto.Request.builder()
				.areaId(300L)
				.email("example@email.com")
				.password("pw")
				.userName("name")
				.phone("010-1111-2222")
				.build()));
		//then
		assertEquals(new CustomException(PASSWORD_SIZE_ERROR).getCustomErrorCode(), exception.getCustomErrorCode());
	}

	@Test
	void testLoginAccount() throws Exception{

		//given
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.ofNullable(User.builder()
				.password("password")
				.status("ING")
				.build()));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(true);
		given(tokenProvider.createToken(any()))
			.willReturn("access-token");
		given(tokenProvider.createRefreshToken(any()))
			.willReturn("refresh-token");

		LoginUserDto.Request request = new LoginUserDto.Request("example@email.com", "password");

		//when
		TokenDto.Response response = userService.loginAccount(request);

		//then
		assertEquals("Bearer", response.getGrantType());
		assertEquals("access-token", response.getAccessToken());
		assertEquals("refresh-token", response.getRefreshToken());
	}

	@Test
	void testNotEmailFormInLogin() throws Exception{
		//given
		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> userService.loginAccount(LoginUserDto.Request.builder()
				.email("example.email.com")
				.password("password").build()));
		//then
		assertEquals(new CustomException(NOT_EMAIL_FORM).getCustomErrorCode(), exception.getCustomErrorCode());
	}

	@Test
	void testNotExistEmailInLogin() throws Exception{
		//given
		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> userService.loginAccount(LoginUserDto.Request.builder()
				.email("example@email.com")
				.password("password").build()));
		//then
		assertEquals(new CustomException(LOGIN_FALSE_NOT_EXIST_EMAIL).getCustomErrorCode(), exception.getCustomErrorCode());
	}

	@Test
	void testNotCorrectPasswordInLogin() throws Exception{
		//given
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(User.builder()
				.password("password")
				.build()));
		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> userService.loginAccount(LoginUserDto.Request.builder()
				.email("example@email.com")
				.password("NotCorrectPassword").build()));
		//then
		assertEquals(new CustomException(LOGIN_FALSE_NOT_CORRECT_PASSWORD).getCustomErrorCode(), exception.getCustomErrorCode());
	}

	@Test
	void testNotCorrectStatusInLogin() throws Exception{
		//given
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(User.builder()
				.password("password")
				.status("REQ")
				.build()));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(true);
		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> userService.loginAccount(LoginUserDto.Request.builder()
				.email("example@email.com")
				.password("password").build()));
		//then
		assertEquals(new CustomException(REQ_EMAIL).getCustomErrorCode(), exception.getCustomErrorCode());
	}

	@Test
	void testAuthEmail() throws Exception{
		//given
		given(userRepository.findByEmailAuthKey(anyString()))
			.willReturn(Optional.of(User.builder()
				.status(USER_STATUS_ING)
				.build()));
		given(userRepository.save(any()))
			.willReturn(User.builder()
				.status(USER_STATUS_ING)
				.build());

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

		//when
		userService.authEmail("email-auth-key");

		//then
		verify(userRepository, times(1)).save(captor.capture());
		assertEquals(USER_STATUS_ING, captor.getValue().getStatus());
	}

	@Test
	void testNotExistUuid() throws Exception{
		//given
		given(userRepository.findByEmailAuthKey(anyString()))
			.willReturn(Optional.empty());

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> userService.authEmail("email-auth-key"));

		//then
		assertEquals(NOT_EXIST_UUID, exception.getCustomErrorCode());
	}

	@Test
	void testReadAccountInfo() throws Exception{
		//given
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken("username", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))));

		given(userRepository.findOneByEmail(anyString()))
			.willReturn(Optional.of(
				User.builder()
					.id(3L)
					.area(Area.builder().id(3L).build())
					.email("example@email.com")
					.password("password")
					.userName("name")
					.phone("010-1111-2222")
					.status("ING")
					.emailAuthKey("auth-key")
					.admin(false)
					.createdAt(LocalDateTime.now().minusDays(2))
					.updatedAt(LocalDateTime.now())
					.deleteDt(null)
					.build()));

		given(userRepository.findById(any()))
			.willReturn(Optional.of(User.builder()
				.area(Area.builder().id(300L).build())
				.email("example@email.com")
				.userName("name")
				.phone("010-1111-2222")
				.build()));

		//when
		ReadUserDto readUserDto = userService.readAccountInfo();
		//then
		assertEquals(300, readUserDto.getAreaId());
		assertEquals("example@email.com", readUserDto.getEmail());
		assertEquals("name", readUserDto.getUserName());
		assertEquals("010-1111-2222", readUserDto.getPhone());
	}

	@Test
	@WithMockUser
	void testChangeAccountInfo() throws Exception{
		//given
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken("username", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))));

		given(userRepository.findOneByEmail(anyString()))
			.willReturn(Optional.of(
				User.builder()
					.id(3L)
					.area(Area.builder().id(10L).build())
					.email("example@email.com")
					.password("password")
					.userName("name")
					.phone("010-1111-2222")
					.status("ING")
					.emailAuthKey("auth-key")
					.admin(false)
					.createdAt(LocalDateTime.now().minusDays(2))
					.updatedAt(LocalDateTime.now())
					.deleteDt(null)
					.build()));

		given(areaRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Area.builder().id(300L).build()));

		given(userRepository.save(any()))
			.willReturn(User.builder()
					.id(3L)
					.area(Area.builder().id(10L).build())
					.email("example@email.com")
					.password("password")
					.userName("name")
					.phone("010-1111-2222")
					.status("ING")
					.emailAuthKey("auth-key")
					.admin(false)
					.createdAt(LocalDateTime.now().minusDays(2))
					.updatedAt(LocalDateTime.now())
					.deleteDt(null)
					.build());

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

		//when
		userService.changeAccountInfo(ChangeUserDto.Request
										.builder()
										.areaId(200L)
										.userName("name")
										.phone("010-1111-2222")
										.build());

		//then
		verify(userRepository, times(1)).save(captor.capture());
		assertEquals(3L, captor.getValue().getId());
	}

	@Test
	void testChangePassword() throws Exception{
		//given
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken("username", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))));

		given(userRepository.findOneByEmail(anyString()))
			.willReturn(Optional.of(
				User.builder()
					.id(3L)
					.area(Area.builder().id(10L).build())
					.email("example@email.com")
					.password("password")
					.userName("name")
					.phone("010-1111-2222")
					.status("ING")
					.emailAuthKey("auth-key")
					.admin(false)
					.createdAt(LocalDateTime.now().minusDays(2))
					.updatedAt(LocalDateTime.now())
					.deleteDt(null)
					.build()));

		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(User.builder().build()));

		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(true);

		given(passwordEncoder.encode(anyString())).willReturn("changed-password");

		given(areaRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Area.builder().id(300L).build()));

		given(userRepository.save(any()))
			.willReturn(User.builder()
				.password("changed-password")
				.build());

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

		//when
		userService.changePassword(ChangePasswordDto.Request.builder()
										.email("example@email.com")
										.password("password")
										.newPassword("changed-password")
										.build());

		//then
		verify(userRepository, times(1)).save(captor.capture());
		assertEquals("changed-password", captor.getValue().getPassword());
	}

	@Test
	void testChangeLostPassword() throws Exception{
		//given
		given(userRepository.findOneByEmail(anyString()))
			.willReturn(Optional.of(
				User.builder()
					.id(3L)
					.area(Area.builder().id(10L).build())
					.email("example@email.com")
					.password("password")
					.userName("name")
					.phone("010-1111-2222")
					.status("ING")
					.emailAuthKey("auth-key")
					.admin(false)
					.createdAt(LocalDateTime.now().minusDays(2))
					.updatedAt(LocalDateTime.now())
					.deleteDt(null)
					.build()));

		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(User.builder().build()));

		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(false);

		given(passwordEncoder.encode(anyString())).willReturn("new-password");

		given(areaRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Area.builder().build()));

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

		//when
		userService.changeLostPassword(ChangePasswordDto.LostRequest.builder()
											.email("example@email.com")
											.newPassword("new-password")
											.build());

		//then
		verify(userRepository, times(1)).save(captor.capture());
		assertEquals("new-password", captor.getValue().getPassword());
	}

	@Test
	void testDeleteAccount() throws Exception{
		//given
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken("username", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))));

		given(userRepository.findOneByEmail(anyString()))
			.willReturn(Optional.of(
				User.builder()
					.id(3L)
					.area(Area.builder().id(10L).build())
					.email("example@email.com")
					.password("password")
					.userName("name")
					.phone("010-1111-2222")
					.status("ING")
					.emailAuthKey("auth-key")
					.admin(false)
					.createdAt(LocalDateTime.now().minusDays(2))
					.updatedAt(LocalDateTime.now())
					.deleteDt(null)
					.build()));

		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(true);

		given(areaRepository.findById(anyLong()))
			.willReturn(Optional.ofNullable(Area.builder().id(300L).build()));

		given(userRepository.save(any()))
			.willReturn(User.builder()
				.deleteDt(LocalDateTime.now())
				.build());

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

		//when
		userService.deleteAccount(DeleteUserDto.Request.builder()
													.password("password")
													.build());

		//then
		verify(userRepository, times(1)).save(captor.capture());
		assertNotNull(captor.getValue().getDeleteAt());
	}

	@Test
	void testReissue() throws Exception{
		//given
		given(tokenProvider.getRefreshTokenInfo(anyString()))
			.willReturn("example@email.com");

		given(redisDao.getValues(anyString()))
			.willReturn("refresh-token");

		given(tokenProvider.reCreateToken(anyString()))
			.willReturn("new-access-token");

		ArgumentCaptor<TokenDto.Response> captor = ArgumentCaptor.forClass(TokenDto.Response.class);

		//when
		TokenDto.Response response = userService.reissue(TokenDto.Request.builder()
										.refreshToken("refresh-token")
										.build());

		//then
		verify(redisDao, times(1))
				.getValues(REFRESH_TOKEN_PREFIX + "example@email.com");
		assertEquals("new-access-token", response.getAccessToken());
	}
}