package com.ureca.ufit.domain.rateplan.exception;

import org.springframework.http.HttpStatus;

import com.ureca.ufit.global.exception.ErrorCode;

import lombok.Getter;

@Getter
public enum RatePlanErrorCode implements ErrorCode {

	RATE_PLAN_NOT_FOUND("해당 ratePlanId의 요금제를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	RATE_PLAN_ALREADY_DELETED("이미 삭제된 요금제입니다.", HttpStatus.BAD_REQUEST),
	CANNOT_DELETE_WHILE_ENABLED("판매중인 요금제는 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),
	CANNOT_DELETE_WITH_SUBSCRIBERS("사용자가 있는 요금제는 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST);

	private final String message;
	private final HttpStatus httpStatus;

	RatePlanErrorCode(String message, HttpStatus httpStatus) {
		this.message = message;
		this.httpStatus = httpStatus;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return this.httpStatus;
	}
}