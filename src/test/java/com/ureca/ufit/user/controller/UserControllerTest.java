package com.ureca.ufit.user.controller;

import static com.ureca.ufit.global.auth.util.JwtUtil.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Objects;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.ResultActions;

import com.ureca.ufit.common.support.ApiSupport;
import com.ureca.ufit.domain.user.controller.UserController;
import com.ureca.ufit.domain.user.dto.request.LoginRequest;
import com.ureca.ufit.domain.user.dto.request.RegisterRequest;
import com.ureca.ufit.domain.user.exception.UserErrorCode;
import com.ureca.ufit.domain.user.repository.UserRepository;
import com.ureca.ufit.entity.enums.Gender;
import com.ureca.ufit.entity.enums.Role;
import com.ureca.ufit.global.auth.repository.RefreshTokenRepository;
import com.ureca.ufit.global.auth.util.JwtUtil;
import com.ureca.ufit.global.exception.CommonErrorCode;

import jakarta.servlet.http.Cookie;

public class UserControllerTest extends ApiSupport {

	private final String email = "test@email.com";
	private final String password = "test123!@#";
	private final String rateId = "1";

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private UserController userController;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SecretKey secretKey;

	@BeforeEach
	void setUp() {
		RegisterRequest registerRequest = new RegisterRequest(
			email, password, 25, 175, Gender.MAN, Role.USER, rateId);

		userController.register(registerRequest);
	}

	@AfterEach
	void cleanUp() {
		userRepository.deleteAll();
		refreshTokenRepository.deleteAll();
		redisTemplate.delete(redisTemplate.keys("*"));
		SecurityContextHolder.clearContext();
	}

