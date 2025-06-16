package com.ureca.ufit.domain.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.ureca.ufit.domain.admin.dto.request.CreateRatePlanRequest;
import com.ureca.ufit.domain.admin.dto.response.AdminRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.ChatBotReviewResponse;
import com.ureca.ufit.domain.admin.dto.response.CreateRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.DeleteRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.RatePlanStatusResponse;
import com.ureca.ufit.global.dto.CursorPageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin API", description = "어드민 관련 API")
public interface AdminControllerApiSpec {

	@Operation(
		summary = "요금제 목록 조회",
		description = "Cursor 기반 페이지네이션 + 정렬/필터링으로 요금제 목록을 조회한다.",
		parameters = {
			@Parameter(
				name = "cursor",
				description = "다음 페이지 조회를 위한 커서(ObjectId). 없으면 최신부터 조회",
				in = ParameterIn.QUERY,
				example = "665f3af6d6795e4f5e8e5a12 || 16000/665f3af6d6795e4f5e8e5a12"
			),
			@Parameter(
				name = "size",
				description = "한 번에 가져올 개수(1~20)",
				in = ParameterIn.QUERY,
				schema = @Schema(type = "integer", defaultValue = "20", minimum = "1", maximum = "20")
			),
			@Parameter(
				name = "type",
				description = "정렬·필터 타입",
				in = ParameterIn.QUERY,
				schema = @Schema(allowableValues = {"", "date", "lowestPrice", "highestPrice"})
			)
		}
	)
	@ApiResponses(
		@ApiResponse(
			responseCode = "200", description = "조회 성공",
			content = @Content(schema = @Schema(implementation = CursorPageResponse.class))
		)
	)
	@GetMapping("/api/admin/rateplans")
	public ResponseEntity<CursorPageResponse<AdminRatePlanResponse>> getRatePlansByCursor(
		@RequestParam(name = "cursor", required = false) String cursor,
		@RequestParam(name = "size", defaultValue = "20") int size,
		@RequestParam(name = "type", required = false) String type
	);

	@Operation(summary = "요금제 생성", description = "새로운 요금제를 등록한다.")
	@ApiResponses(@ApiResponse(
		responseCode = "201", description = "등록 성공",
		content = @Content(schema = @Schema(implementation = CreateRatePlanResponse.class))
	))
	@PostMapping("/api/admin/rateplans")
	public ResponseEntity<CreateRatePlanResponse> createRatePlan(
		@Parameter(description = "요금제 정보", required = true)
		@RequestBody CreateRatePlanRequest createRatePlanRequest
	);

	@Operation(summary = "요금제 삭제", description = "요금제를 논리적으로 삭제한다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "삭제 성공",
			content = @Content(schema = @Schema(implementation = DeleteRatePlanResponse.class))),
		@ApiResponse(responseCode = "400", description = "삭제된 요금제"),
		@ApiResponse(responseCode = "404", description = "요금제 미존재")
	})
	@PostMapping("/api/admin/rateplans/{ratePlanId}")
	public ResponseEntity<DeleteRatePlanResponse> deleteRatePlan(
		@Parameter(description = "요금제 ID", required = true, example = "665f3af6d6795e4f5e8e5a12")
		@PathVariable String ratePlanId
	);

	@Operation(
		summary = "챗봇 리뷰 목록 조회",
		description = "Cursor 기반 페이지네이션으로 챗봇 리뷰를 조회한다.",
		parameters = {
			@Parameter(
				name = "cursor",
				description = "다음 페이지 커서(ObjectId)",
				in = ParameterIn.QUERY,
				example = "665f84e6df4b8c1df0f65b21"
			),
			@Parameter(
				name = "size",
				description = "한 번에 가져올 개수(1~20)",
				in = ParameterIn.QUERY,
				schema = @Schema(type = "integer", defaultValue = "10", minimum = "1", maximum = "20")
			)
		}
	)
	@ApiResponses(@ApiResponse(
		responseCode = "200", description = "조회 성공",
		content = @Content(schema = @Schema(implementation = CursorPageResponse.class))
	))
	@GetMapping("/api/admin/chats/reviews")
	public ResponseEntity<CursorPageResponse<ChatBotReviewResponse>> getChatBotReviewByCursor(
		@RequestParam(name = "cursor", required = false) String cursor,
		@RequestParam(name = "size", defaultValue = "10") int size
	);

	@Operation(summary = "요금제 판매 상태 변경", description = "삭제되지 않은 요금제의 판매 상태를 변경한다.")
	@ApiResponses(
		@ApiResponse(
			responseCode = "200", description = "요금제 상태 변경 성공",
			content = @Content(schema = @Schema(implementation = RatePlanStatusResponse.class))
		)
	)
	@PatchMapping("/api/admin/rateplans/{rateplanId}")
	public ResponseEntity<RatePlanStatusResponse> updateRatePlanSalesStatus(
		@PathVariable(name = "ratePlanId") String ratePlanId
	);
}
