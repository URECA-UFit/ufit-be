package com.ureca.ufit.entity;

import static lombok.AccessLevel.*;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.ureca.ufit.global.domain.MongoTimeBaseEntity;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Document(collection = "chat_bot_messages")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class ChatBotMessage extends MongoTimeBaseEntity {

	@Id
	private String id;

	@NotNull
	@Field("content")
	private String content;

	@NotNull
	@Field("owner")
	private boolean owner;

	@NotNull
	@Field("recommend_plan")
	private Map<String, Object> recommendPlan;

	@NotNull
	@Field("chat_room_id")
	private Long chatRoomId;

	@Builder(access = PRIVATE)
	private ChatBotMessage(String content, boolean owner, Map<String, Object> recommendPlan, Long chatRoomId) {
		this.content = content;
		this.owner = owner;
		this.recommendPlan = recommendPlan;
		this.chatRoomId = chatRoomId;
	}

	public static ChatBotMessage of(String content, boolean owner, Map<String, Object> recommendPlan, Long chatRoomId) {
		return ChatBotMessage.builder()
			.content(content)
			.owner(owner)
			.recommendPlan(recommendPlan)
			.chatRoomId(chatRoomId)
			.build();
	}

}
