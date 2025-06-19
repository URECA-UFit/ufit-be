package com.ureca.ufit.global.profanity;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

public class ProfanityFilter {

	private final Trie trie;
	private final List<Pattern> attackPatterns;

	public ProfanityFilter(List<String> bannedWords) {
		this.trie = Trie.builder()
			.ignoreCase()
			.addKeywords(bannedWords)
			.build();

		this.attackPatterns = List.of(
			//SQL Injection
			Pattern.compile("(?i)(\\bor\\b|\\band\\b)?\\s*['\"].*?['\"]\\s*(=|>|<|like)?\\s*['\"].*?['\"]"),
			Pattern.compile("(?i)(union(.*?)select)"),
			Pattern.compile("(?i)(drop|insert|delete|update|select|truncate|alter)\\s+\\w+"),
			Pattern.compile("(?i)(--)|(#)"),

			// XSS
			Pattern.compile("(?i)<script.*?>.*?</script.*?>"),
			Pattern.compile("(?i)(onerror|onload|alert|prompt|confirm)\\s*="),
			Pattern.compile("(?i)<.*?javascript:.*?>"),
			Pattern.compile("(?i)<iframe.*?>.*?</iframe>"),

			// 기타 명령어
			Pattern.compile("(?i)exec\\s+(xp_cmdshell|sp_)"),
			Pattern.compile("(?i)benchmark\\s*\\(.*?\\)"),
			Pattern.compile("(?i)sleep\\s*\\(\\s*[0-9]+\\s*\\)")
		);
	}

	// 정제된 데이터 반환
	public String normalize(String input, Set<BanwordFilterPolicy> policies) {
		if (input == null || policies.isEmpty()) {
			return input;
		}
		String result = input;
		for (BanwordFilterPolicy p : policies) {
			result = result.replaceAll(p.getRegex(), "");
		}
		return result;
	}

	public boolean containsBannedword(String text) {
		Collection<Emit> emits = trie.parseText(text);
		return !emits.isEmpty();
	}

	public boolean containsAttackPattern(String text) {
		if (text == null) {
			return false;
		}
		return attackPatterns.stream().anyMatch(p -> p.matcher(text).find());
	}

	public boolean isBanned(String text, Set<BanwordFilterPolicy> policies) {
		if (text == null)
			return false;

		if (containsAttackPattern(text))
			return true;
		if (containsBannedword(text))
			return true;

		String cleanedText = normalize(text, policies);
		return containsBannedword(cleanedText);
	}
}
