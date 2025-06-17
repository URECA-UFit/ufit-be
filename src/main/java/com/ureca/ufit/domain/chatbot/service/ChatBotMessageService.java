package com.ureca.ufit.domain.chatbot.service;

import static com.ureca.ufit.global.profanity.BanwordFilterPolicy.*;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ureca.ufit.domain.chatbot.dto.ChatMessageMapper;
import com.ureca.ufit.domain.chatbot.dto.request.CreateAIAnswerRequest;
import com.ureca.ufit.domain.chatbot.dto.request.CreateChatBotMessageRequest;
import com.ureca.ufit.domain.chatbot.dto.response.ChatMessageDto;
import com.ureca.ufit.domain.chatbot.dto.response.CreateChatBotMessageResponse;
import com.ureca.ufit.domain.chatbot.exception.ChatBotErrorCode;
import com.ureca.ufit.domain.chatbot.repository.ChatBotMessageRepository;
import com.ureca.ufit.domain.chatbot.repository.ChatRoomRepository;
import com.ureca.ufit.entity.ChatRoom;
import com.ureca.ufit.global.dto.CursorPageResponse;
import com.ureca.ufit.global.exception.RestApiException;
import com.ureca.ufit.global.profanity.BanwordFilterPolicy;
import com.ureca.ufit.global.profanity.ProfanityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatBotMessageService {

	@Value("${llm.base-url}")
	private String llmBaseUrl;
	private final ProfanityService profanityService;
	private final ChatBotMessageRepository chatBotMessageRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final RestTemplate restTemplate;

	public CursorPageResponse<ChatMessageDto> getChatMessages(Long chatRoomId, Pageable pageable,
		String lastMessageId) {
		ChatRoom findChatRoom = chatRoomRepository.getById(chatRoomId);

		return chatBotMessageRepository.findMessagesPage(findChatRoom, pageable, lastMessageId);
	}

	public CreateChatBotMessageResponse createChatBotMessage(CreateChatBotMessageRequest request, Long userId) {

		Set<BanwordFilterPolicy> policies = Set.of(NUMBERS, WHITESPACES);

		if (profanityService.containsBannedWord(request.content(), policies)) {
			throw new RestApiException(ChatBotErrorCode.CONTENT_RESTRICTED_WORD);
		}

		final String fastApiUrl = String.format("%s/api/chats/message/ai", llmBaseUrl);

		CreateAIAnswerRequest createAIAnswerRequest = ChatMessageMapper.toCreateAIAnswerRequest(request, userId);

		try {
			return restTemplate.postForObject(
				fastApiUrl,
				createAIAnswerRequest,
				CreateChatBotMessageResponse.class
			);

		} catch (Exception e) {
			throw new RestApiException(ChatBotErrorCode.LLM_TIMEOUT);
		}
	}

	public void validateMessageBelongsToChatRoom(String chatBotMessage, Long chatRoomId) {
		if (chatBotMessageRepository.existsByIdAndChatRoomId(chatBotMessage, chatRoomId)) {
			throw new RestApiException(ChatBotErrorCode.INVALID_CHATBOT_MESSAGE);
		}
	}
}
