package com.ureca.ufit.rateplan.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.ureca.ufit.common.fixture.RatePlanFixture;
import com.ureca.ufit.common.support.DataMongoSupport;
import com.ureca.ufit.domain.admin.dto.response.AdminRatePlanResponse;
import com.ureca.ufit.domain.rateplan.dto.response.RatePlanDetailResponse;
import com.ureca.ufit.domain.rateplan.dto.response.RatePlanPreviewResponse;
import com.ureca.ufit.domain.rateplan.repository.RatePlanQueryRepositoryImpl;
import com.ureca.ufit.domain.rateplan.repository.RatePlanRepository;
import com.ureca.ufit.entity.RatePlan;
import com.ureca.ufit.global.dto.CursorPageResponse;

class RatePlanQueryRepositoryTest extends DataMongoSupport {

	@Autowired
	RatePlanQueryRepositoryImpl ratePlanQueryRepositoryImpl;

	@Autowired
	RatePlanRepository ratePlanRepository;

	@AfterEach
	void tearDown() {
		ratePlanRepository.deleteAll();
	}

	@DisplayName("커서 기반으로 요금제 목록을 조회한다")
	@Test
	void getRatePlansByCursor() {
		final int SIZE = 2;
		final String TYPE = "lowestPrice";

		RatePlan plan1 = RatePlanFixture.ratePlan("plan1", 100);
		RatePlan plan2 = RatePlanFixture.ratePlan("plan2", 500);
		RatePlan plan3 = RatePlanFixture.ratePlan("plan3", 900);
		RatePlan plan4 = RatePlanFixture.ratePlan("plan4", 700);
		RatePlan plan5 = RatePlanFixture.ratePlan("plan5", 300);
		List<RatePlan> ratePlans = ratePlanRepository.saveAll(List.of(plan1, plan2, plan3, plan4, plan5));

		CursorPageResponse<AdminRatePlanResponse> response1 = ratePlanQueryRepositoryImpl.getRatePlansByCursor(
			null,
			SIZE,
			TYPE
		);

		CursorPageResponse<AdminRatePlanResponse> response2 = ratePlanQueryRepositoryImpl.getRatePlansByCursor(
			response1.nextCursor(),
			SIZE,
			TYPE
		);

		assertAll(
			() -> assertThat(response2.item().size()).isEqualTo(SIZE),
			() -> assertThat(response2.item().get(SIZE - 1).planName()).isEqualTo(plan4.getPlanName()),
			() -> assertThat(response2.nextCursor()).isEqualTo(plan4.getMonthlyFee() + "/" + plan4.getId())
		);
	}

	@DisplayName("낮은 가격 순으로 요금제 목록을 조회한다.")
	@Test
	void getRatePlansOrderByLowestPrice() {
		final int SIZE = 2;
		final String TYPE = "lowestPrice";

		RatePlan plan1 = RatePlanFixture.ratePlan("plan1", 100);
		RatePlan plan2 = RatePlanFixture.ratePlan("plan2", 500);
		RatePlan plan3 = RatePlanFixture.ratePlan("plan3", 900);
		RatePlan plan4 = RatePlanFixture.ratePlan("plan4", 700);
		RatePlan plan5 = RatePlanFixture.ratePlan("plan5", 300);
		ratePlanRepository.saveAll(List.of(plan1, plan2, plan3, plan4, plan5));

		CursorPageResponse<AdminRatePlanResponse> response = ratePlanQueryRepositoryImpl.getRatePlansByCursor(
			null,
			SIZE,
			TYPE
		);

		assertAll(
			() -> assertThat(response.item().size()).isEqualTo(SIZE),
			() -> assertThat(response.item().get(SIZE - 1).planName()).isEqualTo(plan5.getPlanName())
		);
	}

	@DisplayName("높은 가격 순으로 요금제 목록을 조회한다.")
	@Test
	void getRatePlansOrderByHighestPrice() {
		final int SIZE = 2;
		final String TYPE = "highestPrice";

		RatePlan plan1 = RatePlanFixture.ratePlan("plan1", 100);
		RatePlan plan2 = RatePlanFixture.ratePlan("plan2", 500);
		RatePlan plan3 = RatePlanFixture.ratePlan("plan3", 900);
		RatePlan plan4 = RatePlanFixture.ratePlan("plan4", 700);
		RatePlan plan5 = RatePlanFixture.ratePlan("plan5", 300);
		ratePlanRepository.saveAll(List.of(plan1, plan2, plan3, plan4, plan5));

		CursorPageResponse<AdminRatePlanResponse> response = ratePlanQueryRepositoryImpl.getRatePlansByCursor(
			null,
			SIZE,
			TYPE
		);

		assertAll(
			() -> assertThat(response.item().size()).isEqualTo(SIZE),
			() -> assertThat(response.item().get(SIZE - 1).planName()).isEqualTo(plan4.getPlanName())
		);
	}

	@DisplayName("요금제 목록이 비어있을 때 빈 목록이 조회된다.")
	@Test
	void getEmptyWhenRatePlanIsEmpty() {
		final int SIZE = 10;
		final String TYPE = "highestPrice";

		CursorPageResponse<AdminRatePlanResponse> response = ratePlanQueryRepositoryImpl.getRatePlansByCursor(
			null,
			SIZE,
			TYPE
		);

		assertAll(
			() -> assertThat(response.item().size()).isZero(),
			() -> assertThat(response.hasNext()).isFalse(),
			() -> assertThat(response.nextCursor()).isNull()
		);
	}

