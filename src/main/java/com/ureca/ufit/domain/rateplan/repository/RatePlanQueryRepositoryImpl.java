package com.ureca.ufit.domain.rateplan.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ureca.ufit.domain.admin.dto.response.AdminRatePlanResponse;
import com.ureca.ufit.domain.rateplan.dto.response.RatePlanDetailResponse;
import com.ureca.ufit.domain.rateplan.dto.response.RatePlanPreviewResponse;
import com.ureca.ufit.entity.RatePlan;
import com.ureca.ufit.global.dto.CursorPageResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RatePlanQueryRepositoryImpl implements RatePlanQueryRepository {

	private static final String CURSOR = "_id";
	private static final String DATE = "date";
	private static final String LOWEST_PRICE = "lowestPrice";
	private static final String HIGHEST_PRICE = "highestPrice";
	private static final String RATE_PLANS = "rate_plans";
	private static final String MONTHLY_FEE = "monthly_fee";

	private static final String IS_DELETED = "is_deleted";
	private static final String IS_ENABLED = "is_enabled";
	private static final String PRICE_DESC = "PRICE_DESC";
	private static final String NAME_ASC = "NAME_ASC";
	private static final String NAME_DESC = "NAME_DESC";

	private final MongoTemplate mongoTemplate;

	public CursorPageResponse<AdminRatePlanResponse> getRatePlansByCursor(
		String cursor, int size, String type
	) {
		Criteria criteria = new Criteria();
		if (cursor != null && !cursor.isBlank()) {
			if (LOWEST_PRICE.equalsIgnoreCase(type) || HIGHEST_PRICE.equalsIgnoreCase(type)) {
				String[] parts = cursor.split("/");
				int fee = Integer.parseInt(parts[0]);
				String id = parts[1];

				Criteria idLess = Criteria.where(CURSOR).lt(id);

				Criteria feeCriteria = getFeeCriteria(type, fee);
				criteria = getOperator(criteria, feeCriteria, fee, idLess);

			} else {
				criteria.and(CURSOR).lt(new ObjectId(cursor));
			}
		}

		Sort.Order primary = getPrimaryOrder(type);
		Sort.Order secondary = Sort.Order.desc(CURSOR);

		List<AggregationOperation> pipeline = new ArrayList<>();
		pipeline.add(Aggregation.match(criteria));

		if (type != null && isPee(type)) {
			pipeline.add(Aggregation.sort(Sort.by(primary, secondary)));
		} else {
			pipeline.add(Aggregation.sort(Sort.by(primary)));
		}
		pipeline.add(Aggregation.limit(size + 1));

		AggregationOperation project = Aggregation.project()
			.and("_id").as("ratePlanId")
			.and("plan_name").as("planName")
			.and("summary").as("summary")
			.and("monthly_fee").as("monthlyFee")
			.and("discount_fee").as("discountFee")
			.and("voice_allowance").as("voiceAllowance")
			.and("sms_allowance").as("smsAllowance")
			.and("basic_benefit").as("basicBenefit")
			.and("special_benefit").as("specialBenefit")
			.and("discount_benefit").as("discountBenefit")
			.and("createdAt").as("createdAt");

		pipeline.add(project);

		List<AdminRatePlanResponse> items = mongoTemplate.aggregate(
			Aggregation.newAggregation(pipeline),
			RATE_PLANS,
			AdminRatePlanResponse.class
		).getMappedResults();

		if (Objects.isNull(items) || items.isEmpty()) {
			return new CursorPageResponse<>(items, null, false);
		}

		boolean hasNext = items.size() > size;

		items = subLastPage(items, hasNext, size);

		String nextCursor = getNextCursor(items, hasNext, type);

		return new CursorPageResponse<>(items, nextCursor, hasNext);
	}

	private static boolean isPee(String type) {
		return LOWEST_PRICE.equalsIgnoreCase(type) || HIGHEST_PRICE.equalsIgnoreCase(type);
	}

	private Criteria getFeeCriteria(String type, int fee) {
		if (LOWEST_PRICE.equalsIgnoreCase(type)) {
			return Criteria.where(MONTHLY_FEE).gt(fee);
		}
		return Criteria.where(MONTHLY_FEE).lt(fee);
	}

	private Criteria getOperator(Criteria criteria, Criteria feeCriteria, int fee, Criteria idLess) {
		return criteria.orOperator(feeCriteria,
			new Criteria().andOperator(
				Criteria.where(MONTHLY_FEE).is(fee), idLess));
	}

	private List<AdminRatePlanResponse> subLastPage(List<AdminRatePlanResponse> items, boolean hasNext, int size) {
		if (hasNext) {
			return items.subList(0, size);
		}

		return items;
	}

	private String getNextCursor(List<AdminRatePlanResponse> items, boolean hasNext, String type) {
		if (!hasNext || Objects.isNull(items) || items.isEmpty()) {
			return null;
		}

		AdminRatePlanResponse lastItem = items.get(items.size() - 1);

		if (isPee(type)) {
			return lastItem.monthlyFee() + "/" + lastItem.ratePlanId();
		}

		return lastItem.ratePlanId().toString();
	}

	private Sort.Order getPrimaryOrder(String type) {
		if (LOWEST_PRICE.equalsIgnoreCase(type)) {
			return Sort.Order.asc(MONTHLY_FEE);
		} else if (HIGHEST_PRICE.equalsIgnoreCase(type)) {
			return Sort.Order.desc(MONTHLY_FEE);
		}
		return Sort.Order.desc(CURSOR);
	}

	@Override
	public Page<RatePlan> findEnabledRatePlansWithSort(Pageable pageable, String sortType) {
		Criteria criteria = Criteria.where(IS_ENABLED).is(true)
			.and(IS_DELETED).is(false);

		Sort sort;
		if (NAME_ASC.equalsIgnoreCase(sortType)) {
			sort = Sort.by("planName").ascending();
		} else if (NAME_DESC.equalsIgnoreCase(sortType)) {
			sort = Sort.by("planName").descending();
		} else if (PRICE_DESC.equalsIgnoreCase(sortType)) {
			sort = Sort.by("monthlyFee").descending();
		} else {
			sort = Sort.by("monthlyFee").ascending();
		}

		Query query = new Query(criteria)
			.with(sort)
			.skip(pageable.getOffset())
			.limit(pageable.getPageSize());

		List<RatePlan> items = mongoTemplate.find(query, RatePlan.class);
		long total = items.size();

		return new PageImpl<>(items, pageable, total);
	}

	@Override
	public Page<RatePlanPreviewResponse> getRatePlanPreviews(Pageable pageable, String sortType) {
		Criteria criteria = Criteria.where(IS_ENABLED).is(true)
			.and(IS_DELETED).is(false);

		Sort sort;
		if (NAME_ASC.equalsIgnoreCase(sortType)) {
			sort = Sort.by("plan_name").ascending();
		} else if (NAME_DESC.equalsIgnoreCase(sortType)) {
			sort = Sort.by("plan_name").descending();
		} else if (PRICE_DESC.equalsIgnoreCase(sortType)) {
			sort = Sort.by("monthly_fee").descending();
		} else {
			sort = Sort.by("monthly_fee").ascending();
		}

		Query query = new Query(criteria)
			.with(sort)
			.skip(pageable.getOffset())
			.limit(pageable.getPageSize());

		List<RatePlanPreviewResponse> results = mongoTemplate.find(query, RatePlanPreviewResponse.class, RATE_PLANS);
		long total = results.size();

		return new PageImpl<>(results, pageable, total);
	}

	@Override
	public Optional<RatePlanDetailResponse> getRatePlanDetailById(String id) {
		Criteria criteria = Criteria.where("_id").is(id)
			.and(IS_ENABLED).is(true)
			.and(IS_DELETED).is(false);

		Query query = new Query(criteria);

		RatePlanDetailResponse result = mongoTemplate.findOne(
			query,
			RatePlanDetailResponse.class,
			"rate_plans"
		);

		return Optional.ofNullable(result);
	}

}
