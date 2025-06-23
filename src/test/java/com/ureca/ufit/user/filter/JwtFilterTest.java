package com.ureca.ufit.user.filter;

import static com.ureca.ufit.global.auth.util.JwtUtil.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.ureca.ufit.global.auth.filter.JwtFilter;
import com.ureca.ufit.global.auth.service.CustomUserDetailsService;
import com.ureca.ufit.global.auth.util.JwtUtil;
import com.ureca.ufit.global.exception.CommonErrorCode;
import com.ureca.ufit.global.exception.RestApiException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

class JwtFilterTest {

	private JwtFilter jwtFilter;
	private RedisTemplate<String, String> redisTemplate;
	private CustomUserDetailsService userDetailsService;
	private SecretKey secretKey;

	@BeforeEach
	void setUp() {
		redisTemplate = mock(RedisTemplate.class);
		userDetailsService = mock(CustomUserDetailsService.class);
		secretKey = mock(SecretKey.class);

		jwtFilter = new JwtFilter(userDetailsService, redisTemplate, secretKey);
	}

	@DisplayName("화이트리스트 URI는 필터링 하지 않는다")
	@Test
	void whiteListFilteringTest() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = mock(FilterChain.class);

		jwtFilter.doFilter(request, response, filterChain);

		verify(filterChain, times(1)).doFilter(request, response);
	}

	@DisplayName("유효한 토큰이 존재하면 인증객체를 설정한다")
	@Test
	void validTokenTest() throws Exception {
		String email = "user@example.com";
		String validToken = "valid.token.value";

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
		request.addHeader(AUTH_HEADER, BEARER_PREFIX + validToken);
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = mock(FilterChain.class);

		UserDetails mockUser = new User(email, "", Collections.emptyList());

		when(redisTemplate.hasKey("blacklist:" + validToken)).thenReturn(false);
		when(userDetailsService.loadUserByUsername(email)).thenReturn(mockUser);

		try (MockedStatic<JwtUtil> mockedJwtUtil = mockStatic(JwtUtil.class)) {
			mockedJwtUtil
				.when(() -> JwtUtil.validateAccessToken(validToken, secretKey))
				.thenCallRealMethod();

			mockedJwtUtil.when(() -> JwtUtil.validateAccessToken(validToken, secretKey)).thenAnswer(inv -> null);

			mockedJwtUtil.when(() -> JwtUtil.getEmail(validToken, secretKey)).thenReturn(email);

			jwtFilter.doFilter(request, response, filterChain);

			verify(userDetailsService).loadUserByUsername(email);
			verify(filterChain).doFilter(request, response);
		}
	}

	@DisplayName("토큰이 없고 Public이 아니면 예외가 발생한다.")
	@Test
	void isNotPublicTest() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = mock(FilterChain.class);

		assertThatThrownBy(() -> jwtFilter.doFilter(request, response, filterChain))
			.isInstanceOf(RestApiException.class)
			.hasMessageContaining(CommonErrorCode.NOT_EXIST_BEARER_SUFFIX.getMessage());
	}
}
