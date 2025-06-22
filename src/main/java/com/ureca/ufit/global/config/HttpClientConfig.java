package com.ureca.ufit.global.config;

import java.time.Duration;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class HttpClientConfig {
	@Bean
	public RestTemplate restTemplate() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(100);
		cm.setDefaultMaxPerRoute(100);

		RequestConfig requestConfig = RequestConfig.custom()
			.setConnectTimeout(Timeout.ofMilliseconds(3_000))
			.setResponseTimeout(Timeout.ofSeconds(60))
			.build();

		CloseableHttpClient http = HttpClients.custom()
			.setConnectionManager(cm)
			.setDefaultRequestConfig(requestConfig)
			.evictIdleConnections(TimeValue.ofSeconds(30))
			.build();

		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(http));
	}

	@Bean
	public WebClient webClient() {
		ConnectionProvider provider = ConnectionProvider.builder("ufit-pool")
			.maxConnections(100)
			.maxIdleTime(Duration.ofSeconds(30))
			.pendingAcquireTimeout(Duration.ofSeconds(5))
			.build();

		HttpClient http = HttpClient.create(provider)
			.responseTimeout(Duration.ofSeconds(60))
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3_000);

		return WebClient.builder()
			.clientConnector(new ReactorClientHttpConnector(http))
			.build();
	}

}