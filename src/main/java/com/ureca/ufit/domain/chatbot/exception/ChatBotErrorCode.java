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
	CONTENT_RESTRICTED_WORD(HttpStatus.UNPROCESSABLE_ENTITY, "죄송합니다. 해당 요청은 서비스 이용 정책에 따라 처리할 수 없습니다.\n다른 질문을 해주세요."),
	CHAT_BOT_REVIEW_DUPLICATED(HttpStatus.CONFLICT, "리뷰를 중복해서 입력할 수 없습니다."),
	INVALID_CHATBOT_MESSAGE(HttpStatus.BAD_REQUEST, "해당 추천 메시지는 채팅방에 속하지 않습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
