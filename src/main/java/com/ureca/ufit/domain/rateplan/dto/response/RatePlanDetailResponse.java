package com.ureca.ufit.domain.rateplan.dto.response;

import java.util.Map;

public record RatePlanDetailResponse(
	String id,
	String planName,
	String summary,
	int monthlyFee,
	int discountFee,
	String dataAllowance,
	String voiceAllowance,
	String smsAllowance,

	Map<String, Object> basicBenefit,
	Map<String, Object> specialBenefit,
	Map<String, Object> discountBenefit
) {
}
