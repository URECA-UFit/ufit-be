package com.ureca.ufit.domain.chatbot.dto.response;

import java.util.Map;

import com.ureca.ufit.entity.enums.AnswerType;

public record CreateChatBotMessageResponse(

	Long messageId,

	Long userId,

	String answer,

	AnswerType answerType,

	Map<String, Map<String, Object>> recommendPlans
) {
}
