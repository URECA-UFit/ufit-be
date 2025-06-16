package com.ureca.ufit.admin.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.ureca.ufit.common.fixture.RatePlanFixture;
import com.ureca.ufit.common.fixture.UserFixture;
import com.ureca.ufit.common.support.DataMongoSupport;
import com.ureca.ufit.common.support.TestContainerSupport;
import com.ureca.ufit.domain.admin.dto.response.RatePlanMetricsItem;
import com.ureca.ufit.domain.admin.dto.response.RatePlanMetricsResponse;
import com.ureca.ufit.domain.admin.service.AdminService;
import com.ureca.ufit.domain.rateplan.repository.RatePlanRepository;
import com.ureca.ufit.domain.user.repository.UserRepository;
import com.ureca.ufit.entity.RatePlan;
import com.ureca.ufit.entity.User;
import com.ureca.ufit.entity.enums.Gender;
import com.ureca.ufit.entity.enums.Role;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminServiceTest extends TestContainerSupport {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RatePlanRepository ratePlanRepository;

	@Autowired
	private AdminService adminService;

	@AfterEach
	void tearDown() {
		ratePlanRepository.deleteAll();
	}

	@BeforeEach
	void setup() {
		mongoTemplate.dropCollection(RatePlan.class);
		List<RatePlan> savedPlans = IntStream.rangeClosed(1, 10)
			.mapToObj(i -> RatePlanFixture.ratePlan("요금제" + i, 10000 + i * 1000))
			.map(mongoTemplate::save)
			.toList();

		for (int i = 0; i < 10; i++) {
			String planId = savedPlans.get(i).getId();
			int userCount = (i == 0) ? 5 : 1;

			for (int j = 0; j < userCount; j++) {
				User user = UserFixture.createUser(
					"user_" + i + "_" + j + "@example.com",
					"pw",
					20 + j,
					0,
					Gender.MAN,
					Role.USER,
					planId
				);
				userRepository.save(user);
			}
		}

	}


	@DisplayName("가입자 순으로 요금제 이름을 조회한다")
	@Test
	void getRatePlanMetrics(){

		// when
		RatePlanMetricsResponse response = adminService.getRatePlanMetrics(1, 10);

		List<RatePlanMetricsItem> items = response.item();

		// then
		assertThat(items).isSortedAccordingTo(
			Comparator.comparingInt(RatePlanMetricsItem::popularity).reversed()
		);
	}
}
