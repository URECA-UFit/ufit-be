package com.ureca.ufit.common.fixture;

import static lombok.AccessLevel.*;

import java.util.Map;

import com.ureca.ufit.entity.RatePlan;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class RatePlanFixture {

	public static RatePlan ratePlan(String planName, int monthlyFee) {
		return RatePlan.of(
			planName,
			"요금제",
			monthlyFee,
			1000,
			"100G",
			"무제한",
			"무제한",
			Map.of("benefit", "basic"),
			true,
			false
		);
	}

	public static RatePlan ratePlan(String planName, boolean isEnable, boolean isDeleted) {
		return RatePlan.of(
			planName,
			"요금제",
			1000,
			1000,
			"100G",
			"무제한",
			"무제한",
			Map.of("benefit", "basic"),
			isEnable,
			isDeleted
		);
	}
}
