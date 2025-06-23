package com.ureca.ufit.global.auth.handler;

import static com.ureca.ufit.global.auth.util.JwtUtil.*;

import java.io.IOException;

import javax.crypto.SecretKey;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.ufit.domain.user.dto.response.LoginResponse;
import com.ureca.ufit.entity.RefreshToken;
import com.ureca.ufit.global.auth.details.CustomUserDetails;
import com.ureca.ufit.global.auth.repository.RefreshTokenRepository;
import com.ureca.ufit.global.auth.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final SecretKey secretKeyKey;
	private final RefreshTokenRepository refreshTokenRepository;
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {


		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();


		String accessToken = JwtUtil.createAccessToken(userDetails.email(), secretKeyKey);
		String refreshToken = JwtUtil.createRefreshToken(userDetails.email(), secretKeyKey);


		RefreshToken refreshTokenEntity = RefreshToken.of(refreshToken, userDetails.email());
		refreshTokenRepository.save(refreshTokenEntity);


		JwtUtil.updateRefreshTokenCookie(response, refreshToken, REFRESH_TOKEN_EXPIRED_MS / 1000);


		response.setHeader(AUTH_HEADER, BEARER_PREFIX + accessToken);


		LoginResponse loginResponse = LoginResponse.of(userDetails.getUsername(), userDetails.role());
		objectMapper.writeValue(response.getWriter(), loginResponse);
	}

}
