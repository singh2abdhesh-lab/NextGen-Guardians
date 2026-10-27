package com.enterprise.ai.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
        return WebClient.builder().exchangeStrategies(strategies);
    }

    @Bean("scannerClient")
    public WebClient scannerClient(WebClient.Builder builder,
                                    @Value("${ai.services.scanner-url}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean("riskClient")
    public WebClient riskClient(WebClient.Builder builder,
                                 @Value("${ai.services.risk-url}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean("auditClient")
    public WebClient auditClient(WebClient.Builder builder,
                                  @Value("${ai.services.audit-url}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean("qualityClient")
    public WebClient qualityClient(WebClient.Builder builder,
                                    @Value("${ai.services.quality-url}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean("llmClient")
    public WebClient llmClient(WebClient.Builder builder,
                                @Value("${ai.upstream.base-url}") String url) {
        return builder.baseUrl(url).build();
    }
}
