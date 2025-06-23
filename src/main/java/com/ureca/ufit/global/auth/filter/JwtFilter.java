package com.ureca.ufit.global.auth.filter;

import static com.ureca.ufit.global.auth.util.JwtUtil.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ureca.ufit.global.auth.service.CustomUserDetailsService;
import com.ureca.ufit.global.auth.util.JwtUtil;
import com.ureca.ufit.global.exception.CommonErrorCode;
import com.ureca.ufit.global.exception.RestApiException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {


	private static final List<String> WHITE_LIST = List.of(
		"/error", "/favicon.ico", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
		"/api/auth/login",
		"/api/users/register",
		"/api/rateplans/storages",
		"/api/rateplans/storages/{rateplanId}",
		"/api/auth/reissue/token",
		"/api/auth/logout",
		"/actuator/health/**",
		"/actuator/prometheus",
		"/actuator/info"
	);


	private static final List<String> PUBLIC_LIST = List.of(
		"/api/chats/message",
		"/api/chats/review",
		"/api/chats",
		"/api/chats/rooms"
	);

	private static final AntPathMatcher matcher = new AntPathMatcher();

	private final CustomUserDetailsService userDetailsService;
	private final RedisTemplate<String, String> redisTemplate;
	private final SecretKey secretKey;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		boolean isPublic = PUBLIC_LIST.stream().anyMatch(pub -> matcher.match(pub, request.getRequestURI()));


		String bearerToken = request.getHeader(AUTH_HEADER);


		if (Optional.ofNullable(bearerToken).isEmpty()) {
			if (!isPublic) {
				throw new RestApiException(CommonErrorCode.NOT_EXIST_BEARER_SUFFIX);
			}
			filterChain.doFilter(request, response);
			return;
		}


		String accessToken = Optional.of(bearerToken)
			.filter(token -> token.startsWith(BEARER_PREFIX))
			.map(token -> token.substring(BEARER_PREFIX.length()))
			.orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_EXIST_BEARER_SUFFIX));


		JwtUtil.validateAccessToken(accessToken, secretKey);
		if (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken))) {
			throw new RestApiException(CommonErrorCode.INVALID_TOKEN);
		}


		String email = JwtUtil.getEmail(accessToken, secretKey);
		UserDetails userDetails = userDetailsService.loadUserByUsername(email);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return WHITE_LIST.stream().anyMatch(white -> matcher.match(white, request.getRequestURI()));
	}
}
