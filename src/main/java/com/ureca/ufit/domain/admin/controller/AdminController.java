package com.ureca.ufit.domain.admin.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ureca.ufit.domain.admin.dto.request.CreateRatePlanRequest;
import com.ureca.ufit.domain.admin.dto.response.AdminRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.ChatBotReviewResponse;
import com.ureca.ufit.domain.admin.dto.response.CreateRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.DeleteRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.RatePlanMetricsResponse;
import com.ureca.ufit.domain.admin.service.AdminService;
import com.ureca.ufit.global.dto.CursorPageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminController implements AdminControllerApiSpec {

	private final AdminService adminService;

	@Override
	public ResponseEntity<CursorPageResponse<AdminRatePlanResponse>> getRatePlansByCursor(String cursor, int size,
		String type) {
		CursorPageResponse<AdminRatePlanResponse> response = adminService.getRatePlansByCursor(cursor, size, type);
		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<CreateRatePlanResponse> createRatePlan(CreateRatePlanRequest createRatePlanRequest) {
		CreateRatePlanResponse response = adminService.createRatePlan(createRatePlanRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Override
	public ResponseEntity<DeleteRatePlanResponse> deleteRatePlan(String ratePlanId) {
		DeleteRatePlanResponse response = adminService.deleteRatePlan(ratePlanId);
		return ResponseEntity.ok(response);
	}

	// 요금제 지표 조회
	@GetMapping("/api/admin/rateplans/metrics")
	public ResponseEntity<RatePlanMetricsResponse> getRatePlanMetrics(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size
	){
	   RatePlanMetricsResponse response = adminService.getRatePlanMetrics(page, size);
	   return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<CursorPageResponse<ChatBotReviewResponse>> getChatBotReviewByCursor(String cursor, int size) {
		CursorPageResponse<ChatBotReviewResponse> response = adminService.getChatBotReview(cursor, size);
		return ResponseEntity.ok(response);
	}

}
