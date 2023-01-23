package com.example.secondhand.domain.user.service;

import static com.example.secondhand.domain.user.status.AccountStatusCode.ACCOUNT_STATUS_ING;
import static com.example.secondhand.domain.user.status.AccountStatusCode.ACCOUNT_STATUS_REQ;
import static com.example.secondhand.global.exception.CustomErrorCode.DUPLICATE_ACCOUNT;
import static com.example.secondhand.global.exception.CustomErrorCode.LOGIN_FALSE_NOT_CORRECT_PASSWORD;
import static com.example.secondhand.global.exception.CustomErrorCode.LOGIN_FALSE_NOT_EXIST_EMAIL;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EMAIL_FORM;
import static com.example.secondhand.global.exception.CustomErrorCode.NOT_EXIST_UUID;
import static com.example.secondhand.global.exception.CustomErrorCode.PASSWORD_SIZE_ERROR;
import static com.example.secondhand.global.exception.CustomErrorCode.REGISTER_INFO_NULL;
import static com.example.secondhand.global.exception.CustomErrorCode.REQ_EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.secondhand.domain.user.components.MailComponents;
import com.example.secondhand.domain.user.domain.Account;
import com.example.secondhand.domain.user.dto.ChangeAccountDto;
import com.example.secondhand.domain.user.dto.CreateAccountDto;
import com.example.secondhand.domain.user.dto.LoginAccountDto;
import com.example.secondhand.domain.user.dto.ReadAccountDto;
import com.example.secondhand.domain.user.dto.TokenDto;
import com.example.secondhand.domain.user.dto.TokenInfoResponseDto;
import com.example.secondhand.domain.user.repository.AccountRepository;
import com.example.secondhand.global.config.jwt.SecurityUtil;
import com.example.secondhand.global.config.jwt.TokenProvider;
import com.example.secondhand.global.config.redis.RedisDao;
import com.example.secondhand.global.exception.CustomException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

@ExtendWith(MockitoExtension.class) //Test 클래스가 Mockito 를 사용.
class AccountServiceTest {

	@InjectMocks //Mock 객체가 주입된 클래스를 사용.
	private AccountService accountService;
	@Mock //실제 구현된 객체 대신 Mock 객체를 사용.
	private AccountRepository accountRepository;
	@Mock
	private MailComponents mailComponents;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private TokenProvider tokenProvider;
	@Mock
	private RedisDao redisDao;

//	@Before
//	public void setUp() {
//		SecurityContextHolder.getContext()
//			.setAuthentication(new UsernamePasswordAuthenticationToken("username", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))));
//	}

	@Test
	void testCreateAccount() throws Exception{
		//given
		given(accountRepository.existsByEmail(anyString()))
			.willReturn(false);
		given(passwordEncoder.encode(anyString()))
			.willReturn("encoded-password");
		given(accountRepository.save(any()))
			.willReturn(Account.builder()
				.areaId(300L)
				.email("example@email.com")
				.password("encoded-password")
				.userName("name")
				.phone("010-1111-2222")
				.admin(false)
				.status(ACCOUNT_STATUS_REQ)
				.emailAuthKey("uuid")
				.build());
		willDoNothing().given(mailComponents).sendMail(anyString(), anyString(), anyString());

		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

		//when
		accountService.createAccount(CreateAccountDto.Request.builder()
			.areaId(300L)
			.email("example@email.com")
			.password("password")
			.userName("name")
			.phone("010-1111-2222").build());

		//then
		verify(accountRepository, times(1)).save(captor.capture());
		assertEquals(300, captor.getValue().getAreaId());
		assertEquals("example@email.com", captor.getValue().getEmail());
		assertEquals("encoded-password", captor.getValue().getPassword());
		assertEquals("name", captor.getValue().getUserName());
		assertEquals("010-1111-2222", captor.getValue().getPhone());
		assertEquals(false, captor.getValue().isAdmin());
		assertEquals(ACCOUNT_STATUS_REQ, captor.getValue().getStatus());
		assertNotNull(captor.getValue().getEmailAuthKey());
	}