	@DisplayName("가격 내림차순으로 요금제 목록을 조회한다")
	@Test
	void getRatePlanPreviewsOrderByPriceDesc() {
		final int PAGE_SIZE = 3;

		RatePlan plan1 = RatePlanFixture.ratePlan("5G 베이직", 35000);
		RatePlan plan2 = RatePlanFixture.ratePlan("5G 프리미어", 115000);
		RatePlan plan3 = RatePlanFixture.ratePlan("5G 라이트", 55000);
		ratePlanRepository.saveAll(List.of(plan1, plan2, plan3));

		Pageable pageable = PageRequest.of(0, PAGE_SIZE);

		Page<RatePlanPreviewResponse> result =
			ratePlanQueryRepositoryImpl.getRatePlanPreviews(pageable, "PRICE_DESC");

		assertAll(
			() -> assertThat(result.getContent().size()).isEqualTo(3),
			() -> assertThat(result.getTotalElements()).isEqualTo(3),
			() -> assertThat(result.getContent().get(0).planName()).isEqualTo("5G 프리미어"),
			() -> assertThat(result.getContent().get(1).planName()).isEqualTo("5G 라이트"),
			() -> assertThat(result.getContent().get(2).planName()).isEqualTo("5G 베이직")
		);
	}

	@DisplayName("가격 오름차순으로 요금제 목록을 조회한다")
	@Test
	void getRatePlanPreviewsOrderByPriceAsc() {
		final int PAGE_SIZE = 3;

		RatePlan plan1 = RatePlanFixture.ratePlan("5G 베이직", 35000);
		RatePlan plan2 = RatePlanFixture.ratePlan("5G 프리미어", 115000);
		RatePlan plan3 = RatePlanFixture.ratePlan("5G 라이트", 55000);
		ratePlanRepository.saveAll(List.of(plan1, plan2, plan3));

		Pageable pageable = PageRequest.of(0, PAGE_SIZE);

		Page<RatePlanPreviewResponse> result =
			ratePlanQueryRepositoryImpl.getRatePlanPreviews(pageable, "PRICE_ASC");

		assertAll(
			() -> assertThat(result.getContent().size()).isEqualTo(3),
			() -> assertThat(result.getTotalElements()).isEqualTo(3),
			() -> assertThat(result.getContent().get(0).planName()).isEqualTo("5G 베이직"),
			() -> assertThat(result.getContent().get(1).planName()).isEqualTo("5G 라이트"),
			() -> assertThat(result.getContent().get(2).planName()).isEqualTo("5G 프리미어")
		);
	}

	@DisplayName("페이지네이션이 정상적으로 동작한다")
	@Test
	void getRatePlanPreviewsPaginationWorks() {
		RatePlan plan1 = RatePlanFixture.ratePlan("5G 베이직", 35000);
		RatePlan plan2 = RatePlanFixture.ratePlan("5G 프리미어", 115000);
		RatePlan plan3 = RatePlanFixture.ratePlan("5G 라이트", 55000);
		ratePlanRepository.saveAll(List.of(plan1, plan2, plan3));

		Pageable pageable = PageRequest.of(0, 2);

		Page<RatePlanPreviewResponse> result =
			ratePlanQueryRepositoryImpl.getRatePlanPreviews(pageable, "PRICE_DESC");

		assertAll(
			() -> assertThat(result.getContent().size()).isEqualTo(2),
			() -> assertThat(result.getTotalElements()).isEqualTo(3),
			() -> assertThat(result.hasNext()).isTrue()
		);
	}

	@DisplayName("저장된 요금제가 없을 때 빈 목록을 조회한다")
	@Test
	void getRatePlanPreviewsEmptyWhenNoData() {
		final int PAGE_SIZE = 10;
		Pageable pageable = PageRequest.of(0, PAGE_SIZE);

		Page<RatePlanPreviewResponse> result =
			ratePlanQueryRepositoryImpl.getRatePlanPreviews(pageable, "PRICE_DESC");

		assertAll(
			() -> assertThat(result.getContent().size()).isZero(),
			() -> assertThat(result.getTotalElements()).isZero(),
			() -> assertThat(result.hasNext()).isFalse()
		);
	}

	@DisplayName("존재하는 ID로 요금제 상세 정보를 조회한다")
	@Test
	void getRatePlanDetailReturnsData() {
		RatePlan plan = RatePlanFixture.ratePlan("5G 프리미어", 115000);
		RatePlan savedPlan = ratePlanRepository.save(plan);
		String id = savedPlan.getId();

		Optional<RatePlanDetailResponse> result =
			ratePlanQueryRepositoryImpl.getRatePlanDetailById(id);

		assertThat(result).isPresent();
		RatePlanDetailResponse detail = result.get();

		assertAll(
			() -> assertThat(detail.planName()).isEqualTo("5G 프리미어"),
			() -> assertThat(detail.monthlyFee()).isEqualTo(115000),
			() -> assertThat(detail.discountFee()).isEqualTo(1000),
			() -> assertThat(detail.dataAllowance()).isEqualTo("100G"),
			() -> assertThat(detail.voiceAllowance()).isEqualTo("무제한"),
			() -> assertThat(detail.smsAllowance()).isEqualTo("무제한"),
			() -> assertThat(detail.basicBenefit()).containsEntry("benefit", "basic"),
			() -> assertThat(detail.specialBenefit()).isEmpty(),
			() -> assertThat(detail.discountBenefit()).isEmpty()
		);
	}

	@DisplayName("존재하지 않는 ID로 요금제 상세 정보를 조회하면 빈 값을 반환한다")
	@Test
	void getRatePlanDetailReturnsEmptyForInvalidId() {
		String fakeId = "666f6f2d6261722d71757778";

		Optional<RatePlanDetailResponse> result =
			ratePlanQueryRepositoryImpl.getRatePlanDetailById(fakeId);

		assertThat(result).isNotPresent();
	}
}
