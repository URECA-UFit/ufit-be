package com.ureca.ufit.common.fixture;

import static lombok.AccessLevel.*;

import java.util.Map;

import com.ureca.ufit.entity.ChatBotReview;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ChatBotReviewFixture {

	public static ChatBotReview chatBotReview(int rating, Map<String, Object> recommendPlan) {
		return ChatBotReview.of(
			"추천 퀄리티가 너무 좋아서…",
			rating,
			recommendPlan,
			"넉넉한 데이터로 마음놓고 사용가능한 요금제",
			"chatReview-123"
		);
	}

	public static ChatBotReview chatBotReview(String content, String questionSummary) {
		return ChatBotReview.of(
			content,
			1,
			Map.of(),
			questionSummary,
			"chatReview-123"
		);
	}
}
