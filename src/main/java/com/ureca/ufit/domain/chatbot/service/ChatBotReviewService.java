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

	private final RestTemplate restTemplate;
	private final ChatBotReviewRepository chatBotReviewRepository;
	private final ProfanityService profanityService;
	private Set<BanwordFilterPolicy> policies = Set.of(NUMBERS, WHITESPACES);

	public CreateChatBotReviewResponse createChatBotReview(CreateChatBotReviewRequest request) {

		validateDuplicatedChatBotReview(request);

		validateProfanity(request);

		final String url = String.format("%s/api/chats/review/%d", llmBaseUrl, request.chatRoomId());

		CreateUserQuerySummaryRequest chatReviewSummaryRequest = new CreateUserQuerySummaryRequest(
			request.recommendation_message_id());

		QuestionSummaryDto questionSummaryDto = restTemplate.postForObject(url, chatReviewSummaryRequest,
			QuestionSummaryDto.class);

		validateSummary(questionSummaryDto);

		ChatBotReview chatBotReview = ChatBotReviewMapper.toChatBotReview(request, questionSummaryDto);

		chatBotReviewRepository.save(chatBotReview);

		return ChatBotReviewMapper.toCreateChatBotReviewResponse();
	}

	private void validateDuplicatedChatBotReview(CreateChatBotReviewRequest request) {
		if (chatBotReviewRepository.existsByChatBotMessageId(request.recommendation_message_id())) {
			throw new RestApiException(CHAT_BOT_REVIEW_DUPLICATED);
		}
	}

	private void validateProfanity(CreateChatBotReviewRequest request) {
		if (profanityService.containsBannedWord(request.content(), policies)) {
			throw new RestApiException(CONTENT_RESTRICTED_WORD);
		}
	}

	private void validateSummary(QuestionSummaryDto questionSummaryDto) {
		Optional.ofNullable(questionSummaryDto)
			.map(QuestionSummaryDto::summary)
			.filter(StringUtils::hasText)
			.orElseThrow(() -> new RestApiException(LLM_SUMMARY_FAIL));
	}
}
