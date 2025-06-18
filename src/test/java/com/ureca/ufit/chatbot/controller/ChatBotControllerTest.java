package com.ureca.ufit.chatbot.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.ureca.ufit.common.support.ApiSupport;
import com.ureca.ufit.domain.chatbot.dto.response.ChatRoomCreateResponse;
import com.ureca.ufit.domain.chatbot.service.ChatBotMessageService;
import com.ureca.ufit.domain.chatbot.service.ChatRoomService;

class ChatBotControllerTest extends ApiSupport {

	@MockBean
	ChatRoomService chatRoomService;


	@DisplayName("이미 존재하는 채팅방이 있으면 ID만 반환한다.")
	@Test
	void returnExistingChatRoomId() throws Exception {
		// given
		long expectedChatRoomId = 1L;
		ChatRoomCreateResponse stub =
			new ChatRoomCreateResponse(expectedChatRoomId, false);

		given(chatRoomService.getOrCreateChatRoom(any())).willReturn(stub);

		// when // then
		mockMvc.perform(post("/api/chats/rooms")
				.contentType(APPLICATION_JSON)
				.header("Authorization", accessTokenOfUser))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.chatRoomId").value(expectedChatRoomId))
			.andExpect(jsonPath("$.isAnonymous").value(false));
	}

	@DisplayName("비회원이 요청하면 새로운 익명 채팅방을 생성한다.")
	@Test
	void createAnonymousChatRoomWhenNoToken() throws Exception {
		// given
		long newChatRoomId = 2L;
		ChatRoomCreateResponse stub = new ChatRoomCreateResponse(newChatRoomId, true);

		given(chatRoomService.getOrCreateChatRoom(isNull())).willReturn(stub);

		// when // then
		mockMvc.perform(post("/api/chats/rooms")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.chatRoomId").value(newChatRoomId))
			.andExpect(jsonPath("$.isAnonymous").value(true));
	}

}