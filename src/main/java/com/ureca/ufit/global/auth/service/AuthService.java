package com.ureca.ufit.global.auth.service;

import static com.ureca.ufit.global.auth.util.JwtUtil.*;

import javax.crypto.SecretKey;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ureca.ufit.entity.RefreshToken;
import com.ureca.ufit.global.auth.repository.RefreshTokenRepository;
import com.ureca.ufit.global.auth.util.JwtUtil;
import com.ureca.ufit.global.exception.CommonErrorCode;
import com.ureca.ufit.global.exception.RestApiException;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final SecretKey secretKey;
	private final RedisTemplate<String, String> redisTemplate;

	public void reissueToken(String bearerToken, String refreshToken, HttpServletResponse response) {

		String accessToken = bearerToken.substring(BEARER_PREFIX.length());

		if (redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken)) {
			throw new RestApiException(CommonErrorCode.INVALID_TOKEN);
		}

		RefreshToken refreshTokenEntity = refreshTokenRepository.findById(refreshToken).orElseThrow(() ->
			new RestApiException(CommonErrorCode.REFRESH_NOT_FOUND)
		);

		String email = JwtUtil.getEmailOnlyIfExpired(accessToken, secretKey);

		if (!email.equals(refreshTokenEntity.getEmail())) {
			throw new RestApiException(CommonErrorCode.REFRESH_DENIED);
		}

		JwtUtil.validateRefreshToken(refreshToken, secretKey);

		String newRefreshToken = JwtUtil.createRefreshToken(email, secretKey);
		RefreshToken newRefreshTokenEntity = RefreshToken.of(newRefreshToken, email);
		refreshTokenRepository.deleteById(refreshToken);
		refreshTokenRepository.save(newRefreshTokenEntity);
		JwtUtil.updateRefreshTokenCookie(response, newRefreshToken, REFRESH_TOKEN_EXPIRED_MS / 1000);

		String newAccessToken = JwtUtil.createAccessToken(email, secretKey);
		response.setHeader(AUTH_HEADER, BEARER_PREFIX + newAccessToken);
	}

}