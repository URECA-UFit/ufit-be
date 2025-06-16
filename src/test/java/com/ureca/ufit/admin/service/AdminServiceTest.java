package com.ureca.ufit.admin.service;

import static org.assertj.core.api.Assertions.*;import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ureca.ufit.common.fixture.RatePlanFixture;
import com.ureca.ufit.domain.admin.dto.response.DeleteRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.RatePlanStatusResponse;
import com.ureca.ufit.domain.admin.service.AdminService;
import com.ureca.ufit.domain.chatbot.repository.ChatBotReviewRepository;
import com.ureca.ufit.domain.rateplan.exception.RatePlanErrorCode;
import com.ureca.ufit.domain.rateplan.repository.RatePlanRepository;
import com.ureca.ufit.domain.user.repository.UserRepository;
import com.ureca.ufit.entity.RatePlan;
import com.ureca.ufit.global.exception.RestApiException;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

	private static final String PLAN_ID = "plan-123";

	@Mock
	RatePlanRepository ratePlanRepository;

	@Mock
	UserRepository userRepository;

	@Mock
	ChatBotReviewRepository chatBotReviewRepository;

	@InjectMocks
	AdminService adminService;

	@DisplayName("삭제되지 않고 판매 중인 요금제를 판매 중지시킨다.")
	@Test
	void pauseSalesWhenRatePlanIsSellingAndNotDeleted() {
		// given
		final String id = "dsanfono2n4ioh198h89n";
		RatePlan ratePlan = RatePlanFixture.ratePlan("판매 요금제", true, false);
		ReflectionTestUtils.setField(ratePlan, "id", "dsanfono2n4ioh198h89n");

		RatePlan savedRatePlan = RatePlanFixture.ratePlan("판매 요금제", false, false);
		ReflectionTestUtils.setField(savedRatePlan, "id", "dsanfono2n4ioh198h89n");

		given(ratePlanRepository.getById(anyString())).willReturn(ratePlan);
		given(ratePlanRepository.save(any(RatePlan.class))).willReturn(savedRatePlan);

		// when
		RatePlanStatusResponse response = adminService.updateRatePlanSalesStatus(id);

		// then
		assertAll(
			() -> assertThat(response.isEnabled()).isFalse(),
			() -> assertThat(response.ratePlanId()).isEqualTo(id)
		);
	}

	@DisplayName("삭제된 요금제의 판매 여부를 변경하지 못한다.")
	@Test
	void throwExceptionWhenDeletedRatePlanUpdateSalesStatus() {
		// given
		final String id = "dsanfono2n4ioh198h89n";
		RatePlan ratePlan = RatePlanFixture.ratePlan("삭제된 요금제", true, true);
		ReflectionTestUtils.setField(ratePlan, "id", "dsanfono2n4ioh198h89n");

		given(ratePlanRepository.getById(anyString())).willReturn(ratePlan);

		// when  // then
		assertThatThrownBy(() -> adminService.updateRatePlanSalesStatus(id))
			.isInstanceOf(RestApiException.class)
			.hasMessage(RatePlanErrorCode.RATE_PLAN_ALREADY_DELETED.getMessage());
  }

  	@DisplayName("판매중지이며 가입자가 한 명도 없을 때 요금제를 삭제한다.")
	@Test
	void deleteRatePlan() {
		// given
		RatePlan plan = RatePlanFixture.ratePlan("테스트플랜", false, false);
		ReflectionTestUtils.setField(plan, "id", PLAN_ID);
		given(ratePlanRepository.findById(PLAN_ID)).willReturn(Optional.of(plan));
		given(userRepository.countByRatePlanId(PLAN_ID)).willReturn(0L);

		// when
		DeleteRatePlanResponse response = adminService.deleteRatePlan(PLAN_ID);

		// then
		assertThat(response.message())
			.isEqualTo("요금제가 성공적으로 삭제되었습니다.");
		assertThat(plan.isDeleted()).isTrue();

	}

}
