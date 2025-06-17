package com.ureca.ufit.domain.admin.exception;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ureca.ufit.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FastAPIErrorCode implements ErrorCode {

	RATE_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "요금제를 찾을 수 없습니다."),
	EMBEDDING_CREATE_FAIL(HttpStatus.GATEWAY_TIMEOUT, "요금제 생성 임베딩 중 오류가 발생하였습니다."),
	EMBEDDING_DELETE_FAIL(HttpStatus.GATEWAY_TIMEOUT, "요금제 삭제 임베딩 중 오류가 발생하였습니다.");


	private final HttpStatus httpStatus;
	private final String message;


	@JsonCreator
	public static FastAPIErrorCode from(String value) {
		for (FastAPIErrorCode code : FastAPIErrorCode.values()) {
			if (code.name().equalsIgnoreCase(value)) {
				return code;
			}
		}
		throw new IllegalArgumentException("Unknown errorCode: " + value);
	}
}
