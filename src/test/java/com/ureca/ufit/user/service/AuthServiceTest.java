package com.ureca.ufit.user.service;

import static com.ureca.ufit.global.auth.util.JwtUtil.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import com.ureca.ufit.entity.RefreshToken;
import com.ureca.ufit.global.auth.repository.RefreshTokenRepository;
import com.ureca.ufit.global.auth.service.AuthService;
import com.ureca.ufit.global.auth.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private SecretKey secretKey;

	@Mock
	private HttpServletResponse response;

	@InjectMocks
	private AuthService authService;

	@DisplayName("토큰 재발급시 어세스토큰과 리프레시토큰이 모두 재발급 된다.(RTR)")
	@Test
	void reissueTokenSuccess() {
		String email = "user@example.com";
		String accessToken = "access.token.value";
		String bearerToken = BEARER_PREFIX + accessToken;
		String refreshToken = "refresh.token.value";
		String newRefreshToken = "new.refresh.token";
		String newAccessToken = "new.access.token";

		RefreshToken refreshTokenEntity = RefreshToken.of(refreshToken, email);

		when(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken)).thenReturn(false);
		when(refreshTokenRepository.findById(refreshToken)).thenReturn(Optional.of(refreshTokenEntity));

		try (MockedStatic<JwtUtil> mocked = mockStatic(JwtUtil.class)) {
			mocked.when(() -> JwtUtil.getEmailOnlyIfExpired(accessToken, secretKey)).thenReturn(email);
			mocked.when(() -> JwtUtil.validateRefreshToken(refreshToken, secretKey)).thenAnswer(inv -> null);
			mocked.when(() -> JwtUtil.createRefreshToken(email, secretKey)).thenReturn(newRefreshToken);
			mocked.when(() -> JwtUtil.createAccessToken(email, secretKey)).thenReturn(newAccessToken);
			mocked.when(
					() -> JwtUtil.updateRefreshTokenCookie(response, newRefreshToken, REFRESH_TOKEN_EXPIRED_MS / 1000))
				.thenCallRealMethod();

			authService.reissueToken(bearerToken, refreshToken, response);

			verify(refreshTokenRepository).deleteById(refreshToken);
			ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
			verify(refreshTokenRepository).save(captor.capture());
			RefreshToken saved = captor.getValue();
			assertThat(saved.getEmail()).isEqualTo(email);
			verify(response).setHeader(AUTH_HEADER, BEARER_PREFIX + newAccessToken);
		}
	}
}
