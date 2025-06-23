package com.ureca.ufit.chatbot.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.ureca.ufit.common.fixture.ChatRoomFixture;
import com.ureca.ufit.common.support.DataMongoSupport;
import com.ureca.ufit.domain.chatbot.dto.response.ChatMessageDto;
import com.ureca.ufit.domain.chatbot.dto.response.PlanDto;
import com.ureca.ufit.domain.chatbot.repository.ChatBotMessageRepository;
import com.ureca.ufit.entity.ChatRoom;
import com.ureca.ufit.global.dto.CursorPageResponse;

public class ChatBotMessageRepositoryTest extends DataMongoSupport {

	private static final String COLLECTION_NAME = "chat_bot_messages";
	private static final int PAGE_SIZE = 3;

	@Autowired
	private ChatBotMessageRepository chatBotMessageRepository;

	@Test
	@DisplayName("첫 페이지를 커서 기반으로 조회하면 최신 순으로 지정된 개수의 메시지를 반환하고 hasNext가 true이다.")
	void findMessagesPage() {
		final Long CHAT_ROOM_ID = 1L;
		ChatRoom chatRoom = ChatRoomFixture.chatRoom(CHAT_ROOM_ID, null);

		List<Document> docs = new ArrayList<>();
		for (int i = 1; i <= 5; i++) {
			List<Document> recommendPlans = List.of(
				new Document("planId", "a-" + i).append("name", "요금제A-" + i),
				new Document("planId", "b-" + i).append("name", "요금제B-" + i)
			);
			docs.add(new Document()
				.append("_id", new ObjectId())
				.append("chat_room_id", CHAT_ROOM_ID)
				.append("content", String.valueOf(i))
				.append("owner", i % 2 == 0)
				.append("recommend_plan", recommendPlans)
			);
		}
		mongoTemplate.getDb().getCollection(COLLECTION_NAME).insertMany(docs);

		PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);
		CursorPageResponse<ChatMessageDto> response =
			chatBotMessageRepository.findMessagesPage(chatRoom, pageRequest, null);

		assertThat(response.item()).hasSize(PAGE_SIZE);
		assertThat(response.hasNext()).isTrue();
		assertThat(response.nextCursor()).isNotNull();

		assertThat(response.item())
			.extracting(ChatMessageDto::content)
			.containsExactly("5", "4", "3");

		assertThat(response.item())
			.extracting(ChatMessageDto::owner)
			.containsExactly(false, true, false);

		assertThat(response.item())
			.flatExtracting(ChatMessageDto::recommendPlans)
			.extracting(PlanDto::planId)
			.contains("a-5", "b-5", "a-4", "b-4", "a-3", "b-3");
	}

	@Test
	@DisplayName("마지막 페이지를 커서 기반으로 조회하면 남은 메시지만 반환하고 hasNext가 false여야 한다.")
	void findMessagesPage_lastPage() {
		final Long CHAT_ROOM_ID = 2L;
		ChatRoom chatRoom = ChatRoomFixture.chatRoom(CHAT_ROOM_ID, null);

		List<Document> docs = new ArrayList<>();
		for (int i = 1; i <= 5; i++) {
			List<Document> recommendPlans = List.of(
				new Document("planId", "a-" + i).append("name", "요금제A-" + i),
				new Document("planId", "b-" + i).append("name", "요금제B-" + i)
			);
			docs.add(new Document()
				.append("_id", new ObjectId())
				.append("chat_room_id", CHAT_ROOM_ID)
				.append("content", String.valueOf(i))
				.append("owner", i % 2 == 0)
				.append("recommend_plan", recommendPlans)
			);
		}
		mongoTemplate.getDb().getCollection(COLLECTION_NAME).insertMany(docs);

		PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);
		CursorPageResponse<ChatMessageDto> firstPage =
			chatBotMessageRepository.findMessagesPage(chatRoom, pageRequest, null);

		CursorPageResponse<ChatMessageDto> secondPage =
			chatBotMessageRepository.findMessagesPage(chatRoom, pageRequest, firstPage.nextCursor());

		assertThat(secondPage.item()).hasSize(2);
		assertThat(secondPage.hasNext()).isFalse();
		assertThat(secondPage.nextCursor()).isNull();
		assertThat(secondPage.item())
			.extracting(ChatMessageDto::content)
			.containsExactly("2", "1");
	}

	@Test
	@DisplayName("메시지가 없는 채팅방을 조회하면 빈 목록과 함께 hasNext는 false를 반환한다.")
	void findMessagesPage_empty() {
		final Long CHAT_ROOM_ID = 3L;
		ChatRoom chatRoom = ChatRoomFixture.chatRoom(CHAT_ROOM_ID, null);

		CursorPageResponse<ChatMessageDto> response =
			chatBotMessageRepository.findMessagesPage(chatRoom, PageRequest.of(0, 3), null);

		assertThat(response.item()).isEmpty();
		assertThat(response.hasNext()).isFalse();
		assertThat(response.nextCursor()).isNull();
	}

}
