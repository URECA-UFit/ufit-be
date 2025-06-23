package com.ureca.ufit.chatbot.controller;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ureca.ufit.common.fixture.ChatBotMessageFixture;
import com.ureca.ufit.common.fixture.ChatRoomFixture;
import com.ureca.ufit.common.support.ApiSupport;
import com.ureca.ufit.domain.chatbot.repository.ChatBotMessageRepository;
import com.ureca.ufit.domain.chatbot.repository.ChatRoomRepository;
import com.ureca.ufit.entity.ChatBotMessage;
import com.ureca.ufit.entity.ChatRoom;

class ChatBotControllerTest extends ApiSupport {

	@Autowired
	private ChatRoomRepository chatRoomRepository;

	@Autowired
	private ChatBotMessageRepository chatBotMessageRepository;

	@AfterEach
	void teardown() {
		chatBotMessageRepository.deleteAll();
		chatRoomRepository.deleteAll();
	}

	@DisplayName("이미 존재하는 채팅방이 있으면 ID만 반환한다.")
	@Test
	void returnExistingChatRoomId() throws Exception {
		ChatRoom existingChatRoom = chatRoomRepository.save(ChatRoomFixture.chatRoom(2L, loginUser));

		mockMvc.perform(post("/api/chats/rooms")
				.header("Authorization", accessTokenOfUser)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.chatRoomId").value(existingChatRoom.getId()))
			.andExpect(jsonPath("$.isAnonymous").value(false));
	}

	@DisplayName("비회원이 요청하면 새로운 익명 채팅방을 생성한다.")
	@Test
	void createAnonymousChatRoomWhenNoToken() throws Exception {
		mockMvc.perform(post("/api/chats/rooms")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.chatRoomId").isNumber())
			.andExpect(jsonPath("$.isAnonymous").value(true));
	}

	@DisplayName("특정 채팅방의 메시지를 Cursor 방식으로 조회한다.")
	@Test
	void getMessagesWithCursorSuccess() throws Exception {
		ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.chatRoom(1L, loginUser));

		ChatBotMessage savedMsg = chatBotMessageRepository.save(
			ChatBotMessageFixture.chatBotMessage(chatRoom.getId(), null));

		mockMvc.perform(get("/api/chats/{chatroomId}", chatRoom.getId())
				.param("size", "10")
				.header("Authorization", accessTokenOfUser)
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.item[0].messageId").value(savedMsg.getId().toString()))
			.andExpect(jsonPath("$.hasNext").value(false));
	}
}