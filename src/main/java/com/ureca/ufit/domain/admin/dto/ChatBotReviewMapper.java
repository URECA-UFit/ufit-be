package com.ureca.ufit.domain.admin.dto;

import static lombok.AccessLevel.*;

import com.ureca.ufit.domain.chatbot.dto.request.CreateChatBotReviewRequest;
import com.ureca.ufit.domain.chatbot.dto.response.CreateChatBotReviewResponse;
import com.ureca.ufit.domain.chatbot.dto.response.QuestionSummaryDto;
import com.ureca.ufit.entity.ChatBotReview;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ChatBotReviewMapper {

	private static final String CHAT_REVIEW_SUCCESS_MSG = "리뷰가 정상적으로 제출되었습니다.";

	//    private static ChatBotReviewResponse toChatBotReview(){
	//        return ;
	//    }

	public static CreateChatBotReviewResponse toCreateChatBotReviewResponse() {
		return new CreateChatBotReviewResponse(CHAT_REVIEW_SUCCESS_MSG);
	}

	public static ChatBotReview toChatBotReview(CreateChatBotReviewRequest request,
		QuestionSummaryDto questionSummaryDto) {
		return ChatBotReview.of(
			request.content(),
			request.rating(),
			request.recommendPlans(),
			questionSummaryDto.summary()
		);
	}
}