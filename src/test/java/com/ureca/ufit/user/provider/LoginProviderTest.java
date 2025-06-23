package com.ureca.ufit.user.provider;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ureca.ufit.global.auth.provider.LoginProvider;
import com.ureca.ufit.global.auth.service.CustomUserDetailsService;

@ExtendWith(MockitoExtension.class)
class LoginProviderTest {

	private final String EMAIL = "test@email.com";
	private final String RAW_PASSWORD = "password123";
	private final String ENCODED_PASSWORD = "encoded_password";

	@Mock
	PasswordEncoder passwordEncoder;

	@Mock
	CustomUserDetailsService userDetailsService;

	@InjectMocks
	LoginProvider loginProvider;

	@DisplayName("정상 로그인 시 Authentication 객체를 반환한다.")
	@Test
	void authenticateSuccess() {

		UserDetails userDetails = User.withUsername(EMAIL)
			.password(ENCODED_PASSWORD)
			.roles("USER")
			.build();

		UsernamePasswordAuthenticationToken token =
			new UsernamePasswordAuthenticationToken(EMAIL, RAW_PASSWORD);

		when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
		when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

		Authentication result = loginProvider.authenticate(token);

		assertThat(result.isAuthenticated()).isTrue();
		assertThat(result.getPrincipal()).isEqualTo(userDetails);
	}

}
