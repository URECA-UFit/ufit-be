package com.ureca.ufit.domain.chatbot.dto.response;

import java.util.List;
import java.util.Map;

import com.ureca.ufit.entity.enums.AnswerType;

public record CreateChatBotMessageResponse(

	String messageId,

	String answer,

	AnswerType answerType,

	List<Map<String, Object>> recommendPlans
) {
}
