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
	CONTENT_RESTRICTED_WORD(HttpStatus.UNPROCESSABLE_ENTITY, "입력한 내용에 허용되지 않은 단어가 포함되어 있습니다. 다른 표현을 사용해주세요."),
	CHAT_BOT_REVIEW_DUPLICATED(HttpStatus.CONFLICT, "리뷰를 중복해서 입력할 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
