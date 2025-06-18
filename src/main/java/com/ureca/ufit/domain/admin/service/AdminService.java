package com.ureca.ufit.domain.admin.service;

import static com.ureca.ufit.domain.rateplan.exception.RatePlanErrorCode.*;
import static com.ureca.ufit.entity.enums.DataCategory.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureca.ufit.domain.admin.dto.RatePlanMapper;
import com.ureca.ufit.domain.admin.dto.request.CallRatePlanRequest;
import com.ureca.ufit.domain.admin.dto.request.CreateRatePlanRequest;
import com.ureca.ufit.domain.admin.dto.response.AdminRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.CallRatePlanErrorResponse;
import com.ureca.ufit.domain.admin.dto.response.ChatBotReviewResponse;
import com.ureca.ufit.domain.admin.dto.response.CreateRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.DeleteRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.RatePlanMetricsResponse;
import com.ureca.ufit.domain.admin.dto.response.RatePlanStatusResponse;
import com.ureca.ufit.domain.admin.exception.FastAPIErrorCode;
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
	private static final String FAST_DATA = "빠른 데이터(다쓰면 최대 5Mbps)";
	private static final String NORMAL_DATA = "보통 데이터(다쓰면 최대 3Mbps)";
	private static final String SLOW_DATA = "느린 데이터(다쓰면 최대 1Mbps)";
	private static final String VERY_SLOW_DATA = "매우 느린 데이터(다쓰면 최대 400Kpbs)";

	@Value("${llm.base-url}")
	private String llmBaseUrl;

	private final RatePlanRepository ratePlanRepository;
	private final ChatBotReviewRepository chatBotReviewRepository;
	private final UserRepository userRepository;
	private final RestTemplate restTemplate;

	public CursorPageResponse<AdminRatePlanResponse> getRatePlansByCursor(String cursor, int size, String type) {
		return ratePlanRepository.getRatePlansByCursor(cursor, size, type);
	}

	public CreateRatePlanResponse createRatePlan(CreateRatePlanRequest createRatePlanRequest) {
		RatePlan savedRatePlan = ratePlanRepository.save(RatePlanMapper.toEntity(createRatePlanRequest));
		String dataCategory = createDataCategory(createRatePlanRequest.extraData());

		CallRatePlanRequest callRatePlanRequest = RatePlanMapper.toCallRatePlanRequest(createRatePlanRequest,
			savedRatePlan.getId(), dataCategory);

		String url = String.format("%s/api/admin/rateplans/%s", llmBaseUrl, savedRatePlan.getId());

		try {
			restTemplate.postForObject(url, callRatePlanRequest, String.class);
		} catch (HttpStatusCodeException e) {
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				CallRatePlanErrorResponse errorResponse = objectMapper.readValue(
					e.getResponseBodyAsString(),
					CallRatePlanErrorResponse.class
				);
				if (FastAPIErrorCode.EMBEDDING_CREATE_FAIL.equals(errorResponse.errorCode())) {
					ratePlanRepository.deleteById(savedRatePlan.getId());
					throw new RestApiException(FastAPIErrorCode.EMBEDDING_CREATE_FAIL);
				}
				throw new RestApiException(errorResponse.errorCode());
			} catch (Exception parseError) {
				ratePlanRepository.deleteById(savedRatePlan.getId());
				throw new RestApiException(FastAPIErrorCode.EMBEDDING_CREATE_FAIL);
			}
		} catch (Exception e) {
			ratePlanRepository.deleteById(savedRatePlan.getId());
			throw new RestApiException(FastAPIErrorCode.EMBEDDING_CREATE_FAIL);
		}

		return RatePlanMapper.toCreateRateResponse();
	}

	private static String createDataCategory(String extraData) {
		if (FAST_DATA.equals(extraData)) {
			return FULL.getCategories();
		} else if (NORMAL_DATA.equals(extraData)) {
			return FULL.getCategories();
		} else if (SLOW_DATA.equals(extraData)) {
			return STANDARD.getCategories();
		} else if (VERY_SLOW_DATA.equals(extraData)) {
			return BASIC.getCategories();
		}
		return EMPTY.getCategories();
	}

	public DeleteRatePlanResponse deleteRatePlan(String ratePlanId) {
		RatePlan findRatePlan = ratePlanRepository.findById(ratePlanId)
			.orElseThrow(() -> new RestApiException(RatePlanErrorCode.RATE_PLAN_NOT_FOUND)
			);

		long subscriberCount = userRepository.countByRatePlanId(ratePlanId);

		if (findRatePlan.isEnabled()) {
			throw new RestApiException(RatePlanErrorCode.CANNOT_DELETE_WHILE_ENABLED);
		}
		if (subscriberCount > 0) {
			throw new RestApiException(RatePlanErrorCode.CANNOT_DELETE_WITH_SUBSCRIBERS);
		}

		findRatePlan.updateDeleteStatus();
		ratePlanRepository.save(findRatePlan);

		String url = String.format("%s/api/admin/rateplans/%s", llmBaseUrl, ratePlanId);

		callDeleteRatePlanApi(url, findRatePlan);

		return RatePlanMapper.toDeleteRateResponse(findRatePlan.getId(), findRatePlan.isDeleted());
	}

	private void callDeleteRatePlanApi(String url, RatePlan ratePlan) {

		try {
			restTemplate.delete(url);
		} catch (HttpStatusCodeException e) {
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				CallRatePlanErrorResponse errorResponse = objectMapper.readValue(
					e.getResponseBodyAsString(),
					CallRatePlanErrorResponse.class
				);

				if (FastAPIErrorCode.RATE_PLAN_NOT_FOUND.equals(errorResponse.errorCode())) {
					return;
				}

				if (FastAPIErrorCode.EMBEDDING_DELETE_FAIL.equals(errorResponse.errorCode())) {
					ratePlan.updateDeleteStatus();
					ratePlanRepository.save(ratePlan);
					throw new RestApiException(FastAPIErrorCode.EMBEDDING_DELETE_FAIL);
				}

				throw new RestApiException(errorResponse.errorCode());

			} catch (Exception remainError) {
				ratePlan.updateDeleteStatus();
				ratePlanRepository.save(ratePlan);
				throw new RestApiException(FastAPIErrorCode.EMBEDDING_DELETE_FAIL);
			}
		} catch (Exception unexpectedError) {
			ratePlan.updateDeleteStatus();
			ratePlanRepository.save(ratePlan);
			throw new RestApiException(FastAPIErrorCode.EMBEDDING_DELETE_FAIL);
		}
	}

	public RatePlanMetricsResponse getRatePlanMetrics(int page, int size) {

		Map<String, Long> subscriberMap = userRepository.countUsersByRatePlan().stream()
			.collect(Collectors.toMap(
				UserRepository.RatePlanCountProjection::getRatePlanId,
				UserRepository.RatePlanCountProjection::getCount
			));

		List<RatePlan> allPlans = new ArrayList<>(ratePlanRepository.findAll().stream()
			.filter(RatePlan::isEnabled)
			.filter(plan -> !plan.isDeleted())
			.toList());

		allPlans.sort(Comparator
			.comparingLong((RatePlan plan) -> subscriberMap.getOrDefault(plan.getId(), 0L))
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
