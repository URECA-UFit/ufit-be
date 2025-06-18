package com.ureca.ufit.chatbot.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ureca.ufit.common.fixture.ChatBotReviewFixture;
import com.ureca.ufit.common.support.DataMongoSupport;
import com.ureca.ufit.domain.admin.dto.response.ChatBotReviewResponse;
import com.ureca.ufit.domain.chatbot.repository.ChatBotReviewQueryRepositoryImpl;
import com.ureca.ufit.domain.chatbot.repository.ChatBotReviewRepository;
import com.ureca.ufit.entity.ChatBotReview;
import com.ureca.ufit.global.dto.CursorPageResponse;

public class ChatBotReviewRepositoryTest extends DataMongoSupport {

	@Autowired
	ChatBotReviewRepository chatBotReviewRepository;

	@Autowired
	ChatBotReviewQueryRepositoryImpl repository = new ChatBotReviewQueryRepositoryImpl(mongoTemplate);

	@AfterEach
	void tearDown() {
		chatBotReviewRepository.deleteAll();
	}

	@DisplayName("커서 기반으로 챗봇 리뷰를 조회한다")
	@Test
	void getRatePlansByCursor() {
		// given
		final int SIZE = 2;

		// recommendPlan 더미 데이터
		Map<String, Object> dummyPlan = Map.of(
			"aPlan", "aPlan",
			"bPlan", "bPlan"
		);

		// Fixture로 생성
		ChatBotReview r1 = ChatBotReviewFixture.chatBotReview(5, dummyPlan);
		ChatBotReview r2 = ChatBotReviewFixture.chatBotReview(4, dummyPlan);
		ChatBotReview r3 = ChatBotReviewFixture.chatBotReview(3, dummyPlan);
		ChatBotReview r4 = ChatBotReviewFixture.chatBotReview(2, dummyPlan);
		ChatBotReview r5 = ChatBotReviewFixture.chatBotReview(1, dummyPlan);

		chatBotReviewRepository.saveAll(List.of(r1, r2, r3, r4, r5));

		// when
		CursorPageResponse<ChatBotReviewResponse> page1 =
			chatBotReviewRepository.getChatBotReviewByCursor(null, SIZE);

		// then - 원하는 결과
		assertAll(
			() -> assertThat(page1.item()).hasSize(SIZE),
			() -> assertThat(page1.item())
				.extracting(ChatBotReviewResponse::chatBotReviewId)
				.containsExactly(r5.getId(), r4.getId()),
			() -> assertThat(page1.hasNext()).isTrue(),
			() -> assertThat(page1.nextCursor()).isEqualTo(r4.getId())
		);

		//
	}

}