	@Test
	void testRegisterInfoNullInCreateAccount() throws Exception{
		//given
		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> accountService.createAccount(CreateAccountDto.Request.builder().build()));
		//then
		assertEquals(new CustomException(REGISTER_INFO_NULL).getCustomErrorCode(), exception.getCustomErrorCode());
	}

	@Test
	void testDuplicateAccountInCreateAccount() throws Exception{
		//given
		given(accountRepository.existsByEmail(anyString()))
			.willReturn(true);
		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> accountService.createAccount(CreateAccountDto.Request.builder()
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
			() -> accountService.createAccount(CreateAccountDto.Request.builder()
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
			() -> accountService.createAccount(CreateAccountDto.Request.builder()
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
		given(accountRepository.findByEmail(anyString()))
			.willReturn(Optional.ofNullable(Account.builder()
				.password("password")
				.status("ING")
				.build()));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(true);
		given(tokenProvider.createToken(any()))
			.willReturn("access-token");
		given(tokenProvider.createRefreshToken(any()))
			.willReturn("refresh-token");

		LoginAccountDto.Request request = new LoginAccountDto.Request("example@email.com", "password");

		//when
		TokenDto.Response response = accountService.loginAccount(request);

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
			() -> accountService.loginAccount(LoginAccountDto.Request.builder()
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
			() -> accountService.loginAccount(LoginAccountDto.Request.builder()
				.email("example@email.com")
				.password("password").build()));
		//then
		assertEquals(new CustomException(LOGIN_FALSE_NOT_EXIST_EMAIL).getCustomErrorCode(), exception.getCustomErrorCode());
	}

	@Test
	void testNotCorrectPasswordInLogin() throws Exception{
		//given
		given(accountRepository.findByEmail(anyString()))
			.willReturn(Optional.of(Account.builder()
				.password("password")
				.build()));
		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> accountService.loginAccount(LoginAccountDto.Request.builder()
				.email("example@email.com")
				.password("NotCorrectPassword").build()));
		//then
		assertEquals(new CustomException(LOGIN_FALSE_NOT_CORRECT_PASSWORD).getCustomErrorCode(), exception.getCustomErrorCode());
	}

	@Test
	void testNotCorrectStatusInLogin() throws Exception{
		//given
		given(accountRepository.findByEmail(anyString()))
			.willReturn(Optional.of(Account.builder()
				.password("password")
				.status("REQ")
				.build()));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(true);
		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> accountService.loginAccount(LoginAccountDto.Request.builder()
				.email("example@email.com")
				.password("password").build()));
		//then
		assertEquals(new CustomException(REQ_EMAIL).getCustomErrorCode(), exception.getCustomErrorCode());
	}

	@Test
	void testAuthEmail() throws Exception{
		//given
		given(accountRepository.findByEmailAuthKey(anyString()))
			.willReturn(Optional.of(Account.builder()
				.status(ACCOUNT_STATUS_ING)
				.build()));
		given(accountRepository.save(any()))
			.willReturn(Account.builder()
				.status(ACCOUNT_STATUS_ING)
				.build());

		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

		//when
		accountService.authEmail("email-auth-key");

		//then
		verify(accountRepository, times(1)).save(captor.capture());
		assertEquals(captor.getValue().getStatus(), ACCOUNT_STATUS_ING);
	}

	@Test
	void testNotExistUuid() throws Exception{
		//given
		given(accountRepository.findByEmailAuthKey(anyString()))
			.willReturn(Optional.empty());

		//when
		CustomException exception = assertThrows(CustomException.class,
			() -> accountService.authEmail("email-auth-key"));

		//then
		assertEquals(exception.getCustomErrorCode(), NOT_EXIST_UUID);
	}

	@Test
	void testReadAccountInfo() throws Exception{
		//given
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken("username", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))));

		given(accountRepository.findOneByEmail(anyString()))
			.willReturn(Optional.of(
				Account.builder()
					.userId(3L)
					.areaId(10L)
					.email("example@email.com")
					.password("password")
					.userName("name")
					.phone("010-1111-2222")
					.status("ING")
					.emailAuthKey("auth-key")
					.admin(false)
					.createdDt(LocalDateTime.now().minusDays(2))
					.updatedDt(LocalDateTime.now())
					.deleteDt(null)
					.build()));

		given(accountRepository.findById(any()))
			.willReturn(Optional.of(Account.builder()
				.areaId(300L)
				.email("example@email.com")
				.userName("name")
				.phone("010-1111-2222")
				.build()));

		//when
		ReadAccountDto readAccountDto = accountService.readAccountInfo();
		//then
		assertEquals(300, readAccountDto.getAreaId());
		assertEquals("example@email.com", readAccountDto.getEmail());
		assertEquals("name", readAccountDto.getUserName());
		assertEquals("010-1111-2222", readAccountDto.getPhone());
	}

	@Test
	@WithMockUser
	void testChangeAccountInfo() throws Exception{
		//given
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken("username", "password", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))));

		given(accountRepository.findOneByEmail(anyString()))
			.willReturn(Optional.of(
				Account.builder()
					.userId(3L)
					.areaId(10L)
					.email("example@email.com")
					.password("password")
					.userName("name")
					.phone("010-1111-2222")
					.status("ING")
					.emailAuthKey("auth-key")
					.admin(false)
					.createdDt(LocalDateTime.now().minusDays(2))
					.updatedDt(LocalDateTime.now())
					.deleteDt(null)
					.build()));

		given(accountRepository.save(any()))
			.willReturn(Account.builder()
					.userId(3L)
					.areaId(10L)
					.email("example@email.com")
					.password("password")
					.userName("name")
					.phone("010-1111-2222")
					.status("ING")
					.emailAuthKey("auth-key")
					.admin(false)
					.createdDt(LocalDateTime.now().minusDays(2))
					.updatedDt(LocalDateTime.now())
					.deleteDt(null)
					.build());

		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

		//when
		accountService.changeAccountInfo(ChangeAccountDto.Request
										.builder()
										.areaId(200L)
										.userName("name")
										.phone("010-1111-2222")
										.build());

		//then
		verify(accountRepository, times(1)).save(captor.capture());
		assertEquals(captor.getValue().getUserId(), 3L);
	}

	@Test
	void test() throws Exception{
		//given
		//when
		//then
	}
}