package com.ureca.ufit.domain.chatbot.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ureca.ufit.domain.chatbot.dto.request.CreateChatBotMessageRequest;
import com.ureca.ufit.domain.chatbot.dto.request.CreateChatBotReviewRequest;
import com.ureca.ufit.domain.chatbot.dto.response.ChatMessageDto;
import com.ureca.ufit.domain.chatbot.dto.response.ChatRoomCreateResponse;
import com.ureca.ufit.domain.chatbot.dto.response.CreateChatBotMessageResponse;
import com.ureca.ufit.domain.chatbot.dto.response.CreateChatBotReviewResponse;
import com.ureca.ufit.global.auth.details.CustomUserDetails;
import com.ureca.ufit.global.dto.CursorPageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@Tag(name = "ChatBot API", description = "챗봇 관련 API")
@RequestMapping("/api/chats")
public interface ChatBotControllerApiSpec {

	@Operation(
		summary = "챗봇 대화 메시지 목록 조회 API",
		description = "Cursor 방식으로 특정 채팅방의 대화 메시지를 페이지네이션 조회한다.",
		parameters = {
			@Parameter(
				name = "chatroomId",
				description = "채팅방 ID",
				in = ParameterIn.PATH,
				required = true,
				example = "42"
			),
			@Parameter(
				name = "lastMessageId",
				description = "마지막으로 수신한 메시지의 ID(ObjectId) — 해당 ID보다 _신규_ 메시지만 가져온다\n" +
					"(값이 없으면 최신부터 조회)",
				in = ParameterIn.QUERY,
				required = false,
				example = "665f3af6d6795e4f5e8e5a12"
			)
		}
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공",
			content = @Content(
				mediaType = "application/json",
				schema = @Schema(implementation = CursorPageResponse.class)
			)
		),
		@ApiResponse(responseCode = "404", description = "채팅방이 존재하지 않음")
	})
	@GetMapping("/{chatroomId}")
	public ResponseEntity<CursorPageResponse<ChatMessageDto>> getMessages(
		@PathVariable("chatroomId") Long chatRoomId,
		@RequestParam(value = "lastMessageId", required = false) String lastMessageId,
		@PageableDefault(size = 10) Pageable pageable
	);

	@Operation(
		summary = "채팅방 생성/조회 API",
		description = "해당 사용자의 채팅방이 이미 존재하면 ID를 반환하고, 없으면 새로 생성한다."
	)
	@ApiResponses(@ApiResponse(
		responseCode = "200", description = "채팅방 생성 또는 기존 ID 반환",
		content = @Content(schema = @Schema(implementation = ChatRoomCreateResponse.class))
	))
	@PostMapping("/rooms")
	public ResponseEntity<ChatRoomCreateResponse> getOrCreateChatRoom(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails userDetails
	);

	@Operation(
		summary = "챗봇 리뷰 저장 API",
		description = "대화 요약·추천 요금제와 함께 사용자의 리뷰를 저장한다."
	)
	@ApiResponses(@ApiResponse(
		responseCode = "201", description = "리뷰 저장 성공",
		content = @Content(schema = @Schema(implementation = CreateChatBotReviewResponse.class))
	))
	@PostMapping("/review")
	public ResponseEntity<CreateChatBotReviewResponse> createChatBotReview(
		@RequestBody @Valid CreateChatBotReviewRequest request
	);

	@Operation(
		summary = "챗봇 메시지 저장 API",
		description = "사용자 메시지를 저장하고, AI 답변을 반환한다."
	)
	@ApiResponses(@ApiResponse(
		responseCode = "201", description = "메시지 저장 & 답변 완료",
		content = @Content(schema = @Schema(implementation = CreateChatBotMessageResponse.class))
	))
	@PostMapping("/message")
	public ResponseEntity<CreateChatBotMessageResponse> createChatBotMessage(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid CreateChatBotMessageRequest request
	);

	@Operation(
		summary = "챗봇 메시지 저장 API(WebClient)",
		description = "사용자 메시지를 저장하고, AI 답변을 반환한다."
	)
	@ApiResponses(@ApiResponse(
		responseCode = "201", description = "메시지 저장 & 답변 완료",
		content = @Content(schema = @Schema(implementation = CreateChatBotMessageResponse.class))
	))
	@PostMapping("/message/webclient")
	public ResponseEntity<Mono<CreateChatBotMessageResponse>> createChatBotMessageWithWebClient(
		@Parameter(hidden = true)
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid CreateChatBotMessageRequest request
	);

}
