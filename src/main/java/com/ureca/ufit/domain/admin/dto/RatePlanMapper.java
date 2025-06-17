package com.ureca.ufit.domain.admin.dto;

import static lombok.AccessLevel.*;

import java.util.List;
import java.util.Map;

import com.ureca.ufit.domain.admin.dto.request.CreateRatePlanRequest;
import com.ureca.ufit.domain.admin.dto.response.CreateRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.DeleteRatePlanResponse;
import com.ureca.ufit.domain.admin.dto.response.RatePlanMetricsItem;
import com.ureca.ufit.domain.admin.dto.response.RatePlanMetricsResponse;
import com.ureca.ufit.entity.RatePlan;

import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class RatePlanMapper {

	private static final String CREATED_MESSAGE = "요금제가 성공적으로 생성되었습니다.";
	private static final String DELETED_MESSAGE = "요금제가 성공적으로 삭제되었습니다.";

	public static RatePlan toEntity(CreateRatePlanRequest createRatePlanRequest) {
		return RatePlan.of(
			createRatePlanRequest.planName(),
			createRatePlanRequest.summary(),
			createRatePlanRequest.monthlyFee(),
			createRatePlanRequest.discountFee(),
			createRatePlanRequest.data_allowance(),
			createRatePlanRequest.voice_allowance(),
			createRatePlanRequest.sms_allowance(),
			createRatePlanRequest.basic_benefit()
		);
	}

	public static CreateRatePlanResponse toCreateRateResponse() {
		return new CreateRatePlanResponse(CREATED_MESSAGE);
	}

	public static DeleteRatePlanResponse toDeleteRateResponse(String ratePlanId, boolean isDeleted) {
		return new DeleteRatePlanResponse(DELETED_MESSAGE, ratePlanId, isDeleted);
	}

	public static RatePlanMetricsResponse toRatePlanMetricsResponse(
		List<RatePlan> item,
		Map<String, Long> subscriberCountMap,
		int page,
		int size,
		long totalCount
	) {
		List<RatePlanMetricsItem> items = item.stream()
			.map(plan -> new RatePlanMetricsItem(
				plan.getPlanName(),
				subscriberCountMap.getOrDefault(plan.getId(), 0L).intValue()
			)).toList();

		int offset = (page - 1) * size;
		boolean hasPrevious = page > 1;
		boolean hasNext = page * size < totalCount;
		return new RatePlanMetricsResponse(items, page, size, offset, hasPrevious, hasNext);
	}

}
