package com.ureca.ufit.domain.admin.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record AdminRatePlanResponse(
	String ratePlanId,
	String planName,
	String summary,
	int monthlyFee,
	int discountFee,
	String dataAllowance,
	String voiceAllowance,
	String smsAllowance,
	Map<String, Object> basicBenefit,
	Map<String, Object> specialBenefit,
	Map<String, Object> discountBenefit,
	boolean isEnabled,
	boolean isDeleted,
	LocalDateTime createdAt
) {
}
