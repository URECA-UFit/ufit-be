package com.ureca.ufit.domain.rateplan.dto.response;

public record RatePlanPreviewResponse(
	String id,
	String planName,
	int monthlyFee,
	int discountFee
) {
}