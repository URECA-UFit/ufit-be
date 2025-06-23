package com.ureca.ufit.global.auth.util;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.ureca.ufit.global.exception.CommonErrorCode;
import com.ureca.ufit.global.exception.RestApiException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtil {

	public static final String AUTH_HEADER = "Authorization";
	public static final String BEARER_PREFIX = "Bearer ";
	public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
	public static final String REFRESH_TOKEN_PREFIX = "refreshToken:";
	public static final String BLACKLIST_PREFIX = "blackList:";

	public static final String JWT_CLAIM_KEY_EMAIL = "email";
	public static final String JWT_CLAIM_KEY_TYPE = "type";
	public static final String JWT_CLAIM_KEY_ID = "jit";

	public static final String JWT_CLAIM_VALUE_ACCESS_TYPE = "access";
	public static final String JWT_CLAIM_VALUE_REFRESH_TYPE = "refresh";

	public static final String COOKIE_HEADER_NAME = "Set-Cookie";
	public static final String COOKIE_SAME_SITE_STRATEGY = "Lax";

	public static final int ACCESS_TOKEN_EXPIRED_MS = 1000 * 60 * 30;
	public static final int REFRESH_TOKEN_EXPIRED_MS = 1000 * 60 * 60 * 24 * 3;

	public static String createToken(String email, String type, SecretKey secretKey, long expiresIn) {
		return Jwts.builder()
			.claim(JWT_CLAIM_KEY_EMAIL, email)
			.claim(JWT_CLAIM_KEY_TYPE, type)
			.claim(JWT_CLAIM_KEY_ID, UUID.randomUUID().toString())
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + expiresIn))
			.signWith(secretKey)
			.compact();
	}

	public static String createAccessToken(String email, SecretKey secretKey) {
		return createToken(email, JWT_CLAIM_VALUE_ACCESS_TYPE, secretKey, ACCESS_TOKEN_EXPIRED_MS);
	}

	public static String createRefreshToken(String email, SecretKey secretKey) {
		return createToken(email, JWT_CLAIM_VALUE_REFRESH_TYPE, secretKey, REFRESH_TOKEN_EXPIRED_MS);
	}

	public static String getEmail(String token, SecretKey secretKey) {
		return parseClaims(token, secretKey).get(JWT_CLAIM_KEY_EMAIL, String.class);
	}

	private static String getType(String token, SecretKey secretKey) {
		return parseClaims(token, secretKey).get(JWT_CLAIM_KEY_TYPE, String.class);
	}

	public static Date getExpiration(String token, SecretKey secretKey) {
		return parseClaims(token, secretKey).getExpiration();
	}

	private static Claims parseClaims(String token, SecretKey secretKey) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public static void validateAccessToken(String token, SecretKey secretKey) {
		validateToken(token, secretKey, JWT_CLAIM_VALUE_ACCESS_TYPE, CommonErrorCode.EXPIRED_TOKEN);
	}

	public static void validateRefreshToken(String token, SecretKey secretKey) {
		validateToken(token, secretKey, JWT_CLAIM_VALUE_REFRESH_TYPE, CommonErrorCode.REFRESH_DENIED);
	}

	private static void validateToken(String token, SecretKey secretKey, String expectedType,
		CommonErrorCode expiredErrorCode) {
		try {
			Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token);

			String type = getType(token, secretKey);
			if (!expectedType.equals(type)) {
				throw new RestApiException(CommonErrorCode.INVALID_TOKEN);
			}
		} catch (SecurityException | MalformedJwtException e) {
			throw new RestApiException(CommonErrorCode.INVALID_TOKEN);
		} catch (ExpiredJwtException e) {
			throw new RestApiException(expiredErrorCode);
		} catch (UnsupportedJwtException e) {
			throw new RestApiException(CommonErrorCode.UNSUPPORTED_TOKEN);
		} catch (IllegalArgumentException e) {
			throw new RestApiException(CommonErrorCode.ILLEGAL_TOKEN);
		} catch (Exception e) {
			throw new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public static String getEmailOnlyIfExpired(String token, SecretKey secretKey) {
		try {
			parseClaims(token, secretKey);
			throw new RestApiException(CommonErrorCode.REFRESH_DENIED);
		} catch (RestApiException e) {
			throw e;
		} catch (ExpiredJwtException e) {
			return e.getClaims().get(JWT_CLAIM_KEY_EMAIL, String.class);
		} catch (SecurityException | MalformedJwtException e) {
			throw new RestApiException(CommonErrorCode.INVALID_TOKEN);
		} catch (UnsupportedJwtException e) {
			throw new RestApiException(CommonErrorCode.UNSUPPORTED_TOKEN);
		} catch (IllegalArgumentException e) {
			throw new RestApiException(CommonErrorCode.ILLEGAL_TOKEN);
		} catch (Exception e) {
			throw new RestApiException(CommonErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public static void updateRefreshTokenCookie(HttpServletResponse response, String refreshToken,
		int timeout) {
		ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(Duration.ofSeconds(timeout))
			.sameSite(COOKIE_SAME_SITE_STRATEGY)
			.build();

		response.setHeader(COOKIE_HEADER_NAME, cookie.toString());
	}

	public static String getRefreshTokenCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();

		if (cookies == null)
			throw new RestApiException(CommonErrorCode.REFRESH_NOT_FOUND);

		for (Cookie cookie : cookies) {
			if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}

		throw new RestApiException(CommonErrorCode.REFRESH_NOT_FOUND);
	}

}