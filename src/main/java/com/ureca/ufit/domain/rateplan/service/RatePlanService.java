package com.ureca.ufit.domain.rateplan.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ureca.ufit.domain.rateplan.dto.response.RatePlanDetailResponse;
import com.ureca.ufit.domain.rateplan.dto.response.RatePlanPreviewResponse;
import com.ureca.ufit.domain.rateplan.repository.RatePlanRepository;
import com.ureca.ufit.global.exception.CommonErrorCode;
import com.ureca.ufit.global.exception.RestApiException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RatePlanService {

	private final RatePlanRepository ratePlanRepository;

	public Page<RatePlanPreviewResponse> getRatePlanList(Pageable pageable, String sortType) {
		return ratePlanRepository.getRatePlanPreviews(pageable, sortType);
	}

	public RatePlanDetailResponse getRatePlanDetail(String id) {
		return ratePlanRepository.getRatePlanDetailById(id)
			.orElseThrow(() -> new RestApiException(CommonErrorCode.RATEPLAN_NOT_FOUND));
	}
}
