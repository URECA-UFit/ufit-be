package com.ureca.ufit.user.filter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.ufit.domain.user.dto.request.LoginRequest;
import com.ureca.ufit.global.auth.filter.LoginFilter;

import jakarta.servlet.ServletException;

class LoginFilterTest {

	private LoginFilter loginFilter;
	private AuthenticationManager authenticationManager;

	@BeforeEach
	void setUp() {
		authenticationManager = mock(AuthenticationManager.class);
		loginFilter = new LoginFilter();
		loginFilter.setAuthenticationManager(authenticationManager);
	}

	@DisplayName("로그인 요청을 정상적으로 파싱하고 인증을 위임한다")
	@Test
	void loginFilterTest() throws IOException, ServletException {
		LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
		ObjectMapper objectMapper = new ObjectMapper();
		byte[] requestBody = objectMapper.writeValueAsBytes(loginRequest);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		request.setRequestURI("/api/auth/login");
		request.setContentType("application/json");
		request.setContent(requestBody);

		MockHttpServletResponse response = new MockHttpServletResponse();

		Authentication dummyAuth = mock(Authentication.class);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
			.thenReturn(dummyAuth);

		Authentication result = loginFilter.attemptAuthentication(request, response);

		assertThat(result).isNotNull();
		verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
	}
}
