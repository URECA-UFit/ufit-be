package com.ureca.ufit.global.auth.handler;

import static com.ureca.ufit.global.auth.util.JwtUtil.*;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.ureca.ufit.global.auth.repository.RefreshTokenRepository;
import com.ureca.ufit.global.auth.util.JwtUtil;
import com.ureca.ufit.global.auth.util.SendErrorResponseUtil;
import com.ureca.ufit.global.exception.CommonErrorCode;
import com.ureca.ufit.global.exception.RestApiException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

	private final RefreshTokenRepository refreshTokenRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final SecretKey secretKey;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) {

		try {
			String bearerToken = request.getHeader(AUTH_HEADER);
			if (bearerToken == null || !bearerToken.startsWith(BEARER_PREFIX)) {
				throw new RestApiException(CommonErrorCode.NOT_EXIST_BEARER_SUFFIX);
			}
			String accessToken = bearerToken.substring(BEARER_PREFIX.length());

			try {
				JwtUtil.validateAccessToken(accessToken, secretKey);

				addToBlacklistRedis(accessToken);
			} catch (RestApiException e) {
				if( !e.getErrorCode().equals(CommonErrorCode.EXPIRED_TOKEN))
					throw e;
			}

			String	refreshToken = JwtUtil.getRefreshTokenCookies(request);

			JwtUtil.updateRefreshTokenCookie(response, null, 0);


			refreshTokenRepository.delete(
				refreshTokenRepository.findById(refreshToken).orElseThrow( () ->
						new RestApiException(CommonErrorCode.REFRESH_NOT_FOUND)
				)
			);

		} catch (RestApiException e) {

			if(e.getErrorCode().equals(CommonErrorCode.REFRESH_NOT_FOUND))
				return;
			try {
				SendErrorResponseUtil.sendErrorResponse(response, e.getErrorCode());
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private void addToBlacklistRedis(String accessToken) {
		Date expiration = JwtUtil.getExpiration(accessToken, secretKey);
		long ttl = expiration.getTime() - System.currentTimeMillis();
		redisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, "logout", ttl, TimeUnit.MILLISECONDS);
	}
}
