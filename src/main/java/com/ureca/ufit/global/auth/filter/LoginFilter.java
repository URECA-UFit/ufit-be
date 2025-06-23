package com.ureca.ufit.global.auth.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.ufit.domain.user.dto.request.LoginRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginFilter extends AbstractAuthenticationProcessingFilter {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public LoginFilter() {
		super(new AntPathRequestMatcher("/api/auth/login", "POST"));
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
		HttpServletResponse response)
		throws AuthenticationException, IOException {

		LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
		String email = loginRequest.email();
		String password = loginRequest.password();

		Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);


		return this.getAuthenticationManager().authenticate(authentication);
	}
}
