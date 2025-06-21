package com.ureca.ufit.chatbot.service;

import static com.ureca.ufit.domain.chatbot.exception.ChatBotErrorCode.*;
import static java.lang.Boolean.*;
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
import com.ureca.ufit.domain.chatbot.client.ChatClient;
import com.ureca.ufit.domain.chatbot.dto.request.CreateChatBotReviewRequest;
import com.ureca.ufit.domain.chatbot.dto.request.CreateUserQuerySummaryRequest;
import com.ureca.ufit.domain.chatbot.dto.response.CreateChatBotReviewResponse;
import com.ureca.ufit.domain.chatbot.dto.response.QuestionSummaryDto;
import com.ureca.ufit.domain.chatbot.repository.ChatBotReviewRepository;
import com.ureca.ufit.domain.chatbot.service.ChatBotMessageService;
import com.ureca.ufit.domain.chatbot.service.ChatBotReviewService;
import com.ureca.ufit.domain.chatbot.service.ChatRoomService;
import com.ureca.ufit.entity.ChatBotReview;
import com.ureca.ufit.global.exception.RestApiException;
import com.ureca.ufit.global.profanity.ProfanityService;

@ExtendWith(MockitoExtension.class)
class ChatBotReviewServiceTest {

	@Mock
	// RestTemplate restTemplate;
	ChatClient chatClient;
	@Mock
	ChatBotReviewRepository chatBotReviewRepository;
	@Mock
	ProfanityService profanityService;
	@Mock
	ChatRoomService chatRoomService;
	@Mock
	ChatBotMessageService chatBotMessageService;
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

		given(chatClient.getSummary(any(), anyLong())).willReturn(questionSummaryDto);

		// given(restTemplate.postForObject(
		// 	anyString(),
		// 	any(CreateUserQuerySummaryRequest.class),
		// 	eq(QuestionSummaryDto.class)
		// )).willReturn(questionSummaryDto);
		given(chatBotReviewRepository.save(any(ChatBotReview.class))).willReturn(chatBotReview);

		// when
		CreateChatBotReviewResponse response = chatBotReviewService.createChatBotReview(request);

		// then
		// assertAll(
		// 	() -> assertThat(response.message()).isEqualTo(CHAT_REVIEW_SUCCESS_MSG),
		// 	() -> verify(chatBotReviewRepository).save(any(ChatBotReview.class)),
		// 	() -> verify(restTemplate).postForObject(
		// 		anyString(),
		// 		any(CreateUserQuerySummaryRequest.class),
		// 		eq(QuestionSummaryDto.class))
		// );
		assertThat(response.message()).isEqualTo(CHAT_REVIEW_SUCCESS_MSG);
		verify(chatClient).getSummary(any(), anyLong());
		verify(chatBotReviewRepository).save(any());
	}

	@DisplayName("채팅방ID가 유효하지 않은 리뷰는 저장할 수 없다.")
	@Test
	void throwExceptionWhenChatRoomIdIsInvalid() {
		// given
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

		doThrow(new RestApiException(CHATROOM_NOT_FOUND))
			.when(chatRoomService)
			.getValidatedChatRoom(anyLong());

		// when  // then
		assertThatThrownBy(() -> chatBotReviewService.createChatBotReview(request))
			.isInstanceOf(RestApiException.class)
			.hasMessage(CHATROOM_NOT_FOUND.getMessage());
	}

	@DisplayName("리뷰 메시지와 채팅방이 일치하지 않으면 리뷰를 저장할 수 없다.")
	@Test
	void throwExceptionWhenChatBotMessageNotMatchChatRoom() {
		// given
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

		doThrow(new RestApiException(INVALID_CHATBOT_MESSAGE))
			.when(chatBotMessageService)
			.validateMessageBelongsToChatRoom(anyString(), anyLong());

		// when  // then
		assertThatThrownBy(() -> chatBotReviewService.createChatBotReview(request))
			.isInstanceOf(RestApiException.class)
			.hasMessage(INVALID_CHATBOT_MESSAGE.getMessage());
	}

	@DisplayName("챗봇 리뷰 내용에 금칙어가 포함되어 있으면 저장할 수 없다.")
	@Test
	void throwExceptionWhenChatBotReviewContentIsProfanity() {
		// given
		CreateChatBotReviewRequest request = new CreateChatBotReviewRequest(
			1,
			"추천 퀄리티가 존나 좋네요 ㅋㅋ",
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

		given(chatBotReviewRepository.existsByChatBotMessageId(anyString())).willReturn(FALSE);
		given(profanityService.containsBannedWord(anyString(), anySet())).willReturn(TRUE);

		// when  // then
		assertThatThrownBy(() -> chatBotReviewService.createChatBotReview(request))
			.isInstanceOf(RestApiException.class)
			.hasMessage(CONTENT_RESTRICTED_WORD.getMessage());
	}

	@DisplayName("챗봇 리뷰를 중복해서 저장할 수 없다.")
	@Test
	void throwExceptionWhenChatBotReviewIsDuplicated() {
		// given
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

		given(chatBotReviewRepository.existsByChatBotMessageId(anyString())).willReturn(TRUE);

		// when  // then
		assertThatThrownBy(() -> chatBotReviewService.createChatBotReview(request))
			.isInstanceOf(RestApiException.class)
			.hasMessage(CHAT_BOT_REVIEW_DUPLICATED.getMessage());
	}
	//
	// @DisplayName("사용자 질의 요약이 비어있다면 리뷰를 저장할 수 없다.")
	// @Test
	// void throwExceptionWhenSummaryIsEmtpy() {
	// 	// given
	// 	CreateChatBotReviewRequest request = new CreateChatBotReviewRequest(
	// 		1,
	// 		"추천 퀄리티가 너무 좋아서 깜짝 놀랐어요.",
	// 		Map.of(
	// 			"recommandPlans",
	// 			List.of(
	// 				Map.of("aPlan", "5G 무제한"),
	// 				Map.of("bPlan", "내맘대로 5G 요금제")
	// 			)
	// 		),
	// 		1L,
	// 		"684d9a790eea0b57af47a8d1"
	// 	);
	//
	// 	// given(restTemplate.postForObject(
	// 	// 	anyString(),
	// 	// 	any(CreateUserQuerySummaryRequest.class),
	// 	// 	eq(QuestionSummaryDto.class)
	// 	// )).willReturn(null);
	// 	given(chatClient.getSummary(any(), anyLong())).willReturn(null);
	//
	//
	// 	// when  // then
	// 	assertThatThrownBy(() -> chatBotReviewService.createChatBotReview(request))
	// 		.isInstanceOf(RestApiException.class)
	// 		.hasMessage(LLM_SUMMARY_FAIL.getMessage());
	// }
}
