package com.ureca.ufit.domain.rateplan.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ureca.ufit.domain.rateplan.dto.response.RatePlanDetailResponse;
import com.ureca.ufit.domain.rateplan.dto.response.RatePlanPreviewResponse;
import com.ureca.ufit.domain.rateplan.service.RatePlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rateplans/storages")
@RequiredArgsConstructor
public class RatePlanController {

	private final RatePlanService ratePlanService;

	@GetMapping
	public ResponseEntity<Page<RatePlanPreviewResponse>> getRatePlans(
		@RequestParam(value = "type", required = false) String type,
		Pageable pageable
	) {
		return ResponseEntity.ok(ratePlanService.getRatePlanList(pageable, type));
	}

	@GetMapping("/{rateplanId}")
	public ResponseEntity<RatePlanDetailResponse> getRatePlan(@PathVariable("rateplanId") String id) {
		return ResponseEntity.ok(ratePlanService.getRatePlanDetail(id));
	}
}
