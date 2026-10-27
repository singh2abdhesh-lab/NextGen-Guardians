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

    @Bean("scannerWebClient")
    public WebClient scannerWebClient(WebClient.Builder builder,
                                       @Value("${ai.services.scanner-url}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean("riskWebClient")
    public WebClient riskWebClient(WebClient.Builder builder,
                                    @Value("${ai.services.risk-url}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean("auditWebClient")
    public WebClient auditWebClient(WebClient.Builder builder,
                                     @Value("${ai.services.audit-url}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean("qualityWebClient")
    public WebClient qualityWebClient(WebClient.Builder builder,
                                       @Value("${ai.services.quality-url}") String url) {
        return builder.baseUrl(url).build();
    }

    @Bean("llmWebClient")
    public WebClient llmWebClient(WebClient.Builder builder,
                                   @Value("${ai.upstream.base-url}") String url) {
        return builder.baseUrl(url).build();
    }
}

