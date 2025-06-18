package com.ureca.ufit.chatbot.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;

import com.ureca.ufit.common.support.ApiSupport;
import com.ureca.ufit.domain.chatbot.dto.response.ChatMessageDto;
import com.ureca.ufit.domain.chatbot.dto.response.ChatRoomCreateResponse;
import com.ureca.ufit.domain.chatbot.service.ChatBotMessageService;
import com.ureca.ufit.domain.chatbot.service.ChatRoomService;
import com.ureca.ufit.global.dto.CursorPageResponse;

class ChatBotControllerTest extends ApiSupport {

	@MockBean
	ChatRoomService chatRoomService;

	@MockBean
	ChatBotMessageService chatBotMessageService;

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

	@DisplayName("특정 채팅방의 메시지를 Cursor 방식으로 조회한다.")
	@Test
	void getMessagesWithCursorSuccess() throws Exception {
		// given
		Long chatRoomId = 3L;
		String lastMsgId = "665f3af6d6795e4f5e8e5a12";

		ChatMessageDto dto = new ChatMessageDto(lastMsgId, "안녕", true, Collections.emptyList());
		CursorPageResponse<ChatMessageDto> stub = new CursorPageResponse<>(List.of(dto), null, false);

		given(chatBotMessageService.getChatMessages(eq(chatRoomId), any(Pageable.class), eq(lastMsgId)))
			.willReturn(stub);

		// when // then
		mockMvc.perform(get("/api/chats/{chatroomId}", chatRoomId)
				.param("lastMessageId", lastMsgId)
				.param("size", "10")
				.header("Authorization", accessTokenOfUser))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.item[0].messageId").value(dto.messageId()))
			.andExpect(jsonPath("$.hasNext").value(false));
	}
}