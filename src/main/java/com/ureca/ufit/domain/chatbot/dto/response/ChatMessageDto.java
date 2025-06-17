package com.ureca.ufit.domain.chatbot.dto.response;

import java.util.List;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatMessageDto(
	ObjectId messageId,
	String content,
	Boolean owner,
	List<PlanDto> recommendPlans
) {
}
