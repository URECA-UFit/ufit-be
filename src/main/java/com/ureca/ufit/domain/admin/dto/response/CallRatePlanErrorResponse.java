package com.ureca.ufit.domain.admin.dto.response;

import com.ureca.ufit.domain.admin.exception.FastAPIErrorCode;

public record CallRatePlanErrorResponse(
	String message,
	FastAPIErrorCode errorCode
) {
}
