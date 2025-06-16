package com.ureca.ufit.domain.admin.service;


import static com.ureca.ufit.domain.rateplan.exception.RatePlanErrorCode.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ureca.ufit.domain.admin.dto.RatePlanMapper;
import com.ureca.ufit.domain.admin.dto.request.CreateRatePlanRequest;
import com.ureca.ufit.domain.admin.dto.response.AdminRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.ChatBotReviewResponse;
import com.ureca.ufit.domain.admin.dto.response.CreateRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.DeleteRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.RatePlanStatusResponse;
import com.ureca.ufit.domain.admin.dto.response.RatePlanMetricsResponse;
import com.ureca.ufit.domain.chatbot.repository.ChatBotReviewRepository;
import com.ureca.ufit.domain.rateplan.exception.RatePlanErrorCode;
import com.ureca.ufit.domain.rateplan.repository.RatePlanRepository;
import com.ureca.ufit.domain.user.repository.UserRepository;
import com.ureca.ufit.entity.RatePlan;
import com.ureca.ufit.global.dto.CursorPageResponse;
import com.ureca.ufit.global.exception.RestApiException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

	private final RatePlanRepository ratePlanRepository;
	private final ChatBotReviewRepository chatBotReviewRepository;
	private final UserRepository userRepository;

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

		long subscriberCount = userRepository.countByRatePlanId(ratePlanId);

		if (ratePlan.isEnabled()) {
			throw new RestApiException(RatePlanErrorCode.CANNOT_DELETE_WHILE_ENABLED);
		}
		if (subscriberCount > 0) {
			throw new RestApiException(RatePlanErrorCode.CANNOT_DELETE_WITH_SUBSCRIBERS);
		}

		ratePlan.updateDeleteStatus();
		ratePlanRepository.save(ratePlan);

		return RatePlanMapper.toDeleteRateResponse();
	}

	public RatePlanMetricsResponse getRatePlanMetrics(int page, int size) {

		Map<String, Long> subscriberMap = userRepository.countUsersByRatePlan().stream()
			.collect(Collectors.toMap(
				UserRepository.RatePlanCountProjection::getRatePlanId,
				UserRepository.RatePlanCountProjection::getCount
			));

		List<RatePlan> allPlans = new ArrayList<> (ratePlanRepository.findAll().stream()
			.filter(RatePlan::isEnabled)
			.filter(plan -> !plan.isDeleted())
			.toList());

		allPlans.sort(Comparator
			.comparingLong( (RatePlan plan) -> subscriberMap.getOrDefault(plan.getId(), 0L))
			.reversed()
			.thenComparing(RatePlan::getPlanName));

		int totalCount = allPlans.size();
		int from = (page - 1) * size;
		int to = Math.min(from + size, totalCount);
		List<RatePlan> items = (from >= totalCount)
			? List.of()
			: allPlans.subList(from, to);

		return RatePlanMapper.toRatePlanMetricsResponse(items, subscriberMap, page, size, totalCount);
	}

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
