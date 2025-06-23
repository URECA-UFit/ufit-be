package com.ureca.ufit.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ureca.ufit.domain.user.dto.UserMapper;
import com.ureca.ufit.domain.user.dto.request.RegisterRequest;
import com.ureca.ufit.domain.user.dto.response.RegisterResponse;
import com.ureca.ufit.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public RegisterResponse register(RegisterRequest request) {
		String encodedPassword = passwordEncoder.encode(request.password());
		userRepository.save(UserMapper.toEntity(request, encodedPassword));
		return new RegisterResponse(true);
	}

}
