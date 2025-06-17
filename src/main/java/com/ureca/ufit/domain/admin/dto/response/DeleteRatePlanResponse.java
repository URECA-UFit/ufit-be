package com.ureca.ufit.domain.admin.dto.response;

public record DeleteRatePlanResponse(
	String message,
	String ratePlanId,
	boolean isDeleted
) {
}
