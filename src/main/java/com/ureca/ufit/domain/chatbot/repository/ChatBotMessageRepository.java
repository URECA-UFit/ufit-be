package com.ureca.ufit.domain.chatbot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ureca.ufit.entity.ChatBotMessage;

@Repository
public interface ChatBotMessageRepository

	extends MongoRepository<ChatBotMessage, String>, ChatBotMessageRepositoryCustom {
	boolean existsByIdAndChatRoomId(String chatBotMessage, Long chatRoomId);
}
