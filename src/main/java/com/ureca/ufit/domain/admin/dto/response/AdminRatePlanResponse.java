package com.ureca.ufit.domain.admin.dto.response;

import java.time.LocalDateTime;

public record AdminRatePlanResponse(
	String ratePlanId,
	String planName,
	String summary,
	int monthlyFee,
	int discountFee,
	String voiceAllowance,
	String sms_allowance,
	String basic_benefit,
	boolean isEnabled,
	boolean isDeleted,
	LocalDateTime createdAt
) {
}
