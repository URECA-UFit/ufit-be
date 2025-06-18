package com.ureca.ufit.domain.chatbot.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ureca.ufit.domain.admin.dto.response.ChatBotReviewResponse;
import com.ureca.ufit.entity.ChatBotReview;
import com.ureca.ufit.global.dto.CursorPageResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatBotReviewQueryRepositoryImpl implements ChatBotReviewQueryRepository {

	private static final String COLLECTION = "chat_bot_reviews";
	private final MongoTemplate mongoTemplate;

	public CursorPageResponse<ChatBotReviewResponse> getChatBotReviewByCursor(
		String cursor, int size) {

		Criteria criteria = new Criteria();
		if (cursor != null && !cursor.isBlank()) {
			criteria = Criteria.where("_id").lt(new ObjectId(cursor));
		}

		Query query = new Query(criteria)
			.with(Sort.by(Sort.Direction.DESC, "_id"))
			.limit(size + 1);

		List<ChatBotReview> docs = mongoTemplate.find(query, ChatBotReview.class, COLLECTION);

		boolean hasNext = docs.size() > size;
		List<ChatBotReviewResponse> items = new ArrayList<>();

		for (int i = 0; i < Math.min(size, docs.size()) ; i++) {
			ChatBotReview review = docs.get(i);

			String recommendPlanString = "";
			Map<String, Object> recommendPlanMap = review.getRecommendPlan();

			if (recommendPlanMap != null && !recommendPlanMap.isEmpty()) {
				String aPlan = Optional.ofNullable(recommendPlanMap.get("aPlan"))
					.map(Object::toString)
					.orElse("");
				String bPlan = Optional.ofNullable(recommendPlanMap.get("bPlan"))
					.map(Object::toString)
					.orElse("");

				if (!aPlan.isEmpty() && !bPlan.isEmpty()) {
					recommendPlanString = aPlan + ", " + bPlan;
				} else if (!aPlan.isEmpty()) {
					recommendPlanString = aPlan;
				} else if (!bPlan.isEmpty()) {
					recommendPlanString = bPlan;
				}
			}
			items.add(new ChatBotReviewResponse(
				review.getId(),
				review.getQuestionSummary(),
				recommendPlanString,
				review.getRating(),
				review.getContent()
			));
		}

		String nextCursor = hasNext
			? items.get(items.size() - 1).chatBotReviewId()
			: null;

		return new CursorPageResponse<>(items, nextCursor, hasNext);
	}

}
