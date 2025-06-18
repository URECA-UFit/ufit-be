package com.ureca.ufit.domain.admin.dto.request;

import java.util.Map;

public record CallRatePlanRequest(
	String ratePlanId,
	String planName,
	String summary,
	int monthlyFee,
	int discountFee,
	String extraData,
	String dataAllowance,
	String dataCategory,
	String voiceAllowance,
	String smsAllowance,
	Map<String, Object> basicBenefit,
	Map<String, Object> discountBenefit,
	Map<String, Object> specialBenefit,
	String deviceType,
	String dataSharing,
	String socialCategory
) {
}
