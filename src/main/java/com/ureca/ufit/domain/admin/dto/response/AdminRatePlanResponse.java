package com.ureca.ufit.domain.admin.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record AdminRatePlanResponse(
	String ratePlanId,
	String planName,
	String summary,
	Integer monthlyFee,
	Integer discountFee,
	String dataAllowance,
	String voiceAllowance,
	String smsAllowance,
	Map<String, Object> basicBenefit,
	Map<String, Object> specialBenefit,
	Map<String, Object> discountBenefit,
	Boolean isEnabled,
	Boolean isDeleted,
	LocalDateTime createdAt
) {
}
