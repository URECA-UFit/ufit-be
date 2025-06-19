package com.ureca.ufit.common.fixture;

import static lombok.AccessLevel.*;

import java.util.Map;

import com.ureca.ufit.entity.ChatBotMessage;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ChatBotMessageFixture {

	public static ChatBotMessage chatBotMessage(Long chatRoomId, Map<String, Object> recommendPlan) {
		return ChatBotMessage.of(
			"추천해주세요",
			true,
			recommendPlan,
			chatRoomId
		);
	}
}
