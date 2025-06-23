package com.ureca.ufit.global.profanity;

import static com.ureca.ufit.global.profanity.BanwordFilterPolicy.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProfanityFilterTest {

	private ProfanityFilter profanityFilter;

	@BeforeEach
	void setUp() {
		List<String> bannedWords = List.of("바보", "멍청이");
		profanityFilter = new ProfanityFilter(bannedWords);
	}

	@DisplayName("욕설이 포함된 문장이면 금칙어 필터링이 작동된다.")
	@Test
	void testContainsBannedword() {
		String text = "너는 정말 바보 같아!";

		boolean isBanned = profanityFilter.containsBannedword(text);

		assertThat(isBanned).isTrue();
	}

	@DisplayName("해킹 시도가 있는 문장이면 금칙어 필터링이 작동된다.")
	@Test
	void testContainsAttackPattern() {
		String sqlInjection = "' OR 1=1 --";
		String xss = "<script>alert('x')</script>";

		boolean isSqlInjection = profanityFilter.containsAttackPattern(sqlInjection);
		boolean isXss = profanityFilter.containsAttackPattern(xss);

		assertThat(isSqlInjection).isTrue();
		assertThat(isXss).isTrue();
	}

	@DisplayName("우회 금칙어가 있는 문장이면 금칙어 필터링이 작동된다.")
	@Test
	void testBypassFilter() {
		String input = "바 보";
		String input2 = "바1보";
		Set<BanwordFilterPolicy> policies = Set.of(NUMBERS, WHITESPACES);

		boolean isWhitespaces = profanityFilter.isBanned(input, policies);
		boolean isNumber = profanityFilter.isBanned(input2, policies);

		assertThat(isWhitespaces).isTrue();
		assertThat(isNumber).isTrue();
	}

	@DisplayName("욕설과 해킹 시도가 없는 문장이면 금칙어 필터링이 작동하지 않는다.")
	@Test
	void testNoBanned() {
		String text = "안녕하세요. 좋은 하루 보내세요.";
		Set<BanwordFilterPolicy> policies = Set.of(NUMBERS, WHITESPACES);

		boolean isBanned = profanityFilter.isBanned(text, policies);

		assertThat(isBanned).isFalse();
	}
}