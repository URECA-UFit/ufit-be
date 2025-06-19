package com.ureca.ufit.global.profanity;

public enum BanwordFilterPolicy {
	NUMBERS("[\\p{N}]"),  // 모든 숫자
	WHITESPACES("[\\s]"), // 공백 문자
	FOREIGN("[\\p{L}&&[^ㄱ-ㅎ가-힣ㅏ-ㅣa-zA-Z]]"); // 한글/영문 제외 문자 (특수문자)

	private final String regex;

	BanwordFilterPolicy(String regex) {
		this.regex = regex;
	}

	public String getRegex() {
		return regex;
	}
}
