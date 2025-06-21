package com.ureca.ufit.domain.chatbot.client;

import com.ureca.ufit.domain.chatbot.dto.request.CreateUserQuerySummaryRequest;
import com.ureca.ufit.domain.chatbot.dto.response.QuestionSummaryDto;

public interface ChatClient {
	QuestionSummaryDto getSummary(CreateUserQuerySummaryRequest request, long chatRoomId);
}