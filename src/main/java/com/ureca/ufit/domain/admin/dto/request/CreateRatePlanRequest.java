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
	String dataAllowance,
	@Schema(description = "통화 제공량", example = "집/이동전화 무제한(+부가통화 110분)")
	String voiceAllowance,
	@Schema(description = "문자 설명", example = "기본제공")
	String smsAllowance,
	@Schema
		(description = "기본 혜택", example = "{ \"name\": \"U+ 모바일 TV\", \"description\": \"U+ 모바일 TV 기본 월정액 무료\"}")
	@JsonProperty("basicBenefit")
	Map<String, Object> basicBenefit,
	@Schema
		(description = "특별 혜택",
			example = "{ \"name\": \"프리미엄 서비스 기본 제공(택1)\", "
				+ "\"description\": \"삼성팩, 애플디바이스팩, 멀티팩(아이들나라 스탠다드+러닝, 바이브 음악감상, 지니뮤직 음악감상, 밀리의 서재 중 1개 선택)\"}")
	@JsonProperty("specialBenefit")
	Map<String, Object> specialBenefit,
	@Schema
		(description = "할인 혜택",
			example = "{ \"name\": \"U+투게더 결합\", \"description\": \"U+ 휴대폰을 쓰는 친구와 함께 가입하면 추가 할인을 받을 수 있습니다.\"}")
	@JsonProperty("discountBenefit")
	Map<String, Object> discountBenefit,
	@Schema(description = "데이터량을 다 소모한 경우 추가 데이터", example = "다 쓰면 최대 5Mbps(빠른 데이터)")
	String extraData,
	@Schema(description = "모바일 외 디바이스 종류", example = "LTE 전용 태블릿, 빔, 액션 캠 등 스마트 기기")
	String deviceType,
	@Schema(description = "다른 기기 간 데이터 공유", example = "가능(기본 제공량 안에서 테더링+쉐어링 60GB)")
	String dataSharing,
	@Schema(description = "사회적 위치", example = "senior")
	String socialCategory
) {

}
