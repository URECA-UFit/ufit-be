package com.ureca.ufit.global.profanity;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class ProfanityService {
	private ProfanityFilter profanityFilter;

	@PostConstruct
	public void initFilter() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			InputStream inputStream = new ClassPathResource("banned-words.json").getInputStream();
			List<String> bannedWords = mapper.readValue(inputStream, new TypeReference<>() {
			});
			this.profanityFilter = new ProfanityFilter(bannedWords);

		} catch (Exception e) {
			throw new RuntimeException("금칙어 파일 로딩 실패", e);
		}
	}

	public boolean containsBannedWord(String text, Set<BanwordFilterPolicy> policies) {
		return profanityFilter.isBanned(text, policies);
	}
}