	@DisplayName("사용자/관리자가 로그인 하면 refreshToken 및 accessToken이 발급된다.")
	@Test
	void loginTest() throws Exception {
		LoginRequest loginRequest = new LoginRequest(email, password);

		ResultActions result = mockMvc.perform(post("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(toJson(loginRequest)));

		result.andExpect(status().isOk())
			.andExpect(header().exists(AUTH_HEADER))
			.andExpect(header().string(AUTH_HEADER, startsWith(BEARER_PREFIX)))
			.andExpect(cookie().exists(REFRESH_TOKEN_COOKIE_NAME))
			.andExpect(cookie().httpOnly(REFRESH_TOKEN_COOKIE_NAME, true))
			.andExpect(cookie().secure(REFRESH_TOKEN_COOKIE_NAME, true));
	}

	@DisplayName("존재하지 않는 이메일로 로그인 시도 시 404을 반환한다.")
	@Test
	void loginWithNonexistentEmail() throws Exception {
		LoginRequest request = new LoginRequest("nonexistent@email.com", "password");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(request))
			)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code")
				.value(UserErrorCode.USER_NOT_FOUND.name()))
			.andExpect(jsonPath("$.message")
				.value(UserErrorCode.USER_NOT_FOUND.getMessage())
			);
	}

	@DisplayName("잘못된 비밀번호로 로그인 시도 시 400을 반환한다.")
	@Test
	void loginWithWrongPassword() throws Exception {
		LoginRequest request = new LoginRequest("test@email.com", "wrongPassword");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code")
				.value(UserErrorCode.USER_PASSWORD_MISMATCH.name()))
			.andExpect(jsonPath("$.message")
				.value(UserErrorCode.USER_PASSWORD_MISMATCH.getMessage())
			);
	}

	@DisplayName("로그인 후 로그아웃하면 refreshToken 삭제 및 accessToken 블랙리스트 처리된다.")
	@Test
	void logoutTest() throws Exception {
		LoginRequest loginRequest = new LoginRequest(email, password);

		var loginResult = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(loginRequest)))
			.andExpect(status().isOk())
			.andReturn();

		String refreshToken = Objects.requireNonNull(loginResult.getResponse().getCookie(REFRESH_TOKEN_COOKIE_NAME))
			.getValue();
		String bearerToken = loginResult.getResponse().getHeader(AUTH_HEADER);

		mockMvc.perform(post("/api/auth/logout")
				.header(AUTH_HEADER, bearerToken)
				.cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken)))
			.andExpect(status().isOk())
			.andExpect(cookie().maxAge(REFRESH_TOKEN_COOKIE_NAME, 0));

		String accessToken = Objects.requireNonNull(bearerToken).substring(7);
		assertThat(refreshTokenRepository.findById(refreshToken)).isEmpty();
		assertThat(redisTemplate.opsForValue().get(BLACKLIST_PREFIX + accessToken)).isEqualTo("logout");
	}

	@DisplayName("Authorization 헤더 없이 로그아웃 시도 시 401을 반환한다.")
	@Test
	void logoutWithoutAuthorizationHeader() throws Exception {
		mockMvc.perform(post("/api/auth/logout"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(CommonErrorCode.NOT_EXIST_BEARER_SUFFIX.name()))
			.andExpect(jsonPath("$.message").value(CommonErrorCode.NOT_EXIST_BEARER_SUFFIX.getMessage()));
	}

	@DisplayName("유효하지 않은 accessToken으로 로그아웃하면 401 반환")
	@Test
	void logoutWithInvalidAccessToken() throws Exception {
		mockMvc.perform(post("/api/auth/logout")
				.header(AUTH_HEADER, "Bearer invalid.token.here")
				.cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, "dummy")))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(CommonErrorCode.INVALID_TOKEN.name()))
			.andExpect(jsonPath("$.message").value(CommonErrorCode.INVALID_TOKEN.getMessage()));
	}

	@DisplayName("accessToken이 만료되지 않으면 재발급 요청 시 예외가 발생한다.")
	@Test
	void reissueFailsIfAccessTokenIsStillValid() throws Exception {
		LoginRequest loginRequest = new LoginRequest(email, password);

		var loginResult = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(loginRequest)))
			.andExpect(status().isOk())
			.andReturn();

		String validAccessToken = loginResult.getResponse().getHeader(AUTH_HEADER);
		String validRefreshToken = loginResult.getResponse().getCookie(REFRESH_TOKEN_COOKIE_NAME).getValue();

		mockMvc.perform(post("/api/auth/reissue/token")
				.header(AUTH_HEADER, validAccessToken)
				.cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, validRefreshToken)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value(CommonErrorCode.REFRESH_DENIED.name()))
			.andExpect(jsonPath("$.message").value(CommonErrorCode.REFRESH_DENIED.getMessage()));
	}

	@DisplayName("Authorization 헤더가 없으면 400을 반환한다")
	@Test
	void reissueFailsWithoutAuthorizationHeader() throws Exception {
		mockMvc.perform(post("/api/auth/reissue/token")
				.cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, "dummy")))
			.andExpect(status().isBadRequest());
	}

	@DisplayName("accessToken이 만료되었을 경우 refreshToken을 통해 재발급할 수 있다.")
	@Test
	void reissueSucceedsWhenAccessTokenIsExpired() throws Exception {
		LoginRequest loginRequest = new LoginRequest(email, password);

		var loginResult = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(loginRequest)))
			.andExpect(status().isOk())
			.andReturn();

		String refreshToken = loginResult.getResponse().getCookie(REFRESH_TOKEN_COOKIE_NAME).getValue();
		String expiredAccessToken = JwtUtil.createToken(email, "access", secretKey, 1);

		var reissueResult = mockMvc.perform(post("/api/auth/reissue/token")
				.header(AUTH_HEADER, BEARER_PREFIX + expiredAccessToken)
				.cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken)))
			.andExpect(status().isOk())
			.andExpect(header().exists(AUTH_HEADER))
			.andExpect(header().string(AUTH_HEADER, startsWith(BEARER_PREFIX)))
			.andExpect(cookie().exists(REFRESH_TOKEN_COOKIE_NAME))
			.andReturn();

		String newAccessToken = reissueResult.getResponse().getHeader(AUTH_HEADER);
		String newRefreshToken = Objects.requireNonNull(
			reissueResult.getResponse().getCookie(REFRESH_TOKEN_COOKIE_NAME)).getValue();

		assertThat(newAccessToken).isNotEqualTo(expiredAccessToken);
		assertThat(newRefreshToken).isNotEqualTo(refreshToken);
	}

	@DisplayName("Redis에 존재하지 않는 refreshToken으로 재발급 시 404 반환")
	@Test
	void reissueWithTamperedRefreshToken() throws Exception {
		String expiredAccessToken = JwtUtil.createToken(email, "access", secretKey, 1);
		String tamperedRefreshToken = "nonexistent-refresh-token";

		mockMvc.perform(post("/api/auth/reissue/token")
				.header(AUTH_HEADER, BEARER_PREFIX + expiredAccessToken)
				.cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, tamperedRefreshToken)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(CommonErrorCode.REFRESH_NOT_FOUND.name()))
			.andExpect(jsonPath("$.message").value(CommonErrorCode.REFRESH_NOT_FOUND.getMessage()));
	}

	@DisplayName("블랙리스트에 등록된 accessToken으로 재발급 시도 시 401 반환")
	@Test
	void reissueWithBlacklistedAccessToken() throws Exception {
		LoginRequest loginRequest = new LoginRequest(email, password);
		var loginResult = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(loginRequest)))
			.andReturn();

		String accessToken = loginResult.getResponse().getHeader(AUTH_HEADER).substring(7);
		String refreshToken = loginResult.getResponse().getCookie(REFRESH_TOKEN_COOKIE_NAME).getValue();

		redisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, "logout");

		mockMvc.perform(post("/api/auth/reissue/token")
				.header(AUTH_HEADER, BEARER_PREFIX + accessToken)
				.cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(CommonErrorCode.INVALID_TOKEN.name()));
	}

}
