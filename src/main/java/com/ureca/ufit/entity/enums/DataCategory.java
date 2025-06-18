package com.ureca.ufit.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DataCategory {
	FULL("web, kakaotalk, music, video, game"),
	STANDARD("web, kakaotalk, music"),
	BASIC("web, kakaotalk"),
	EMPTY("");

	private final String categories;
}
