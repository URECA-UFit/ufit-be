package com.ureca.ufit.domain.admin.dto.request;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateRatePlanRequest(
	@Schema(description = "요금제 이름", example = "5G 프리미어 슈퍼")
	String planName,
	@Schema(description = "요금제 설명", example = "추가 요금 걱정 없이 SNS..")
	String summary,
	@Schema(description = "월 금액", example = "115000")
	int monthlyFee,
	@Schema(description = "할인 월 금액", example = "81000")
	int discountFee,
	@Schema(description = "데이터 제공량", example = "무제한")
	String data_allowance,
	@Schema(description = "통화 제공량", example = "집/이동전화 무제한(+부가통화 110분)")
	String voice_allowance,
	@Schema(description = "문자 설명", example = "기본제공")
	String sms_allowance,
	@Schema
		(description = "기본 혜택", example = "{ \"name\": \"U+ 모바일 TV\", \"description\": \"U+ 모바일 TV 기본 월정액 무료\"}")
	@JsonProperty("basic_benefit")
	Map<String, Object> basic_benefit,
	@Schema
		(description = "특별 혜택",
		example = "{ \"name\": \"프리미엄 서비스 기본 제공(택1)\", "
			+ "\"description\": \"삼성팩, 애플디바이스팩, 멀티팩(아이들나라 스탠다드+러닝, 바이브 음악감상, 지니뮤직 음악감상, 밀리의 서재 중 1개 선택)\"}")
	@JsonProperty("special_benefit")
	Map<String, Object> special_benefit,
	@Schema
		(description = "할인 혜택",
		example = "{ \"name\": \"U+투게더 결합\", \"description\": \"U+ 휴대폰을 쓰는 친구와 함께 가입하면 추가 할인을 받을 수 있습니다.\"}")
	@JsonProperty("discount_benefit")
	Map<String, Object> discount_benefit
) {

}
