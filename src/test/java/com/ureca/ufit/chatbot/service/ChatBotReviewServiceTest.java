package com.ureca.ufit.chatbot.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.ureca.ufit.common.fixture.ChatBotReviewFixture;
import com.ureca.ufit.domain.chatbot.dto.request.CreateChatBotReviewRequest;
import com.ureca.ufit.domain.chatbot.dto.request.CreateUserQuerySummaryRequest;
import com.ureca.ufit.domain.chatbot.dto.response.CreateChatBotReviewResponse;
import com.ureca.ufit.domain.chatbot.dto.response.QuestionSummaryDto;
import com.ureca.ufit.domain.chatbot.repository.ChatBotReviewRepository;
import com.ureca.ufit.domain.chatbot.service.ChatBotReviewService;
import com.ureca.ufit.entity.ChatBotReview;

@ExtendWith(MockitoExtension.class)
class ChatBotReviewServiceTest {

	@Mock
	RestTemplate restTemplate;
	@Mock
	ChatBotReviewRepository chatBotReviewRepository;
	@InjectMocks
	ChatBotReviewService chatBotReviewService;

	@DisplayName("챗봇 리뷰를 저장한다.")
	@Test
	void savedChatBotReview() {
		// given
		final String CHAT_REVIEW_SUCCESS_MSG = "리뷰가 정상적으로 제출되었습니다.";

		CreateChatBotReviewRequest request = new CreateChatBotReviewRequest(
			1,
			"추천 퀄리티가 너무 좋아서 깜짝 놀랐어요.",
			Map.of(
				"recommandPlans",
				List.of(
					Map.of("aPlan", "5G 무제한"),
					Map.of("bPlan", "내맘대로 5G 요금제")
				)
			),
			1L,
			"684d9a790eea0b57af47a8d1"
		);
		QuestionSummaryDto questionSummaryDto = new QuestionSummaryDto("데이터 많은 요금제 추천해줘");
		ChatBotReview chatBotReview = ChatBotReviewFixture.chatBotReview(
			request.content(),
			questionSummaryDto.summary()
		);

		given(restTemplate.postForObject(
			anyString(),
			any(CreateUserQuerySummaryRequest.class),
			eq(QuestionSummaryDto.class)
		)).willReturn(questionSummaryDto);
		given(chatBotReviewRepository.save(any(ChatBotReview.class))).willReturn(chatBotReview);

		// when
		CreateChatBotReviewResponse response = chatBotReviewService.createChatBotReview(request);

		// then
		assertAll(
			() -> assertThat(response.message()).isEqualTo(CHAT_REVIEW_SUCCESS_MSG),
			() -> verify(chatBotReviewRepository).save(any(ChatBotReview.class)),
			() -> verify(restTemplate).postForObject(
				anyString(),
				any(CreateUserQuerySummaryRequest.class),
				eq(QuestionSummaryDto.class))
		);
	}
}
