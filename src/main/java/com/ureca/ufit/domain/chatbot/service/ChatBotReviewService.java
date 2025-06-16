package com.ureca.ufit.domain.chatbot.service;

import static com.ureca.ufit.domain.chatbot.exception.ChatBotErrorCode.*;

import java.util.Optional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatBotReviewService {

	@Value("${llm.base-url}")
	private String llmBaseUrl;

	private final RestTemplate restTemplate;
	private final ChatBotReviewRepository chatBotReviewRepository;

	public CreateChatBotReviewResponse createChatBotReview(CreateChatBotReviewRequest request) {

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

	private void validateSummary(QuestionSummaryDto questionSummaryDto) {
		Optional.ofNullable(questionSummaryDto)
			.map(QuestionSummaryDto::summary)
			.filter(StringUtils::hasText)
			.orElseThrow(() -> new RestApiException(LLM_SUMMARY_FAIL));
	}
}
