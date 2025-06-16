package com.ureca.ufit.common.support;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.ufit.common.fixture.UserFixture;
import com.ureca.ufit.domain.user.repository.UserRepository;
import com.ureca.ufit.entity.User;
import com.ureca.ufit.entity.enums.Role;
import com.ureca.ufit.global.auth.util.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class ApiSupport extends TestContainerSupport {

	private static final String BEARER = "Bearer ";
	protected User loginAdmin;
	protected User loginUser;
	protected String accessTokenOfUser;
	protected String accessTokenOfAdmin;
	@Autowired
	protected UserRepository userRepository;
	@Autowired
	protected SecretKey secretKey;
	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;

	protected String toJson(Object object) throws JsonProcessingException {
		return objectMapper.writeValueAsString(object);
	}

	public void setUpUser() {
		if (loginAdmin != null && loginUser != null) {
			return;
		}

		this.loginAdmin = userRepository.save(UserFixture.user("admin@naver.com", Role.ADMIN));
		this.loginUser = userRepository.save(UserFixture.user("user@naver.com", Role.USER));

		this.accessTokenOfAdmin = BEARER + JwtUtil.createAccessToken(loginAdmin.getEmail(), secretKey);
		this.accessTokenOfUser = BEARER + JwtUtil.createAccessToken(loginUser.getEmail(), secretKey);
	}

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		setUpUser();
	}
}
