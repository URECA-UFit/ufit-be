package com.ureca.ufit.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ureca.ufit.common.fixture.UserFixture;
import com.ureca.ufit.domain.user.dto.request.RegisterRequest;
import com.ureca.ufit.domain.user.dto.response.RegisterResponse;
import com.ureca.ufit.domain.user.repository.UserRepository;
import com.ureca.ufit.domain.user.service.UserService;
import com.ureca.ufit.entity.User;
import com.ureca.ufit.entity.enums.Gender;
import com.ureca.ufit.entity.enums.Role;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	UserRepository userRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@InjectMocks
	UserService userService;

	@DisplayName("사용자/관리자는 정상적으로 회원가입할 수 있다.")
	@Test
	void registerTest() {
		RegisterRequest request = new RegisterRequest(
			"test@email.com", "test123!@#",
			28, 175,
			Gender.MAN, Role.USER, "1"
		);

		String encodedPassword = "encoded_password";
		when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);

		User savedUser = UserFixture.createUser(
			request.email(), encodedPassword,
			request.age(), request.family(),
			request.gender(), request.role(), request.ratePlanId()
		);
		when(userRepository.save(Mockito.any(User.class))).thenReturn(savedUser);

		RegisterResponse response = userService.register(request);

		assertThat(response.success()).isTrue();
	}
}
