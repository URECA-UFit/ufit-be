package com.ureca.ufit.domain.chatbot.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateUserQuerySummaryRequest(
	@NotBlank
	@Schema(description = "리뷰에 해당하는 추천 요금제 메시지ID", example = "684d9a790eea0b57af47a8d1")
	@JsonProperty("recommendation_message_id")
	String recommendation_message_id

) {
}