package com.ureca.ufit.domain.chatbot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ureca.ufit.entity.ChatBotReview;

@Repository
public interface ChatBotReviewRepository extends MongoRepository<ChatBotReview, String>, ChatBotReviewQueryRepository {
	boolean existsByChatBotMessageId(String chatBotMessageId);
}

