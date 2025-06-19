package com.ureca.ufit.domain.chatbot.service;

import static com.ureca.ufit.domain.chatbot.exception.ChatBotErrorCode.*;
import static com.ureca.ufit.global.profanity.BanwordFilterPolicy.*;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.ureca.ufit.domain.admin.dto.ChatBotReviewMapper;
import com.ureca.ufit.domain.chatbot.dto.request.CreateChatBotReviewRequest;
import com.ureca.ufit.domain.chatbot.dto.request.CreateUserQuerySummaryRequest;
import com.ureca.ufit.domain.chatbot.dto.response.CreateChatBotReviewResponse;
import com.ureca.ufit.domain.chatbot.dto.response.QuestionSummaryDto;
import com.ureca.ufit.domain.chatbot.repository.ChatBotReviewRepository;
import com.ureca.ufit.entity.ChatBotReview;
import com.ureca.ufit.global.exception.RestApiException;
import com.ureca.ufit.global.profanity.BanwordFilterPolicy;
import com.ureca.ufit.global.profanity.ProfanityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatBotReviewService {

	@Value("${llm.base-url}")
	private String llmBaseUrl;
	private Set<BanwordFilterPolicy> policies = Set.of(NUMBERS, WHITESPACES);

	private final RestTemplate restTemplate;
	private final ChatBotReviewRepository chatBotReviewRepository;
	private final ProfanityService profanityService;
	private final ChatBotMessageService chatBotMessageService;
	private final ChatRoomService chatRoomService;

	public CreateChatBotReviewResponse createChatBotReview(CreateChatBotReviewRequest request) {
		validateCreateChatBotReviewRequest(request);

		QuestionSummaryDto questionSummaryDto = requestUserQuerySummary(request);

		createAndSaveChatBotReview(request, questionSummaryDto);

		return ChatBotReviewMapper.toCreateChatBotReviewResponse();
	}

	private void validateCreateChatBotReviewRequest(CreateChatBotReviewRequest request) {
		chatRoomService.getValidatedChatRoom(request.chatRoomId());
		chatBotMessageService.validateMessageBelongsToChatRoom(request.recommendationMessageId(), request.chatRoomId());
		validateDuplicatedChatBotReview(request.recommendationMessageId());
		validateProfanity(request.content());
	}

	private void validateDuplicatedChatBotReview(String recommendationMessageId) {
		if (chatBotReviewRepository.existsByChatBotMessageId(recommendationMessageId)) {
			throw new RestApiException(CHAT_BOT_REVIEW_DUPLICATED);
		}
	}

	private void validateProfanity(String content) {
		if (profanityService.containsBannedWord(content, policies)) {
			throw new RestApiException(CONTENT_RESTRICTED_WORD);
		}
	}

	private QuestionSummaryDto requestUserQuerySummary(CreateChatBotReviewRequest request) {
		final String url = String.format("%s/api/chats/review/%d", llmBaseUrl, request.chatRoomId());

		CreateUserQuerySummaryRequest chatReviewSummaryRequest = new CreateUserQuerySummaryRequest(
			request.recommendationMessageId());

		QuestionSummaryDto questionSummaryDto = restTemplate.postForObject(url, chatReviewSummaryRequest,
			QuestionSummaryDto.class);

		validateSummary(questionSummaryDto);
		return questionSummaryDto;
	}

	private void validateSummary(QuestionSummaryDto questionSummaryDto) {
		Optional.ofNullable(questionSummaryDto)
			.map(QuestionSummaryDto::summary)
			.filter(StringUtils::hasText)
			.orElseThrow(() -> new RestApiException(LLM_SUMMARY_FAIL));
	}

	private void createAndSaveChatBotReview(CreateChatBotReviewRequest request, QuestionSummaryDto questionSummaryDto) {
		ChatBotReview chatBotReview = ChatBotReviewMapper.toChatBotReview(request, questionSummaryDto);

		chatBotReviewRepository.save(chatBotReview);
	}
}
