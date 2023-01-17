package com.example.secondhand.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.secondhand.domain.user.dto.ChangeAccountDto;
import com.example.secondhand.domain.user.dto.ChangePasswordDto;
import com.example.secondhand.domain.user.dto.CreateAccountDto;
import com.example.secondhand.domain.user.dto.DeleteAccountDto;
import com.example.secondhand.domain.user.dto.LoginAccountDto;
import com.example.secondhand.domain.user.dto.LogoutAccountDto;
import com.example.secondhand.domain.user.dto.ReadAccountDto;
import com.example.secondhand.domain.user.dto.TokenDto;
import com.example.secondhand.domain.user.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
	@MockBean
	private AccountService accountService;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@WithMockUser // 권한 인증된 상태로 테스트 진행 (401에러 방지)
	void testCreateAccount() throws Exception{
		//given
		willDoNothing()
			.given(accountService).createAccount(any());
		//when
		//then
		mockMvc.perform(post("/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.with(SecurityMockMvcRequestPostProcessors.csrf()) //MissingCsrfTokenException 방지(403에러 방지)
				.content(
					objectMapper.writeValueAsString(new CreateAccountDto.Request(200L,"example@email.com", "password","name", "010-0000-1111"))
				))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message.status").value("REGISTER_TRUE"));
	}

	@Test
	@WithMockUser
	void testAuthEmail() throws Exception{
		//given
		willDoNothing()
			.given(accountService).authEmail(anyString());
		//when
		//then
		mockMvc.perform(get("/auth/auth-email?id=emailAuthKey"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message.status").value("CERTIFICATION_EMAIL_TRUE"));
	}

	@Test
	@WithMockUser
	void testLoginAccount() throws Exception{
		//given
		given(accountService.loginAccount(any()))
			.willReturn(TokenDto.Response.builder()
				.grantType("Bearer")
				.accessToken("access-token")
				.refreshToken("refresh-token")
				.build());
		//when
		//then
		mockMvc.perform(post("/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.with(SecurityMockMvcRequestPostProcessors.csrf())
			.content(
				objectMapper.writeValueAsString(new LoginAccountDto.Request("example@email.com", "password"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message.status").value("LOGIN_TRUE"))
			.andExpect(jsonPath("$.data.grantType").value("Bearer"))
			.andExpect(jsonPath("$.data.accessToken").value("access-token"))
			.andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
	}

	@Test
	@WithMockUser
	void testReadAccountInfo() throws Exception{
		//given
		given(accountService.readAccountInfo())
			.willReturn(ReadAccountDto.builder()
				.areaId(300L)
				.email("example@email.com")
				.userName("name")
				.phone("010-1111-2222")
				.build());
		//when
		//then
		mockMvc.perform(get("/auth/")
				.contentType(MediaType.APPLICATION_JSON)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message.status").value("READ_INFO_TRUE"))
				.andExpect(jsonPath("$.data.areaId").value(300))
				.andExpect(jsonPath("$.data.email").value("example@email.com"))
				.andExpect(jsonPath("$.data.userName").value("name"))
				.andExpect(jsonPath("$.data.phone").value("010-1111-2222"));
	}

	@Test
	@WithMockUser
	void testChangeAccountInfo() throws Exception{
		//given
		willDoNothing()
			.given(accountService).changeAccountInfo(any());
		//when
		//then
		mockMvc.perform(patch("/auth/")
			.contentType(MediaType.APPLICATION_JSON)
			.with(SecurityMockMvcRequestPostProcessors.csrf())
			.content(
				objectMapper.writeValueAsString(new ChangeAccountDto.Request(300L, "name", "010-1111-2222"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message.status").value("CHANGE_INFO_TRUE"));
	}

	@Test
	@WithMockUser
	void testLogoutAccount() throws Exception{
		//given
		willDoNothing()
			.given(accountService).logoutAccount(any());
		//when
		//then
		mockMvc.perform(delete("/auth/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.content(
					objectMapper.writeValueAsString(new LogoutAccountDto.Request("access-token"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message.status").value("LOGOUT_TRUE"));
	}

	@Test
	@WithMockUser
	void testChangePassword() throws Exception{
		//given
		willDoNothing()
			.given(accountService).changePassword(any());
		//when
		//then
		mockMvc.perform(patch("/auth/password")
				.contentType(MediaType.APPLICATION_JSON)
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.content(
					objectMapper.writeValueAsString(new ChangePasswordDto.Request("example@email.com", "password", "newPassword"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message.status").value("PASSWORD_CHANGE_TRUE"));
	}

	@Test
	@WithMockUser
	void testChangeLostPassword() throws Exception{
		//given
		willDoNothing()
			.given(accountService).changeLostPassword(any());
		//when
		//then
		mockMvc.perform(put("/auth/password")
				.contentType(MediaType.APPLICATION_JSON)
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.content(
					objectMapper.writeValueAsString(new ChangePasswordDto.LostRequest("example@email.com", "newPassword"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message.status").value("PASSWORD_CHANGE_TRUE"));
	}

	@Test
	@WithMockUser
	void testDeleteAccount() throws Exception{
		//given
		willDoNothing()
			.given(accountService).deleteAccount(any());
		//when
		//then
		mockMvc.perform(delete("/auth/")
				.contentType(MediaType.APPLICATION_JSON)
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.content(
					objectMapper.writeValueAsString(new DeleteAccountDto.Request("password"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message.status").value("DELETE_ACCOUNT_TRUE"));
	}

	@Test
	@WithMockUser
	void testReissue() throws Exception{
		//given
		given(accountService.reissue(any()))
			.willReturn(TokenDto.Response.builder()
				.grantType("Bearer")
				.accessToken("access-token")
				.refreshToken("refresh-token")
				.build());
		//when
		//then
		mockMvc.perform(get("/auth")
				.contentType(MediaType.APPLICATION_JSON)
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.content(
					objectMapper.writeValueAsString(new TokenDto.Request("refresh-token"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(200))
			.andExpect(jsonPath("$.message.status").value("TOKEN_REISSUE_TRUE"))
			.andExpect(jsonPath("$.data.grantType").value("Bearer"))
			.andExpect(jsonPath("$.data.accessToken").value("access-token"))
			.andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
	}

}