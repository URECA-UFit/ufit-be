package com.ureca.ufit.admin.controller;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ureca.ufit.common.fixture.RatePlanFixture;
import com.ureca.ufit.common.support.ApiSupport;
import com.ureca.ufit.domain.admin.controller.AdminController;
import com.ureca.ufit.domain.rateplan.repository.RatePlanRepository;
import com.ureca.ufit.entity.RatePlan;

class AdminControllerTest extends ApiSupport {

	@Autowired
	AdminController adminController;

	@Autowired
	RatePlanRepository ratePlanRepository;

	@AfterEach
	void tearDown() {
		ratePlanRepository.deleteAll();
	}

	@DisplayName("요금제의 판매 상태를 변경한다.")
	@Test
	void updateRatePlanSalesStatus() throws Exception {
		// given
		RatePlan savedRatePlan = ratePlanRepository.save(RatePlanFixture.ratePlan("판매 중인 요금제", true, false));

		// when  // then
		mockMvc.perform(patch("/api/admin/rateplans/{ratePlanId}", savedRatePlan.getId())
				.contentType(APPLICATION_JSON)
				.header("Authorization", accessTokenOfAdmin)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ratePlanId").value(savedRatePlan.getId()))
			.andExpect(jsonPath("$.isEnabled").value(false));
	}

	@DisplayName("상품의 ID값이 없으면 판매 상태를 변경할 수 없다.")
	@Test
	void throwValidationExceptionWhenRatePlanIdIsNull() throws Exception {
		// when  // then
		mockMvc.perform(patch("/api/admin/rateplans/{ratePlanId}", " ")
				.contentType(APPLICATION_JSON)
				.header("Authorization", accessTokenOfAdmin)
			)
			.andExpect(status().is4xxClientError());
	}
}
