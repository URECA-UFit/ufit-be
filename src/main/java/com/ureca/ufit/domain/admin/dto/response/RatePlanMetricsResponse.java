package com.ureca.ufit.domain.admin.dto.response;

import java.util.List;

public record RatePlanMetricsResponse(
	List<RatePlanMetricsItem> item,
	int page,
	int size,
	int offset,
	boolean hasPrevious,
	boolean hasNext
) { }
