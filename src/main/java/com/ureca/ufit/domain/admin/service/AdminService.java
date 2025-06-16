package com.ureca.ufit.domain.admin.service;

import static com.ureca.ufit.domain.rateplan.exception.RatePlanErrorCode.*;

import org.springframework.stereotype.Service;

import com.ureca.ufit.domain.admin.dto.RatePlanMapper;
import com.ureca.ufit.domain.admin.dto.request.CreateRatePlanRequest;
import com.ureca.ufit.domain.admin.dto.response.AdminRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.ChatBotReviewResponse;
import com.ureca.ufit.domain.admin.dto.response.CreateRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.DeleteRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.RatePlanStatusResponse;
import com.ureca.ufit.domain.chatbot.repository.ChatBotReviewRepository;
import com.ureca.ufit.domain.rateplan.exception.RatePlanErrorCode;
import com.ureca.ufit.domain.rateplan.repository.RatePlanRepository;
import com.ureca.ufit.entity.RatePlan;
import com.ureca.ufit.global.dto.CursorPageResponse;
import com.ureca.ufit.global.exception.RestApiException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final RatePlanRepository ratePlanRepository;
	private final ChatBotReviewRepository chatBotReviewRepository;

	public CursorPageResponse<AdminRatePlanResponse> getRatePlansByCursor(String cursor, int size, String type) {
		return ratePlanRepository.getRatePlansByCursor(cursor, size, type);
	}

	public CreateRatePlanResponse createRatePlan(CreateRatePlanRequest createRatePlanRequest) {
		RatePlan savedRatePlan = ratePlanRepository.save(RatePlanMapper.toEntity(createRatePlanRequest));
		return RatePlanMapper.toCreateRateResponse();
	}

	public DeleteRatePlanResponse deleteRatePlan(String ratePlanId) {
		RatePlan ratePlan = ratePlanRepository.findById(ratePlanId)
			.orElseThrow(() -> new RestApiException(RatePlanErrorCode.RATE_PLAN_NOT_FOUND)
			);
		ratePlan.updateDeleteStatus();
		ratePlanRepository.save(ratePlan);

		return RatePlanMapper.toDeleteRateResponse();
	}

	//	public RatePlanMetricsResponse getRatePlanMetrics() {
	//		return RatePlanMapper.toRatePlanMetricsResponse();
	//	}

	public CursorPageResponse<ChatBotReviewResponse> getChatBotReview(String cursor, int size) {
		return chatBotReviewRepository.getChatBotReviewByCursor(cursor, size);
	}

	public RatePlanStatusResponse updateRatePlanSalesStatus(String ratePlanId) {
		RatePlan findRatePlan = ratePlanRepository.getById(ratePlanId);

		isDeleted(findRatePlan);

		findRatePlan.updateSalesStatus();
		RatePlan savedRatePlan = ratePlanRepository.save(findRatePlan);

		return new RatePlanStatusResponse(savedRatePlan.getId(), savedRatePlan.isEnabled());
	}

	private void isDeleted(RatePlan findRatePlan) {
		if (findRatePlan.isDeleted()) {
			throw new RestApiException(RATE_PLAN_ALREADY_DELETED);
		}
	}
}
