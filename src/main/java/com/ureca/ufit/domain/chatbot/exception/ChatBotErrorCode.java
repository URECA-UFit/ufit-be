package com.ureca.ufit.domain.chatbot.exception;

import org.springframework.http.HttpStatus;

import com.ureca.ufit.global.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatBotErrorCode implements ErrorCode {

	CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
	LLM_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "챗봇 답변 생성 시 오류가 발생하였습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 UserId의 User를 찾을 수 없습니다."),
	LLM_SUMMARY_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "챗봇 리뷰 요약을 실패했습니다."),
	;

	private final HttpStatus httpStatus;
	private final String message;
}
