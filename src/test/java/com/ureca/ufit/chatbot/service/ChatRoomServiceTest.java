package com.ureca.ufit.chatbot.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ureca.ufit.common.fixture.ChatRoomFixture;
import com.ureca.ufit.common.fixture.UserFixture;
import com.ureca.ufit.domain.chatbot.dto.response.ChatRoomCreateResponse;
import com.ureca.ufit.domain.chatbot.repository.ChatRoomRepository;
import com.ureca.ufit.domain.chatbot.service.ChatRoomService;
import com.ureca.ufit.domain.user.repository.UserRepository;
import com.ureca.ufit.entity.ChatRoom;
import com.ureca.ufit.entity.User;
import com.ureca.ufit.entity.enums.Role;
import com.ureca.ufit.global.auth.details.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
public class ChatRoomServiceTest {

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private ChatRoomService chatRoomService;

	@Test
	@DisplayName("회원에 대해 기존 채팅방이 있을 때 기존 채팅방을 반환한다")
	public void getExistingChatRoom() {
		String email = "test@email.com";
		Long chatRoomId = 1L;

		User user = UserFixture.user(email);
		ChatRoom existingChatRoom = ChatRoomFixture.chatRoom(chatRoomId, user);

		given(userRepository.getByEmail(email)).willReturn(user);
		given(chatRoomRepository.findByUser(user)).willReturn(Optional.of(existingChatRoom));

		ChatRoomCreateResponse result = chatRoomService.getOrCreateChatRoom(
			new CustomUserDetails(1L, email, "pass", Role.USER));

		assertThat(result.chatRoomId()).isEqualTo(chatRoomId);
		assertThat(result.isAnonymous()).isFalse();
		then(chatRoomRepository).should(never()).save(any(ChatRoom.class));
	}

	@Test
	@DisplayName("회원에 대해 기존 채팅방이 없을 때 새로운 채팅방을 생성하여 반환한다")
	public void getOrCreateChatRoom_NoChatRoom_CreatesAndReturnsNewChatRoom() {
		String email = "test@email.com";
		Long chatRoomId = 2L;

		User user = UserFixture.user(email);
		ChatRoom newChatRoom = ChatRoomFixture.chatRoom(chatRoomId, user);

		given(userRepository.getByEmail(email)).willReturn(user);
		given(chatRoomRepository.findByUser(user)).willReturn(Optional.empty());
		given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(newChatRoom);

		ChatRoomCreateResponse result = chatRoomService.getOrCreateChatRoom(
			new CustomUserDetails(1L, email, "pass", Role.USER));

		assertThat(result.chatRoomId()).isEqualTo(chatRoomId);
		assertThat(result.isAnonymous()).isFalse();
		then(chatRoomRepository).should().save(any(ChatRoom.class));
	}

	@Test
	@DisplayName("비회원 사용자인 경우 새 채팅방을 생성하고 isAnonymous가 true인 응답을 반환한다")
	public void getOrCreateChatRoom_AnonymousUser() {
		Long chatRoomId = 3L;

		ChatRoom anonymousChatRoom = ChatRoomFixture.chatRoom(chatRoomId, null);
		given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(anonymousChatRoom);

		ChatRoomCreateResponse result = chatRoomService.getOrCreateChatRoom(null);

		assertThat(result.chatRoomId()).isEqualTo(chatRoomId);
		assertThat(result.isAnonymous()).isTrue();
		then(chatRoomRepository).should().save(any(ChatRoom.class));
	}
}
